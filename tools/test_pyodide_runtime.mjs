import assert from "node:assert/strict";
import { resolve, sep } from "node:path";
import { pathToFileURL } from "node:url";

const ROOT = resolve(new URL("..", import.meta.url).pathname);
const PYODIDE_DIR = resolve(
  ROOT,
  "app/src/debug/assets/runtime-poc/pyodide"
);
const indexURL = PYODIDE_DIR + sep;
const loaderURL = pathToFileURL(resolve(PYODIDE_DIR, "pyodide.mjs")).href;

const { loadPyodide } = await import(loaderURL);
const pyodide = await loadPyodide({
  indexURL,
  jsglobals: Object.freeze(Object.create(null)),
});

assert.equal(pyodide.version, "314.0.7", "Unexpected Pyodide release");

const namespaceProbe = pyodide.runPython(`
import js
import pyodide_js

for namespace_name, namespace in (("js", js), ("pyodide_js", pyodide_js)):
    for name in (
    "self", "globalThis", "postMessage", "fetch", "Worker", "document", "window",
    "XMLHttpRequest", "WebSocket", "importScripts", "close", "navigator",
    "location", "indexedDB", "localStorage", "caches", "crypto",
    "process", "require", "Deno", "Bun", "__dirname", "__filename",
    "Function", "eval", "Object", "Reflect", "Proxy",
):
        if hasattr(namespace, name):
            raise RuntimeError(
                namespace_name + " unexpectedly exposes JavaScript global: " + name
            )
"restricted-js-namespace"
`);
assert.equal(
  namespaceProbe,
  "restricted-js-namespace",
  "The player-facing Python JS namespace must not expose worker/browser globals"
);

const pythonVersion = pyodide.runPython(
  "import sys\nsys.version.split()[0]"
);
assert.match(pythonVersion, /^3\.14\.\d+$/, "Expected Python 3.14.x");

const startupProbe = pyodide.runPython(`
import datetime
import json
import math
import sqlite3
from pathlib import Path

assert math.isclose(6 * 7, 42)

payload = {"city": "NEXUS", "year": 2089}
assert json.loads(json.dumps(payload)) == payload

assert datetime.date(2089, 1, 1).isoformat() == "2089-01-01"

with sqlite3.connect(":memory:") as database:
    database.execute("CREATE TABLE runtime_probe (value INTEGER)")
    database.execute("INSERT INTO runtime_probe VALUES (42)")
    assert database.execute(
        "SELECT value FROM runtime_probe"
    ).fetchone() == (42,)

for host_path in (
    "/data/data/com.mazbayid.cybertrace",
    "/data/user/0/com.mazbayid.cybertrace",
    "/storage/emulated/0",
):
    assert not Path(host_path).exists(), f"Host path unexpectedly visible: {host_path}"
"startup-ok"
`);
assert.equal(startupProbe, "startup-ok");

let stdoutBytes = [];
let stderrBytes = [];
let outputBytes = 0;
let outputTruncated = false;
const MAX_OUTPUT_BYTES = 16 * 1024;

function resetOutputCapture() {
  stdoutBytes = [];
  stderrBytes = [];
  outputBytes = 0;
  outputTruncated = false;

  const capture = (target) => (byte) => {
    if (outputBytes < MAX_OUTPUT_BYTES) {
      target.push(byte);
      outputBytes += 1;
    } else {
      outputTruncated = true;
    }
  };

  pyodide.setStdout({ raw: capture(stdoutBytes) });
  pyodide.setStderr({ raw: capture(stderrBytes) });
}

function decode(bytes) {
  return new TextDecoder("utf-8").decode(new Uint8Array(bytes));
}

resetOutputCapture();
pyodide.runPython(`
events = [3, 7, 12]

def classify(value):
    if value >= 10:
        return "critical"
    if value >= 5:
        return "warning"
    return "normal"

summary = [classify(value) for value in events]
report = {"city": "NEXUS", "events": len(events), "status": summary[-1]}
print(f"{report['city']}:{report['events']}:{report['status']}")
`);
assert.equal(decode(stdoutBytes), "NEXUS:3:critical\n");

pyodide.runPython(`
from pathlib import Path

evidence = Path("/tmp/cybertrace-virtual-workspace.txt")
evidence.write_text("ECHO evidence", encoding="utf-8")
assert evidence.read_text(encoding="utf-8") == "ECHO evidence"
evidence.unlink()
`);

let unavailablePackageError = null;
try {
  pyodide.runPython("import numpy");
} catch (error) {
  unavailablePackageError = String(error);
}
assert.match(
  unavailablePackageError ?? "",
  /ModuleNotFoundError/,
  "An unbundled package should fail closed instead of being installed"
);

resetOutputCapture();
pyodide.runPython('print("NEXUS connection ready")');
assert.equal(decode(stdoutBytes), "NEXUS connection ready\n");
assert.equal(outputTruncated, false);

resetOutputCapture();
pyodide.runPython(`
import sys
print("stdout-check")
print("stderr-check", file=sys.stderr)
`);
assert.equal(decode(stdoutBytes), "stdout-check\n");
assert.equal(decode(stderrBytes), "stderr-check\n");

resetOutputCapture();
pyodide.runPython('print("X" * 20000)');
assert.equal(outputBytes, MAX_OUTPUT_BYTES);
assert.equal(outputTruncated, true);
assert.equal(decode(stdoutBytes).length, MAX_OUTPUT_BYTES);

let syntaxError = null;
try {
  pyodide.runPython('if True print("broken")');
} catch (error) {
  syntaxError = String(error);
}
assert.match(syntaxError ?? "", /SyntaxError/, "Syntax error was not surfaced");

let runtimeError = null;
try {
  pyodide.runPython('raise ValueError("runtime-probe")');
} catch (error) {
  runtimeError = String(error);
}
assert.match(runtimeError ?? "", /ValueError/, "Runtime error was not surfaced");
assert.match(runtimeError ?? "", /runtime-probe/);

console.log("PASS · Pyodide " + pyodide.version);
console.log("PASS · Python " + pythonVersion);
console.log("PASS · restricted Python JS namespace (no worker/browser globals)");
console.log("PASS · arithmetic, JSON, datetime, and SQLite");
console.log("PASS · app-private and shared-storage paths absent from virtual filesystem");
console.log("PASS · beginner variables, conditions, loops, functions, and collections");
console.log("PASS · in-memory virtual filesystem read/write");
console.log("PASS · missing packages fail closed");
console.log("PASS · stdout and stderr capture");
console.log("PASS · 16 KiB combined output cap");
console.log("PASS · syntax and runtime exceptions");
