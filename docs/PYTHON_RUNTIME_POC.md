# Python Runtime Proof of Concept

> Status: disposable debug-only Pyodide POC implemented; production lesson runtime is not integrated or approved.
>
> Scope: CYBER//TRACE in `MazBayID/SecGame` only. The separate `firstGame` project is out of scope and must not be inspected or used.

## Objective

Prove that a real Python interpreter can run beginner exercises on Android without executing player code in the main application process or exposing host resources. A successful `print("hello")` is necessary but not sufficient.

The POC is disposable: its code and assets must not become the production runtime until every mandatory gate is measured and reviewed.

## Candidate order

### Candidate A — Pyodide in a Web Worker hosted by Android WebView

This is the first implementation experiment because a dedicated worker can keep synchronous Python execution away from the UI thread and can be terminated as a whole when a deadline expires. The official Pyodide worker guide requires a module-type worker. Release 314.0.7 publishes a 337,536,678-byte full archive and a 6,757,104-byte core archive. The core archive contains the loader, WASM binary, Python standard library, and lockfile, but is primarily intended for Node and may automatically fetch missing packages from a CDN. It is only a viable offline starting point if the app never invokes automatic package discovery/loading and every additional wheel is bundled and served locally. The POC must explicitly fail closed when a module is not present locally.

Proposed shape:

1. Bundle a pinned Pyodide distribution inside the APK. No runtime download or CDN dependency at execution time.
2. Load a local POC page through AndroidX `WebViewAssetLoader` using its app-assets HTTPS origin, not `file://`.
3. Create a module-type dedicated Web Worker. Initialize Pyodide only inside that worker.
4. Send a small, versioned JSON message containing an execution ID, source, workspace ID, and validated limits.
5. Capture stdout/stderr inside Python using a bounded writer. Return only structured results and capped output.
6. On timeout or cancellation, call `Worker.terminate()`, discard the worker instance, and create a fresh worker for the next run. Do not depend on cooperative Python cancellation to stop an infinite loop. Pyodide's cooperative interrupt mechanism requires a Web Worker plus `SharedArrayBuffer` and the associated cross-origin isolation headers; the initial POC should test hard worker termination first so it does not depend on those headers.
7. Keep the Pyodide virtual filesystem in memory and seed it only with mission-owned files.
8. Do not expose `addJavascriptInterface`, privileged JavaScript bridges, Android objects, host paths, app secrets, or arbitrary host callbacks to the page or worker.
9. Do not grant direct network access to the runtime. Networking lessons use explicit game-owned simulation messages, never real sockets.

Web Worker termination is a hard stop for that worker, but it is not proof of memory isolation or a separate OS process. Verify WebView renderer behavior and recovery on real Android devices. The host-side `Worker.terminate()` operation is specified to stop the worker immediately; test that the WebView releases the worker's WASM memory sufficiently after repeated termination and recreation. A worker shares the WebView renderer's security context; this POC must not claim stronger isolation than the platform actually provides.

### Candidate B — CPython in an Android isolated-process service

Keep this as the comparison path if Pyodide cannot satisfy Python compatibility or memory requirements.

- Declare the service with `android:isolatedProcess="true"`; a normal secondary process is not an equivalent security boundary.
- Verify whether the selected CPython distribution can initialize in this process and load its standard library without granting access to app-private files.
- Use Binder messages containing only bounded source and game-owned identifiers.
- Do not pass file descriptors, host paths, app context, credentials, or unrestricted network capabilities to player code.
- Verify cancellation, service death/restart, output caps, and memory behavior.
- Chaquopy compatibility must be demonstrated in this exact process model; support for Android by itself is not proof that it works in an isolated service.

## WebView hardening checklist

