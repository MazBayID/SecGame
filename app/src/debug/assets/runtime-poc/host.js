const statusNode = document.querySelector("#status");
const outputNode = document.querySelector("#output");
let worker = null;
let sequence = 0;
let workerGeneration = 0;

function write(message) {
  outputNode.textContent = message;
}

function setStatus(message) {
  statusNode.textContent = message;
}

function createWorker() {
  worker?.terminate();
  const generation = ++workerGeneration;
  worker = new Worker(
    new URL("./worker.js?generation=" + generation, import.meta.url),
    { type: "module" }
  );
  worker.onmessage = ({ data }) => {
    if (data?.type === "ready") {
      setStatus("Worker ready · generation " + data.generation);
    } else if (data?.type === "pong") {
      write("PASS · Worker responded to ping.\nGeneration: " + data.generation);
    } else if (data?.type === "started") {
      write("Worker entered a synchronous non-stop task. The UI should remain responsive.");
    } else {
      write("Worker message: " + JSON.stringify(data));
    }
  };
  worker.onerror = (event) => {
    write("Worker error: " + (event.message || "unknown error"));
    setStatus("Worker error");
  };
}

createWorker();

document.querySelector("#ping").addEventListener("click", () => {
  if (!worker) createWorker();
  worker.postMessage({ type: "ping", id: ++sequence });
});

document.querySelector("#hang").addEventListener("click", () => {
  if (!worker) createWorker();
  worker.postMessage({ type: "hang", id: ++sequence });
});

document.querySelector("#stop").addEventListener("click", () => {
  worker?.terminate();
  worker = null;
  setStatus("Worker terminated by host");
  write("Termination requested. The page remains interactive.");
});

document.querySelector("#restart").addEventListener("click", () => {
  createWorker();
  write("A fresh worker was created. Wait for its ready message.");
});

const pythonSource = document.querySelector("#python-source");
const pythonPresets = {
  basic: 'print("NEXUS connection ready")',
  beginner: 'signal = 7\nprint(signal * 6)',
  syntax: 'if True print("broken")',
  runtime: 'print(1 / 0)',
  timeout: 'while True: pass',
  output: 'print("X" * 20000)',
  offline: 'import numpy',
};
document.querySelectorAll("[data-python-preset]").forEach((button) => {
  button.addEventListener("click", () => {
    const preset = button.dataset.pythonPreset;
    if (!Object.hasOwn(pythonPresets, preset)) return;
    pythonSource.value = pythonPresets[preset];
    write("Loaded preset: " + preset.toUpperCase() + ". Press RUN PYTHON to execute.");
  });
});

let pythonWorker = null;
let pythonTimer = null;
let pythonRunState = "idle";
let activeRunId = null;
const pythonButton = document.querySelector("#run-python");
const cancelPythonButton = document.querySelector("#cancel-python");

function finishPythonRun(message) {
  if (pythonTimer !== null) clearTimeout(pythonTimer);
  pythonTimer = null;

  const finishedWorker = pythonWorker;
  pythonWorker = null;
  activeRunId = null;
  pythonRunState = "idle";
  finishedWorker?.terminate();

  pythonButton.disabled = false;
  cancelPythonButton.disabled = true;
  setStatus(message);
}

function formatDiagnostic(diagnostic) {
  if (!diagnostic || typeof diagnostic !== "object") return "";
  const parts = [];
  if (diagnostic.exceptionType) parts.push(diagnostic.exceptionType);
  if (Number.isInteger(diagnostic.line) && diagnostic.line > 0) {
    parts.push("line " + diagnostic.line);
  }
  if (diagnostic.message) parts.push(diagnostic.message);
  return parts.join(" · ");
}

function capCombinedOutput(stdout, stderr) {
  let remaining = 16 * 1024;
  let truncated = false;

  function take(value) {
    const characters = [];
    for (const character of value) {
      const codePoint = character.codePointAt(0);
      const byteLength = codePoint <= 0x7F ? 1
        : codePoint <= 0x7FF ? 2
        : codePoint <= 0xFFFF ? 3
        : 4;
      if (byteLength > remaining) {
        truncated = true;
        break;
      }
      characters.push(character);
      remaining -= byteLength;
    }
    return characters.join("");
  }

  return {
    stdout: take(stdout),
    stderr: take(stderr),
    truncated,
  };
}

cancelPythonButton.addEventListener("click", () => {
  if (!pythonWorker) return;
  finishPythonRun("Execution cancelled · worker terminated");
  write("CANCELLED · The host terminated the Python worker.");
});

