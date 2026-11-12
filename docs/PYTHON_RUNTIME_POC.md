# Python Runtime Proof of Concept

> Status: design and test plan. No runtime is integrated or approved yet.
>
> Scope: CYBER//TRACE in `MazBayID/SecGame` only. The separate `firstGame` project is out of scope and must not be inspected or used.

## Objective

Prove that a real Python interpreter can run beginner exercises on Android without executing player code in the main application process or exposing host resources. A successful `print("hello")` is necessary but not sufficient.

The POC is disposable: its code and assets must not become the production runtime until every mandatory gate is measured and reviewed.

## Candidate order

### Candidate A — Pyodide in a Web Worker hosted by Android WebView

This is the first implementation experiment because a dedicated worker can keep synchronous Python execution away from the UI thread and can be terminated as a whole when a deadline expires. The official Pyodide worker guide requires a module-type worker. The current official download guide lists the v314.0.7 release and says the full distribution is over 200 MB; therefore, the POC must measure the smaller core distribution plus only the offline packages the curriculum actually needs, rather than bundling the full distribution by default.

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

- [ ] Pin the initial candidate to the documented Pyodide v314.0.7 release for reproducibility; record the exact archive checksum in the POC results.
- [ ] Use a pinned, APK-bundled runtime and local assets only.
- [ ] Measure core distribution and selected offline wheels before choosing whether they fit the APK budget; the full distribution is documented as 200+ MB.
- [ ] Use `WebViewAssetLoader`; do not load the POC from `file://`.
- [ ] Enable JavaScript only because Pyodide requires it.
- [ ] Do not register JavaScript interfaces or expose Kotlin/Java objects.
- [ ] Disable file access and content access.
- [ ] Disable file-URL cross-access and mixed content.
- [ ] Reject navigation away from the exact app-assets origin.
- [ ] Apply a restrictive Content Security Policy compatible with the pinned Pyodide/WASM build; test the exact policy rather than weakening it broadly.
- [ ] Do not grant the app `INTERNET` permission for the POC.
- [ ] Do not load remote scripts, packages, Python wheels, or source code at runtime.
- [ ] Ensure worker messages are schema-validated and size-limited on both sides.
- [ ] Confirm that the worker does not fetch missing packages from a CDN; every required module must already be bundled or unavailable.

## Required test matrix

| Test | Procedure | Pass condition |
|---|---|---|
| Real interpreter | Run arithmetic, variables, `print`, conditionals, loops, functions, and standard-library examples | Output matches CPython expectations for supported language features |
| Syntax diagnostic | Submit malformed Python | Structured syntax error with useful line information |
| Runtime diagnostic | Submit a deliberate exception | Structured runtime error; app remains responsive |
| Infinite loop | Run `while True: pass` | Host terminates worker within the configured deadline |
| Cancellation | Cancel a running execution | Worker is stopped; no late result is accepted |
| Output flood | Repeatedly print beyond the cap | Output stays within byte cap and UI remains responsive |
| Memory pressure | Allocate progressively larger objects | Worker is stopped or fails safely; app survives and recovers |
| Host filesystem | Attempt common absolute paths and app-private locations | No host file contents are exposed or modified |
| Environment | Inspect environment variables and runtime globals | No app secrets, tokens, or host configuration are exposed |
| Network | Attempt DNS, HTTP, and socket access | No direct external network access |
| Virtual files | Read/write only mission-provided virtual paths | No access outside the in-memory game workspace |
| JS/Android bridge | Inspect worker globals and attempt bridge access | No privileged Android bridge or host object exists |
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

A disposable WebView harness now lives under `app/src/main/assets/runtime-poc/` and is launched by the debug-only `RuntimePocActivity`. It loads through `WebViewAssetLoader` at the app-assets HTTPS origin, uses a module worker, exposes no JavaScript-to-Android bridge, disables file/content access, rejects navigation outside the app-assets origin, and applies a restrictive CSP with `connect-src 'none'`. It has controls to ping the worker, start a deliberately non-terminating synchronous task, terminate the worker from the host, and create a fresh worker. The harness deliberately contains no Python interpreter and does not execute player code; it validates only the WebView worker lifecycle. It is debug-only and is not part of the production lesson flow. Launch it on a connected device with `adb shell am start -n com.mazbayid.cybertrace/.execution.poc.RuntimePocActivity` after installing a debug APK.

This harness is an early platform experiment, not a sandbox certification. It still requires physical-device testing for module-worker support, hard-stop responsiveness, worker recreation, renderer recovery, and memory release. The next runtime step remains bundling the pinned Pyodide distribution locally and testing offline Python execution inside this worker.

## Implementation order after POC

1. Pin and bundle the candidate runtime.
2. Build a disposable worker/service harness outside the main lesson UI.
3. Automate protocol, timeout, cancellation, and output tests.
4. Test on physical Android hardware, including a low-memory device.
5. Implement the production adapter for `PythonExecutionEngine`.
6. Connect the editor Run/Stop controls only after the security gate passes.
7. Build the first mission around concepts already taught in Lesson 00.01.


## Source notes

- [Pyodide v314.0.7 — Web Worker guide](https://pyodide.org/en/314.0.0/usage/webworker.html): Pyodide uses a module-type worker and documents message-based execution outside the UI thread.
- [Pyodide v314.0.7 — downloading and deploying](https://pyodide.org/en/314.0.0/usage/downloading-and-deploying.html): the full distribution is documented as 200+ MB; the core archive contains the runtime loader, WASM binary, Python standard library, and lockfile.
- [Pyodide — interrupting execution](https://pyodide.org/en/stable/usage/keyboard-interrupts.html): cooperative interruption requires a Web Worker, SharedArrayBuffer, and appropriate cross-origin isolation headers.
- [MDN — Worker.terminate()](https://developer.mozilla.org/en-US/docs/Web/API/Worker/terminate): termination immediately stops a worker without waiting for its code to finish.
- [Android — security tips](https://developer.android.com/privacy-and-security/security-tips): Android recommends minimizing permissions and protecting app-private data and IPC boundaries.
