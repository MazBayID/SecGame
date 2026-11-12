import assert from "node:assert/strict";
import { resolve, sep } from "node:path";
import { pathToFileURL } from "node:url";

const ROOT = resolve(new URL("..", import.meta.url).pathname);
const PYODIDE_DIR = resolve(
  ROOT,
  "app/src/debug/assets/runtime-poc/pyodide"
);
const indexURL = pathToFileURL(PYODIDE_DIR + sep).href;
const loaderURL = pathToFileURL(resolve(PYODIDE_DIR, "pyodide.mjs")).href;

const { loadPyodide } = await import(loaderURL);
const pyodide = await loadPyodide({ indexURL });

assert.equal(pyodide.version, "314.0.7", "Unexpected Pyodide release");

const pythonVersion = pyodide.runPython(
  "import sys\nsys.version.split()[0]"
);
assert.match(pythonVersion, /^3\.14\.\d+$/, "Expected Python 3.14.x");

const startupProbe = pyodide.runPython(`
import datetime
import json
import math
import sqlite3

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
console.log("PASS · arithmetic, JSON, datetime, and SQLite");
console.log("PASS · stdout and stderr capture");
console.log("PASS · 16 KiB combined output cap");
console.log("PASS · syntax and runtime exceptions");