- [ ] Pin the initial candidate to Pyodide v314.0.7. The official GitHub release API reports the core archive as 6,757,104 bytes with SHA-256 `2abdcc2e35208af406e07724cffa85bc582ced97e9028383ecf5462541393f95`; verify this checksum after downloading before extracting it. The full archive is 337,536,678 bytes (SHA-256 `192b5864e6e6d30ab074861af800cb8b4acb0998ef0f9342c3367448aeb86645`).
- [ ] Use a pinned, APK-bundled runtime and local assets only.
- [ ] Measure core distribution and selected offline wheels before choosing whether they fit the APK budget; the full distribution is documented as 200+ MB.
- [ ] Use `WebViewAssetLoader`; do not load the POC from `file://`.
- [ ] Enable JavaScript only because Pyodide requires it.
- [ ] Do not register JavaScript interfaces or expose Kotlin/Java objects.
- [ ] Disable file access and content access.
- [ ] Disable file-URL cross-access and mixed content.
- [ ] Reject navigation away from the exact app-assets origin.
- [ ] Apply a restrictive Content Security Policy compatible with the pinned Pyodide/WASM build as a response header on both document and worker assets; test the exact policy rather than weakening it broadly.
- [ ] Do not grant the app `INTERNET` permission for the POC.
- [ ] Do not load remote scripts, packages, Python wheels, or source code at runtime.
- [ ] Ensure worker messages are schema-validated and size-limited on both sides.
- [ ] Confirm that the worker does not fetch missing packages from a CDN; do not call `loadPackagesFromImports` or `micropip.install` in the offline runtime. Every required module must already be bundled or unavailable.
- [ ] Configure Pyodide `jsglobals` as an empty immutable object instead of default `globalThis`; verify the Python `js` module cannot access worker/browser globals, then fail closed if the probe detects any exposed name. See the [Pyodide 314.0.7 JavaScript API](https://pyodide.org/en/314.0.7/usage/api/js-api.html#pyodideconfig-jsglobals).
- [ ] Keep `worker-src 'none'` in worker-script response CSP so player code cannot create nested workers.
- [ ] Treat the worker's WebAssembly heap as unbounded for this pinned release until a tested memory cap or a separate enforceable process boundary is demonstrated.

## Required test matrix

| Test | Procedure | Pass condition |
|---|---|---|
| Real interpreter | Run arithmetic, variables, `print`, conditionals, loops, functions, and standard-library examples | Output matches CPython expectations for supported language features |
| Syntax diagnostic | Submit malformed Python | Structured syntax error with useful line information |
| Runtime diagnostic | Submit a deliberate exception | Structured runtime error; app remains responsive |
| Infinite loop | Run `while True: pass` | Host terminates worker within the configured deadline |
| Cancellation | Cancel a running execution | Worker is stopped; no late result is accepted |
| Output flood | Repeatedly print beyond the cap | Output stays within byte cap and UI remains responsive |
| Memory pressure | Only after preparing a recovery path, allocate progressively larger objects in controlled increments | Record peak renderer memory, failure mode, app survival, and ability to recreate a fresh worker; no memory cap is claimed by this POC |
| Host filesystem | Attempt common absolute paths and app-private locations | No host file contents are exposed or modified |
| Environment | Inspect environment variables and runtime globals | No app secrets, tokens, or host configuration are exposed |
| Network | Attempt DNS, HTTP, and socket access | No direct external network access |
| Virtual files | Read/write only mission-provided virtual paths | No access outside the in-memory game workspace |
| JS/Android bridge | Run `import js; print([name for name in ('self', 'globalThis', 'postMessage', 'fetch', 'Worker', 'document', 'window') if hasattr(js, name)])` | Empty list; startup probe must fail closed if any worker/browser global is exposed |
| Repeatability | Run, terminate, recreate, and run again repeatedly | Fresh execution state; no previous globals or output leak |
| Renderer failure | Force worker/renderer failure where test tooling allows | Main app survives and can recreate the execution environment |
| Offline | Disable network before first launch | Runtime initializes and executes without downloading dependencies |
| Device budget | Measure APK size, cold start, warm start, peak memory, and CPU on target devices | Meets documented project budget without freezing the UI |

## Proposed initial limits

These are experiment values, not a claim of enforced limits:

- Source: 16 KiB.
- Wall-clock deadline: 3 seconds for beginner exercises.
- Captured stdout plus stderr: 16 KiB.
- One active execution per lesson workspace.
- Fresh worker after cancellation, timeout, or abnormal termination.

The current Kotlin contract also carries a memory limit field. The POC must prove an enforceable memory boundary before that field can be treated as implemented. A timeout and output cap do not constitute a memory cap.

## Decision gate

Select Pyodide only if its offline package, supported Python features, WebView behavior, hard-stop behavior, memory recovery, and host-access tests pass. Select the isolated-service CPython route only if runtime initialization and every isolation test pass inside the actual isolated service.

If neither candidate passes, do not enable Run. Rework the architecture or narrow the runtime capabilities while preserving the core requirement: genuine Python execution with honest, understandable limits.

## Worker lifecycle harness (implemented in debug source set)

A disposable WebView harness now lives under `app/src/debug/assets/runtime-poc/` and is launched by the debug-only `RuntimePocActivity`. The debug APK exposes it as a separate launcher entry named CYBER//TRACE Runtime POC, so it can be opened directly from the Android app drawer without ADB. It loads through `WebViewAssetLoader` at the app-assets HTTPS origin, uses a module worker, exposes no JavaScript-to-Android bridge, disables file/content access, rejects navigation outside the app-assets origin, and applies a restrictive CSP with `connect-src 'self'` (local app-assets only). It has controls to ping the worker, start a deliberately non-terminating synchronous task, terminate the worker from the host, and create a fresh worker. The Python panel has a separate cancel button for host-initiated cancellation. The harness includes a separate real-Python test panel. The pinned Pyodide core archive is downloaded and checksum-verified by `tools/prepare_pyodide_core.sh`, then extracted into ignored debug assets; the manual APK workflow runs this script before packaging. For a local build, run the script before `gradle --no-daemon assembleDebug`. The Python panel creates a fresh module worker, initializes Pyodide from the local app-assets origin, rejects source larger than 16 KiB, captures combined stdout/stderr up to 16 KiB, and uses a 3-second host timer to terminate the worker. Initialization has a separate 20-second ceiling. The host uses a per-run random correlation ID, a single-run state machine, worker-instance checks, and response-shape validation so stale or unrelated messages are ignored. This is protocol hardening, not a security boundary against arbitrary code inside the same worker. Pyodide is initialized with an empty immutable `jsglobals` object rather than its default `globalThis`, so Python's built-in `js` module is not intended to expose `self`, `postMessage`, `fetch`, `Worker`, or browser globals. Startup runs a fail-closed probe and refuses execution if any of those names are visible. Worker response CSP additionally sets `worker-src 'none'` to prevent player code from creating nested workers. These are defense-in-depth controls that still require on-device verification; they do not make WebView an OS-isolated process. The worker returns a bounded diagnostic object with exception type and a best-effort line number. The worker intentionally does not call `loadPackagesFromImports` or `micropip.install`; only the standard library included in the core archive is available, and missing packages must fail rather than download from a CDN. The harness is debug-only and is not connected to the production lesson flow. Open the separate CYBER//TRACE Runtime POC icon from the Android app drawer after installing a debug APK. Alternatively, launch it on a connected device with `adb shell am start -n com.mazbayid.cybertrace/.execution.poc.RuntimePocActivity`.

This harness uses AndroidX WebKit 1.18.0 and is an early platform experiment, not a sandbox certification. The WebView asset handler applies a restrictive CSP as an HTTP response header to every asset, including worker scripts (the HTML meta policy is retained as defense in depth); it also sets X-Content-Type-Options: nosniff and serves .wasm as application/wasm. The policy allows script/WASM execution and same-origin app-assets requests only. The app manifest does not request INTERNET. It still requires physical-device testing for module-worker support, offline Python initialization, hard-stop responsiveness, worker recreation, renderer recovery, and memory release. The 3-second deadline and output cap are host-enforced for this POC; there is no enforceable memory cap yet. A worker shares the WebView renderer security context and must not be described as an Android isolated process.

## Manual smoke-test snippets

Run each snippet separately in the debug Python panel:

| Purpose | Python source | Expected behavior |
|---|---|---|
| Basic execution | `print("NEXUS connection ready")` | Prints the exact message |
| Variables and arithmetic | `signal = 7\nprint(signal * 6)` | Prints `42` |
| Syntax diagnostic | `if True print("broken")` | Returns a syntax error |
| Runtime diagnostic | `print(1 / 0)` | Returns a runtime error |
| Host timeout | `while True: pass` | Host terminates the worker at the 3-second deadline |
| Output cap | `print("X" * 20000)` | Captured output remains capped at 16 KiB and is marked truncated |
| Offline package behavior | `import numpy` | Fails if the wheel is not locally bundled; must not attempt a CDN download |

The smoke tests validate basic runtime behavior only. They do not replace the host-access, memory-pressure, renderer-recovery, or physical-device tests in the full matrix.

## Pyodide artifact facts

The official `pyodide/pyodide` 314.0.7 GitHub release exposes these archives:

| Archive | Compressed size | SHA-256 |
|---|---:|---|
| `pyodide-core-314.0.7.tar.bz2` | 6,757,104 bytes | `2abdcc2e35208af406e07724cffa85bc582ced97e9028383ecf5462541393f95` |
| `pyodide-314.0.7.tar.bz2` | 337,536,678 bytes | `192b5864e6e6d30ab074861af800cb8b4acb0998ef0f9342c3367448aeb86645` |

The core archive is much smaller but does not contain the vendored wheel collection. Upstream documents it as primarily intended for Node, where missing packages may be automatically installed from the CDN. CYBER//TRACE must not inherit that network behavior. The POC worker deliberately avoids package discovery/install APIs, and the WebView CSP allows connections only to the app-assets origin. The app manifest has no INTERNET permission. Missing packages should therefore fail rather than silently download. The debug build preparation script verifies the archive checksum before extracting the core files; verify local startup and missing-package behavior on-device before relying on this configuration.

## Current POC boundaries

- The worker executes real Python through Pyodide 314.0.7 when debug assets are prepared; it is not a fake parser.
- The Python panel is isolated from the main Compose lesson screen and is debug-only.
- Each run uses a new worker, and the host terminates it after completion, error, or timeout.
- The current POC does not enforce a memory cap, does not provide real networking, and does not implement the game virtual filesystem. Pyodide 314.0.7 does not expose the later maxMemoryBytes loader option: that option appears in the 314.1.0 development changelog, not the pinned 314.0.7 release. Do not silently upgrade to a development build to claim memory enforcement. A Web Worker timeout and output cap do not prevent a memory-exhaustion attempt; renderer survival and recovery must be measured on target Android devices. The Python js module also exposes the worker's JavaScript global environment, so the POC must test that no Android bridge, host application object, secret, or privileged callback is reachable. The correlation ID is not a substitute for a security boundary.
- Do not connect this worker to `PythonExecutionEngine` or enable the lesson Run button until the complete security and compatibility matrix passes.

## Implementation order after POC

1. Test the existing Pyodide worker on physical Android hardware, including offline startup and low-memory behavior.
2. Add tests for memory pressure, nested worker attempts, malformed messages, and repeated worker recreation.
3. Decide whether WebView worker termination and renderer behavior meet the project's isolation requirements; compare against CPython in an Android isolated-process service if not.
4. Implement and enforce a real memory/resource boundary or document why the candidate cannot safely meet it.
5. Implement the production adapter for `PythonExecutionEngine` only after the security decision gate passes.
6. Connect the editor Run/Stop controls only after the production adapter and security gate pass.
7. Build the first mission around concepts already taught in Lesson 00.01.


## Source notes

- [Pyodide v314.0.7 — Web Worker guide](https://pyodide.org/en/314.0.7/usage/webworker.html): Pyodide uses a module-type worker and documents message-based execution outside the UI thread.
- [Pyodide v314.0.7 — downloading and deploying](https://pyodide.org/en/314.0.7/usage/downloading-and-deploying.html): the full distribution is documented as 200+ MB; the core archive contains the runtime loader, WASM binary, Python standard library, and lockfile.
- [Pyodide — interrupting execution](https://pyodide.org/en/stable/usage/keyboard-interrupts.html): cooperative interruption requires a Web Worker, SharedArrayBuffer, and appropriate cross-origin isolation headers.
- [MDN — Worker.terminate()](https://developer.mozilla.org/en-US/docs/Web/API/Worker/terminate): termination immediately stops a worker without waiting for its code to finish.
- [Android — security tips](https://developer.android.com/privacy-and-security/security-tips): Android recommends minimizing permissions and protecting app-private data and IPC boundaries.


## Memory-limit version note

The pinned Pyodide 314.0.7 changelog does not list a maxMemoryBytes option. The option appears in the 314.1.0 development changelog and is documented as a WebAssembly heap-growth cap, but that development build is not the pinned release and must not be substituted without a separate compatibility, integrity, and offline validation pass. References: [314.0.7 release changelog](https://pyodide.org/en/314.0.7/project/changelog.html) and [current development changelog](https://pyodide.org/en/latest/project/changelog.html).
