import { loadPyodide } from "./pyodide/pyodide.mjs";

const MAX_SOURCE_BYTES = 16 * 1024;
const MAX_OUTPUT_BYTES = 16 * 1024;
const outputEncoder = new TextDecoder("utf-8");

let pyodide;

function makeCapture() {
  const bytes = [];
  return {
    bytes,
    write(byte, shared) {
      if (shared.count < MAX_OUTPUT_BYTES) {
        bytes.push(byte);
        shared.count += 1;
      } else {
        shared.truncated = true;
      }
    },
    text() {
      return outputEncoder.decode(new Uint8Array(bytes));
    },
  };
}

try {
  pyodide = await loadPyodide({
    indexURL: new URL("./pyodide/", import.meta.url).href,
  });
  pyodide.setStdin({ error: true });
  self.postMessage({ type: "ready", version: pyodide.version });
} catch (error) {
  self.postMessage({
    type: "error",
    message: String(error?.message || error).slice(0, MAX_OUTPUT_BYTES),
  });
}

self.onmessage = ({ data }) => {
  if (!pyodide || data?.type !== "run") return;

  const source = data.source;
  if (typeof source !== "string") {
    self.postMessage({ type: "error", message: "Source must be a string." });
    return;
  }

  if (new TextEncoder().encode(source).byteLength > MAX_SOURCE_BYTES) {
    self.postMessage({
      type: "result",
      status: "RESOURCE_LIMIT",
      stdout: "",
      stderr: "",
      durationMs: 0,
      outputTruncated: false,
      diagnostic: "Source exceeds the 16 KiB limit.",
    });
    return;
  }

  const shared = { count: 0, truncated: false };
  const stdout = makeCapture();
  const stderr = makeCapture();
  pyodide.setStdout({ raw: (byte) => stdout.write(byte, shared) });
  pyodide.setStderr({ raw: (byte) => stderr.write(byte, shared) });

  const startedAt = performance.now();
  let status = "COMPLETED";
  let diagnostic = null;

  try {
    const result = pyodide.runPython(source, { filename: "<player>" });
    if (result && typeof result.destroy === "function") result.destroy();
  } catch (error) {
    const message = String(error?.message || error);
    status = message.includes("SyntaxError") ? "SYNTAX_ERROR" : "RUNTIME_ERROR";
    diagnostic = message.slice(0, MAX_OUTPUT_BYTES);
  }

  self.postMessage({
    type: "result",
    status,
    stdout: stdout.text(),
    stderr: stderr.text(),
    durationMs: Math.round(performance.now() - startedAt),
    outputTruncated: shared.truncated,
    diagnostic,
  });
};
