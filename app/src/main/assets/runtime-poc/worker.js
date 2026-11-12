const generation = new URL(self.location.href).searchParams.get("generation") || "unknown";

self.postMessage({ type: "ready", generation });

self.onmessage = ({ data }) => {
  if (!data || typeof data !== "object") return;

  if (data.type === "ping") {
    self.postMessage({ type: "pong", id: data.id, generation });
    return;
  }

  if (data.type === "hang") {
    self.postMessage({ type: "started", id: data.id, generation });
    // Deliberately synchronous and non-terminating: the host must stop this worker.
    while (true) {
      Math.sqrt(Math.random());
    }
  }
};
