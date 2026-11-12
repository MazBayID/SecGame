const statusNode = document.querySelector("#status");
const outputNode = document.querySelector("#output");
let worker = null;
let sequence = 0;

function write(message) {
  outputNode.textContent = message;
}

function setStatus(message) {
  statusNode.textContent = message;
}

function createWorker() {
  worker?.terminate();
  worker = new Worker(new URL("./worker.js", import.meta.url), { type: "module" });
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
const pythonButton = document.querySelector("#run-python");

function finishPythonRun(message) {
  if (pythonTimer !== null) clearTimeout(pythonTimer);
  pythonTimer = null;
  pythonWorker?.terminate();
  pythonWorker = null;
  pythonButton.disabled = false;
  setStatus(message);
}

pythonButton.addEventListener("click", () => {
  if (pythonWorker) return;

  pythonButton.disabled = true;
  write("Starting a fresh Python worker…");
  setStatus("Loading local Pyodide runtime…");

  pythonWorker = new Worker(new URL("./python-worker.js", import.meta.url), {
    type: "module",
  });

  pythonTimer = setTimeout(() => {
    finishPythonRun("Python worker initialization timed out and was terminated.");
    write("No result. Check that the local Pyodide core assets were prepared before building the debug APK.");
  }, 20000);

  pythonWorker.onmessage = ({ data }) => {
    if (data?.type === "ready") {
      clearTimeout(pythonTimer);
      pythonTimer = setTimeout(() => {
        finishPythonRun("Execution deadline reached · worker terminated.");
        write("TIMEOUT · The host terminated the worker after 3 seconds.");
      }, 3000);
      setStatus("Python " + data.version + " ready · isolated worker thread");
      pythonWorker.postMessage({
        type: "run",
        source: document.querySelector("#python-source").value,
      });
      return;
    }

    if (data?.type === "result") {
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
        data.diagnostic ? "\nDIAGNOSTIC\n" + data.diagnostic : "",
      ].filter(Boolean).join("\n");
      finishPythonRun("Execution finished · worker discarded");
      write(details);
      return;
    }

    if (data?.type === "error") {
      finishPythonRun("Python runtime error · worker discarded");
      write(data.message || "Unknown runtime initialization error.");
    }
  };

  pythonWorker.onerror = (event) => {
    finishPythonRun("Python worker failed · worker discarded");
    write("Worker failed to load. Run tools/prepare_pyodide_core.sh, then rebuild the debug APK.\\n" +
      (event.message || "No additional browser error details."));
  };
});