pythonButton.addEventListener("click", () => {
  if (pythonWorker || pythonRunState !== "idle") return;

  const source = document.querySelector("#python-source").value;
  if (new TextEncoder().encode(source).byteLength > 16 * 1024) {
    write("RESOURCE_LIMIT · Source exceeds 16 KiB.");
    setStatus("Source rejected before worker startup");
    return;
  }

  pythonButton.disabled = true;
  cancelPythonButton.disabled = false;
  pythonRunState = "initializing";
  write("Starting a fresh Python worker…");
  setStatus("Loading local Pyodide runtime…");

  let thisRunId;
  let activeWorker;
  try {
    thisRunId = crypto.randomUUID();
    activeRunId = thisRunId;
    activeWorker = new Worker(new URL("./python-worker.js", import.meta.url), {
      type: "module",
    });
    pythonWorker = activeWorker;
  } catch (error) {
    finishPythonRun("Python worker could not start · worker discarded");
    write("WORKER_CRASHED · " +
      String(error?.message || error).slice(0, 1024));
    return;
  }

  pythonTimer = setTimeout(() => {
    if (pythonWorker !== activeWorker || activeRunId !== thisRunId) return;
    finishPythonRun("Python worker initialization timed out and was terminated.");
    write("No result. Check that the local Pyodide core assets were prepared before building the debug APK.");
  }, 20000);

  activeWorker.onmessage = ({ data }) => {
    if (pythonWorker !== activeWorker || activeRunId !== thisRunId) return;

    const expectedCapabilities = new Set([
      "arithmetic", "json", "datetime", "sqlite3",
    ]);
    const capabilitiesValid = Array.isArray(data?.capabilities) &&
      data.capabilities.length === expectedCapabilities.size &&
      new Set(data.capabilities).size === expectedCapabilities.size &&
      data.capabilities.every((item) => expectedCapabilities.has(item));

    if (data?.type === "ready" && pythonRunState === "initializing" &&
        data.version === "314.0.7" &&
        typeof data.pythonVersion === "string" &&
        /^3\.14\.\d+$/.test(data.pythonVersion) &&
        capabilitiesValid) {
      clearTimeout(pythonTimer);
      pythonRunState = "running";
      pythonTimer = setTimeout(() => {
        if (pythonWorker !== activeWorker || activeRunId !== thisRunId) return;
        finishPythonRun("Execution deadline reached · worker terminated.");
        write("TIMEOUT · The host terminated the worker after 3 seconds.");
      }, 3000);
      setStatus(
        "Python " + data.pythonVersion + " · Pyodide " + data.version +
        " · arithmetic / JSON / datetime / SQLite passed · sandboxed WebView renderer"
      );
      try {
        activeWorker.postMessage({
          type: "run",
          runId: thisRunId,
          source,
        });
      } catch (error) {
        finishPythonRun("Python request could not be sent · worker discarded");
        write("WORKER_CRASHED · " +
          String(error?.message || error).slice(0, 1024));
      }
      return;
    }

    if (data?.type === "result" && pythonRunState === "running" &&
        data.runId === thisRunId) {
      const validStatuses = new Set([
        "COMPLETED", "SYNTAX_ERROR", "RUNTIME_ERROR", "OUTPUT_LIMIT", "RESOURCE_LIMIT",
      ]);
      const diagnosticKinds = new Set(["SYNTAX", "RUNTIME", "RESOURCE"]);
      const diagnosticValid = data.diagnostic === null ||
        (data.diagnostic && typeof data.diagnostic === "object" &&
          !Array.isArray(data.diagnostic) &&
          diagnosticKinds.has(data.diagnostic.kind) &&
          typeof data.diagnostic.exceptionType === "string" &&
          data.diagnostic.exceptionType.length <= 128 &&
          typeof data.diagnostic.message === "string" &&
          data.diagnostic.message.length <= 1024 &&
          new TextEncoder().encode(data.diagnostic.message).byteLength <= 4096 &&
          (data.diagnostic.line === null ||
            (Number.isInteger(data.diagnostic.line) && data.diagnostic.line > 0)));
      if (!validStatuses.has(data.status) ||
          typeof data.stdout !== "string" ||
          typeof data.stderr !== "string" ||
          data.stdout.length > 16 * 1024 ||
          data.stderr.length > 16 * 1024 ||
          typeof data.durationMs !== "number" ||
          !Number.isFinite(data.durationMs) ||
          data.durationMs < 0 ||
          typeof data.outputTruncated !== "boolean" ||
          !diagnosticValid) {
        finishPythonRun("Invalid worker response · worker discarded");
        write("WORKER_CRASHED · The runtime returned an invalid or oversized result.");
        return;
      }

      const boundedOutput = capCombinedOutput(data.stdout, data.stderr);
      const outputTruncated = data.outputTruncated ||
        boundedOutput.truncated || data.status === "OUTPUT_LIMIT";
      const displayStatus = boundedOutput.truncated && data.status === "COMPLETED"
        ? "OUTPUT_LIMIT"
        : data.status;
      const details = [
        displayStatus,
        "Duration: " + data.durationMs + " ms",
        outputTruncated ? "Output truncated at 16 KiB." : "",
        "",
        "STDOUT",
        boundedOutput.stdout || "(empty)",
        "",
        "STDERR",
        boundedOutput.stderr || "(empty)",
        data.diagnostic ? "\nDIAGNOSTIC\n" + formatDiagnostic(data.diagnostic) : "",
      ].filter(Boolean).join("\n");
      finishPythonRun("Execution finished · worker discarded");
      write(details);
      return;
    }

    if (data?.type === "initialization-error" && pythonRunState === "initializing" &&
        typeof data.message === "string" && data.message.length <= 4096) {
      finishPythonRun("Python runtime initialization failed · worker discarded");
      write(data.message || "Unknown runtime initialization error.");
      return;
    }

    finishPythonRun("Unexpected worker message · worker discarded");
    write("WORKER_CRASHED · The worker sent an unexpected or out-of-order message.");
  };

  activeWorker.onerror = (event) => {
    if (pythonWorker !== activeWorker || activeRunId !== thisRunId) return;
    finishPythonRun("Python worker failed · worker discarded");
    write("Worker failed to load. Run tools/prepare_pyodide_core.sh, then rebuild the debug APK.\n" +
      (event.message || "No additional browser error details."));
  };

  activeWorker.onmessageerror = () => {
    if (pythonWorker !== activeWorker || activeRunId !== thisRunId) return;
    finishPythonRun("Python worker message could not be decoded · worker discarded");
    write("WORKER_CRASHED · The host could not decode the worker response.");
  };
});
