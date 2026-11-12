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
  worker = new Worker(new URL("./worker.mjs", import.meta.url), { type: "module" });
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
