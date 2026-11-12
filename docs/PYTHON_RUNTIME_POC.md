# Python Runtime Proof of Concept

> Status: design and test plan. No runtime is integrated or approved yet.
>
> Scope: CYBER//TRACE in `MazBayID/SecGame` only. The separate `firstGame` project is out of scope and must not be inspected or used.

## Objective

Prove that a real Python interpreter can run beginner exercises on Android without executing player code in the main application process or exposing host resources. A successful `print("hello")` is necessary but not sufficient.

The POC is disposable: its code and assets must not become the production runtime until every mandatory gate is measured and reviewed.

## Candidate order

### Candidate A — Pyodide in a Web Worker hosted by Android WebView

This is the first implementation experiment because a dedicated worker can keep synchronous Python execution away from the UI thread and can be terminated as a whole when a deadline expires.

Proposed shape:

1. Bundle a pinned Pyodide distribution inside the APK. No runtime download or CDN dependency at execution time.
2. Load a local POC page through AndroidX `WebViewAssetLoader` using its app-assets HTTPS origin, not `file://`.
3. Create a module-type dedicated Web Worker. Initialize Pyodide only inside that worker.
4. Send a small, versioned JSON message containing an execution ID, source, workspace ID, and validated limits.
5. Capture stdout/stderr inside Python using a bounded writer. Return only structured results and capped output.
6. On timeout or cancellation, call `Worker.terminate()`, discard the worker instance, and create a fresh worker for the next run. Do not depend on cooperative Python cancellation to stop an infinite loop.
7. Keep the Pyodide virtual filesystem in memory and seed it only with mission-owned files.
8. Do not expose `addJavascriptInterface`, privileged JavaScript bridges, Android objects, host paths, app secrets, or arbitrary host callbacks to the page or worker.
9. Do not grant direct network access to the runtime. Networking lessons use explicit game-owned simulation messages, never real sockets.

Web Worker termination is a hard stop for that worker, but it is not proof of memory isolation or a separate OS process. Verify WebView renderer behavior and recovery on real Android devices. A worker shares the WebView renderer's security context; this POC must not claim stronger isolation than the platform actually provides.

### Candidate B — CPython in an Android isolated-process service

Keep this as the comparison path if Pyodide cannot satisfy Python compatibility or memory requirements.

- Declare the service with `android:isolatedProcess="true"`; a normal secondary process is not an equivalent security boundary.
- Verify whether the selected CPython distribution can initialize in this process and load its standard library without granting access to app-private files.
- Use Binder messages containing only bounded source and game-owned identifiers.
- Do not pass file descriptors, host paths, app context, credentials, or unrestricted network capabilities to player code.
- Verify cancellation, service death/restart, output caps, and memory behavior.
- Chaquopy compatibility must be demonstrated in this exact process model; support for Android by itself is not proof that it works in an isolated service.

## WebView hardening checklist

- [ ] Use a pinned, APK-bundled runtime and local assets only.
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

## Implementation order after POC

1. Pin and bundle the candidate runtime.
2. Build a disposable worker/service harness outside the main lesson UI.
3. Automate protocol, timeout, cancellation, and output tests.
4. Test on physical Android hardware, including a low-memory device.
5. Implement the production adapter for `PythonExecutionEngine`.
6. Connect the editor Run/Stop controls only after the security gate passes.
7. Build the first mission around concepts already taught in Lesson 00.01.
