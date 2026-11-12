# Physical Android Runtime POC Test Checklist

> Purpose: collect device evidence for Stage 2. This is a debug-only experiment, not a production Python sandbox approval.

## 1. Build the test APK

1. Open the GitHub repository `MazBayID/SecGame`.
2. Open **Actions** → **Android Build** → **Run workflow**.
3. Select branch `main`.
4. Enable `build_apk`.
5. Start the workflow and wait until all three jobs pass:
   - Unit tests and source validation
   - Real Pyodide runtime smoke test
   - Build debug APK
6. Download the `cyber-trace-debug` artifact and install the APK on the Android test device.

Do not use an APK from an older run as evidence for the current source.

## 2. Runtime POC: WebView + Pyodide

Open **CYBER//TRACE Runtime POC** from the Android app drawer.

### A. Startup and offline behavior

- [ ] Turn on Airplane Mode before first opening the POC.
- [ ] Open the POC.
- [ ] Confirm the page loads without a CDN or network connection.
- [ ] Confirm status reports Python 3.14.x and Pyodide 314.0.7.
- [ ] Confirm the startup capability probe reports arithmetic, JSON, datetime, and SQLite.
- [ ] If the screen says runtime blocked, record the exact message and stop; do not try to bypass the WebView multi-process gate.

### B. Real Python execution

Run each preset separately:

| Preset | Expected result |
|---|---|
| BASIC | `NEXUS connection ready` |
| BEGINNER | `42` |
| SYNTAX ERROR | `SYNTAX_ERROR` with a Python syntax diagnostic |
| RUNTIME ERROR | `RUNTIME_ERROR` for division by zero |
| TIMEOUT | `TIMEOUT`; host terminates the worker after about 3 seconds |
| OUTPUT LIMIT | `OUTPUT_LIMIT`; combined output remains capped at 16 KiB |
| MISSING PACKAGE | Import fails; it must not download a package |

A preset only fills the editor. Tap **RUN PYTHON** to execute it.

### C. Cancellation and lifecycle

- [ ] Start the TIMEOUT preset and press **CANCEL PYTHON** before the deadline.
- [ ] Confirm the UI remains responsive and cancellation is reported.
- [ ] Run BASIC again; a fresh worker should start and return the correct output.
- [ ] Use PING WORKER and confirm a response.
- [ ] Start NON-STOP TASK, then press TERMINATE WORKER. Confirm the page remains interactive.
- [ ] Press RECREATE WORKER and confirm it becomes ready.
- [ ] Repeat basic run/cancel/recreate several times and note any crashes, stuck controls, or unusual memory growth.

### D. Boundary checks (only the safe, non-destructive checks)

In the Python editor, run:

```python
from pathlib import Path

print(Path("/data/user/0/com.mazbayid.cybertrace").exists())
```

Expected: `False`. This is a limited virtual-filesystem probe, not a complete host-file isolation audit.

Then run:

```python
import js

names = (
    "self", "globalThis", "postMessage", "fetch", "Worker", "document",
    "window", "XMLHttpRequest", "WebSocket", "navigator", "location",
    "indexedDB", "localStorage", "process", "require", "Function",
    "eval", "Object", "Reflect", "Proxy",
)

print([name for name in names if hasattr(js, name)])
```

Expected: `[]`.

Do not run large memory-allocation experiments. Pyodide 314.0.7 has no verified per-instance heap cap in this POC. A memory exhaustion attempt can crash the renderer or the app.

## 3. Isolated-process POC

Open **CYBER//TRACE Isolated Process POC** and tap **TEST ISOLATED PROCESS**.

- [ ] Confirm the service UID differs from the app UID.
- [ ] Confirm packaged APK asset read reports PASS.
- [ ] Record both UIDs and the service PID.
- [ ] If either check fails, capture the displayed diagnostic.

This probe does not run Python or Chaquopy. It only verifies Android's isolated-service identity and access to a packaged APK asset.

## 4. Report the results

Please send the results in this format:

- Device model / Android version:
- Android System WebView version (if visible in system settings):
- APK workflow run ID:
- Runtime POC startup: PASS / FAIL
- Offline startup: PASS / FAIL
- BASIC / BEGINNER: PASS / FAIL
- Syntax / runtime errors: PASS / FAIL
- Timeout / cancellation: PASS / FAIL
- Output cap: PASS / FAIL
- Missing package fail-closed: PASS / FAIL
- Worker termination / recreation: PASS / FAIL
- App-private path probe: PASS / FAIL
- JavaScript namespace probe: PASS / FAIL
- Isolated-process UID / asset probe: PASS / FAIL
- Crashes, freezes, unexpected output, or other notes:

## 5. Safety and interpretation

- Do not enter personal files, credentials, tokens, or real targets.
- Do not test arbitrary network targets.
- Do not run memory exhaustion snippets.
- A passing checklist is evidence for the tested device and scenarios only. It is not a general security certification and does not by itself authorize connecting the runtime to the production lesson editor.
