#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MAIN_MANIFEST="${ROOT}/app/src/main/AndroidManifest.xml"
DEBUG_MANIFEST="${ROOT}/app/src/debug/AndroidManifest.xml"
ACTIVITY="${ROOT}/app/src/debug/java/com/mazbayid/cybertrace/execution/poc/RuntimePocActivity.kt"
INDEX="${ROOT}/app/src/debug/assets/runtime-poc/index.html"
HOST="${ROOT}/app/src/debug/assets/runtime-poc/host.js"
PYTHON_WORKER="${ROOT}/app/src/debug/assets/runtime-poc/python-worker.js"
ISOLATED_SERVICE="${ROOT}/app/src/debug/java/com/mazbayid/cybertrace/execution/poc/IsolatedPythonServiceProbe.kt"
ISOLATED_ACTIVITY="${ROOT}/app/src/debug/java/com/mazbayid/cybertrace/execution/poc/IsolatedProcessPocActivity.kt"

require_text() {
  local needle="$1"
  local file="$2"
  if ! grep -Fq -- "$needle" "$file"; then
    echo "Required runtime POC invariant missing: $needle ($file)" >&2
    exit 1
  fi
}

reject_text() {
  local needle="$1"
  local file="$2"
  if grep -Fq -- "$needle" "$file"; then
    echo "Forbidden runtime POC configuration found: $needle ($file)" >&2
    exit 1
  fi
}

# Player code must never run in the app process when WebView multiprocess
# support is absent, and the app must not grant direct network access.
require_text "WebViewFeature.MULTI_PROCESS" "$ACTIVITY"
require_text "WebViewCompat.isMultiProcessEnabled()" "$ACTIVITY"
reject_text "android.permission.INTERNET" "$MAIN_MANIFEST"
reject_text "android.permission.INTERNET" "$DEBUG_MANIFEST"
require_text 'android:isolatedProcess="true"' "$DEBUG_MANIFEST"
require_text "Process.myUid()" "$ISOLATED_SERVICE"
require_text "applicationInfo.uid" "$ISOLATED_ACTIVITY"
require_text "serviceUid != appUid" "$ISOLATED_ACTIVITY"

# Keep the WebView boundary narrow: no Java bridge, host file access, or
# JavaScript eval escape in the document/worker CSP.
reject_text "addJavascriptInterface" "$ACTIVITY"
require_text "settings.allowFileAccess = false" "$ACTIVITY"
require_text "settings.allowContentAccess = false" "$ACTIVITY"
require_text "worker-src 'none'" "$ACTIVITY"
require_text "wasm-unsafe-eval" "$ACTIVITY"
require_text "wasm-unsafe-eval" "$INDEX"
if grep -Eq "['\"]unsafe-eval['\"]" "$ACTIVITY" "$INDEX"; then
  echo "Forbidden CSP directive unsafe-eval found." >&2
  exit 1
fi

# Keep Python's JS namespace empty and require the constructor-escape probe.
require_text "jsglobals: Object.freeze(Object.create(null))" "$PYTHON_WORKER"
require_text "pyodide_js.runPython.constructor" "$PYTHON_WORKER"
require_text "import sqlite3" "$PYTHON_WORKER"
require_text "CREATE TABLE runtime_probe" "$PYTHON_WORKER"
require_text 'pythonVersion' "$PYTHON_WORKER"
require_text '"sqlite3"' "$PYTHON_WORKER"
require_text 'data.pythonVersion' "$HOST"
require_text "finishedWorker?.terminate()" "$HOST"
require_text "crypto.randomUUID()" "$HOST"
require_text "sandboxed WebView renderer" "$HOST"
require_text '"OUTPUT_LIMIT"' "$HOST"
require_text "data.stdout.length > 16 * 1024" "$HOST"
require_text "function capCombinedOutput" "$HOST"
require_text "Python worker could not start" "$HOST"
require_text "Python request could not be sent" "$HOST"
require_text "const diagnosticValid" "$HOST"
require_text "const MAX_DIAGNOSTIC_CHARS = 1024" "$PYTHON_WORKER"
require_text ".slice(0, 128)" "$PYTHON_WORKER"
require_text "unexpected or out-of-order message" "$HOST"
reject_text "loadPackagesFromImports" "$PYTHON_WORKER"
reject_text "micropip.install" "$PYTHON_WORKER"

echo "Runtime POC security invariants passed."
