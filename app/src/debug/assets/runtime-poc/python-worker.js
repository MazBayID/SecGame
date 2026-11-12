import { loadPyodide } from "./pyodide/pyodide.mjs";

const MAX_SOURCE_BYTES = 16 * 1024;
const MAX_OUTPUT_BYTES = 16 * 1024;
const MAX_DIAGNOSTIC_CHARS = 1024;
const outputDecoder = new TextDecoder("utf-8");

let pyodide;
let activeRunId = null;
let isBusy = false;

function sendInitializationError(error) {
  self.postMessage({
    type: "initialization-error",
    message: String(error?.message || error).slice(0, MAX_DIAGNOSTIC_CHARS),
  });
}

function makeCapture(shared) {
  const bytes = [];
  return {
    write(byte) {
      if (shared.count < MAX_OUTPUT_BYTES) {
        bytes.push(byte);
        shared.count += 1;
      } else {
        shared.truncated = true;
      }
    },
    text() {
      return outputDecoder.decode(new Uint8Array(bytes));
    },
  };
}

function classifyError(error) {
  const message = String(error?.message || error).slice(0, MAX_DIAGNOSTIC_CHARS);
  const isSyntaxError = /SyntaxError/.test(message);
  const lineMatch = message.match(/line\s+(\d+)/i);
  const typeMatch = message.match(/(?:^|\n)([A-Za-z_][A-Za-z0-9_.]*Error|SyntaxError):/);

  return {
    kind: isSyntaxError ? "SYNTAX" : "RUNTIME",
    exceptionType: (typeMatch?.[1] || (isSyntaxError ? "SyntaxError" : "PythonError")).slice(0, 128),
    line: lineMatch ? Number(lineMatch[1]) : null,
    message,
  };
}

try {
  pyodide = await loadPyodide({
    indexURL: new URL("./pyodide/", import.meta.url).href,
    // Never expose the real worker global scope through Python's built-in js module.
    // Player code receives an empty, immutable JavaScript namespace instead.
    jsglobals: Object.freeze(Object.create(null)),
  });
  pyodide.setStdin({ error: true });

  // Fail closed if Pyodide ever exposes worker globals to Python despite jsglobals.
  pyodide.runPython(`
import js
import pyodide_js
for namespace_name, namespace in (("js", js), ("pyodide_js", pyodide_js)):
    for name in ("self", "globalThis", "postMessage", "fetch", "Worker", "document", "window"):
        if hasattr(namespace, name):
            raise RuntimeError(
                namespace_name + " unexpectedly exposes JavaScript global: " + name
            )

# The public Pyodide API exposes JavaScript functions. CSP must block the
# Function-constructor escape route from one of those functions to globalThis.
if not hasattr(pyodide_js, "runPython"):
    raise RuntimeError("Pyodide JavaScript API shape changed; escape probe unavailable")
try:
    pyodide_js.runPython.constructor("return globalThis")()
except Exception as error:
    error_text = str(error)
    if "unsafe-eval" not in error_text and "EvalError" not in type(error).__name__:
        raise RuntimeError("Could not verify CSP blocks JavaScript Function constructor")
else:
    raise RuntimeError("CSP allowed JavaScript Function constructor access")
`);

  self.postMessage({ type: "ready", version: pyodide.version });
} catch (error) {
  sendInitializationError(error);
}

self.onmessage = ({ data }) => {
  if (!pyodide || isBusy || data?.type !== "run") return;
  if (typeof data.runId !== "string" || data.runId.length < 16 ||
      data.runId.length > 128) return;

  const source = data.source;
  if (typeof source !== "string") {
    self.postMessage({
      type: "result",
      runId: data.runId,
      status: "RESOURCE_LIMIT",
      stdout: "",
      stderr: "",
      durationMs: 0,
      outputTruncated: false,
      diagnostic: {
        kind: "RESOURCE",
        exceptionType: "InvalidRequest",
        line: null,
        message: "Source must be a string.",
      },
    });
    return;
  }

  if (new TextEncoder().encode(source).byteLength > MAX_SOURCE_BYTES) {
    self.postMessage({
      type: "result",
      runId: data.runId,
      status: "RESOURCE_LIMIT",
      stdout: "",
      stderr: "",
      durationMs: 0,
      outputTruncated: false,
      diagnostic: {
        kind: "RESOURCE",
        exceptionType: "SourceTooLarge",
        line: null,
        message: "Source exceeds the 16 KiB limit.",
      },
    });
    return;
  }

  isBusy = true;
  activeRunId = data.runId;

  const shared = { count: 0, truncated: false };
  const stdout = makeCapture(shared);
  const stderr = makeCapture(shared);
  pyodide.setStdout({ raw: (byte) => stdout.write(byte) });
  pyodide.setStderr({ raw: (byte) => stderr.write(byte) });

  const startedAt = performance.now();
  let status = "COMPLETED";
  let diagnostic = null;

  try {
    const result = pyodide.runPython(source, { filename: "<player>" });
    if (result && typeof result.destroy === "function") result.destroy();
  } catch (error) {
    diagnostic = classifyError(error);
    status = diagnostic.kind === "SYNTAX" ? "SYNTAX_ERROR" : "RUNTIME_ERROR";
  }

  if (shared.truncated && status === "COMPLETED") status = "OUTPUT_LIMIT";

  self.postMessage({
    type: "result",
    runId: activeRunId,
    status,
    stdout: stdout.text(),
    stderr: stderr.text(),
    durationMs: Math.round(performance.now() - startedAt),
    outputTruncated: shared.truncated,
    diagnostic,
  });

  activeRunId = null;
  isBusy = false;
};
