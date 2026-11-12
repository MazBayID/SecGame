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
  activeRunId = crypto.randomUUID();
  write("Starting a fresh Python worker…");
  setStatus("Loading local Pyodide runtime…");

  const thisRunId = activeRunId;
  const activeWorker = new Worker(new URL("./python-worker.js", import.meta.url), {
    type: "module",
  });
  pythonWorker = activeWorker;

  pythonTimer = setTimeout(() => {
    if (pythonWorker !== activeWorker || activeRunId !== thisRunId) return;
    finishPythonRun("Python worker initialization timed out and was terminated.");
    write("No result. Check that the local Pyodide core assets were prepared before building the debug APK.");
  }, 20000);

  activeWorker.onmessage = ({ data }) => {
    if (pythonWorker !== activeWorker || activeRunId !== thisRunId) return;

    if (data?.type === "ready" && pythonRunState === "initializing") {
      clearTimeout(pythonTimer);
      pythonRunState = "running";
      pythonTimer = setTimeout(() => {
        if (pythonWorker !== activeWorker || activeRunId !== thisRunId) return;
        finishPythonRun("Execution deadline reached · worker terminated.");
        write("TIMEOUT · The host terminated the worker after 3 seconds.");
      }, 3000);
      setStatus("Python " + data.version + " ready · Web Worker (not an OS-isolated process)");
      activeWorker.postMessage({
        type: "run",
        runId: thisRunId,
        source,
      });
      return;
    }

    if (data?.type === "result" && pythonRunState === "running" &&
        data.runId === thisRunId) {
      const validStatuses = new Set([
        "COMPLETED", "SYNTAX_ERROR", "RUNTIME_ERROR", "RESOURCE_LIMIT", "OUTPUT_LIMIT",
      ]);
      if (!validStatuses.has(data.status) ||
          typeof data.stdout !== "string" ||
          typeof data.stderr !== "string" ||
          typeof data.durationMs !== "number" ||
          !Number.isFinite(data.durationMs)) {
        finishPythonRun("Invalid worker response · worker discarded");
        write("WORKER_CRASHED · The runtime returned an invalid result.");
        return;
      }

      const details = [
        data.status,
        "Duration: " + data.durationMs + " ms",
        data.outputTruncated ? "Output truncated at 16 KiB." : "",
        "",
        "STDOUT",
        data.stdout || "(empty)",
        "",
        "STDERR",
        data.stderr || "(empty)",
        data.diagnostic ? "\nDIAGNOSTIC\n" + formatDiagnostic(data.diagnostic) : "",
      ].filter(Boolean).join("\n");
      finishPythonRun("Execution finished · worker discarded");
      write(details);
      return;
    }

    if (data?.type === "initialization-error" && pythonRunState === "initializing") {
      finishPythonRun("Python runtime initialization failed · worker discarded");
      write(data.message || "Unknown runtime initialization error.");
    }
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
