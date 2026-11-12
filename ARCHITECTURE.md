# CYBER//TRACE — Architecture & Technical Direction

> Status: Phase 1 foundation started; Android CI build verified.  \
> Decision state: Android direction accepted as the working target; Python sandbox implementation remains **unapproved until a proof of concept passes security and compatibility tests**.  \
> Scope: This document describes the CYBER//TRACE implementation in this repository only.

## 1. Repository boundary

- Canonical project: `MazBayID/SecGame`.
- This repository owns CYBER//TRACE source code, documentation, tests, and build configuration.
- No source code, assets, story material, or implementation from any other game repository is required.
- Preserve the existing concept and story documents as the creative source of truth.
- Do not inspect, modify, copy, or depend on the separate `firstGame` project. It is outside this repository's scope.

## 2. Platform direction

The working target is an Android phone application.

### Application stack

| Area | Current foundation | Reason / status |
|---|---|---|
| Application language | Kotlin | Native Android integration |
| UI | Jetpack Compose | Screen-based terminal, editor, panels, dialogue, and responsive layouts |
| Android Gradle Plugin | 9.4.0 | Current build configuration; CI build verified |
| Gradle | 9.6.0 | Used by the CI workflow; CI build verified |
| JDK | 17 | Used by the CI workflow |
| Kotlin support | AGP built-in Kotlin | Avoids the obsolete separate Android Kotlin plugin |
| Compose compiler plugin | 2.4.21 | Matches the selected Kotlin toolchain |
| Compose BOM | 2026.09.00 | Aligns Compose library versions |
| compileSdk / targetSdk | 37 / 37 | Current build configuration |
| minSdk | 26 | Initial baseline; may change after device testing |
| Application ID | `com.mazbayid.cybertrace` | New identity for this project |
| Local persistence | Not yet implemented | Room and DataStore remain candidates |
| Python execution | Not yet implemented | Must be isolated behind a narrow IPC interface |

The Android project scaffold and initial terminal screen compile successfully in GitHub Actions. The current CI pipeline also compiles and executes the unit-test source set, assembles a debug APK, and uploads it as an artifact. Physical-device installation and runtime behavior have not yet been verified.

Version references:
- [Android Gradle Plugin 9.4.0 release notes](https://developer.android.com/build/releases/agp-9-4-0-release-notes)
- [Kotlin releases](https://kotlinlang.org/docs/releases.html)
- [Jetpack Compose BOM](https://developer.android.com/develop/ui/compose/bom)

## 3. High-level architecture

The application should be separated into the following boundaries:

1. **Presentation** — Compose screens, navigation, terminal output, editor, dialogue, and accessibility.
2. **Game domain** — missions, objectives, inventory, progression, relationships, evidence, and story decisions.
3. **Learning domain** — curriculum prerequisites, lessons, practice tasks, mastery checks, and NLAS assistance levels.
4. **Simulation domain** — game-owned virtual files, fictional services, databases, devices, logs, and mission fixtures.
5. **Persistence** — saved game state, settings, lesson progress, and virtual-world data.
6. **Python execution boundary** — a separate execution component receiving bounded requests and returning bounded results.
7. **Content** — story chapters, dialogue, mission definitions, lesson definitions, and localization.

The game domain must not directly access Android storage, network sockets, or the Python interpreter. Those capabilities must be provided through explicit interfaces.

## 4. Python runtime: critical proof of concept

A real Python interpreter is mandatory. A fake command parser, text-matching answer checker, or restricted imitation of Python is not an acceptable substitute.

However, arbitrary Python code can access files, processes, imports, sockets, and consume unbounded resources. AST checks, filtered built-ins, or hiding dangerous commands are not security boundaries.

### Candidate A — Android isolated service

Investigate running the interpreter inside an Android service declared with `android:isolatedProcess="true"`.

Android documents that such a service runs in a special isolated process without its own permissions, and communicates with the rest of the app through the Service API. This is a promising boundary, but does not by itself prove that a chosen Python runtime can be initialized safely there.

Questions the proof of concept must answer:

- Can the selected CPython distribution initialize in the isolated service?
- Can its standard library and required runtime files be loaded without granting access to the app's private data?
- Can the runtime use a dedicated temporary/virtual filesystem rather than the host application's files?
- Can all player-visible file operations be routed through a game-owned virtual filesystem?
- Can network access be denied at the process boundary, not merely by hiding Python APIs?
- Can execution be stopped reliably after a deadline or output limit?
- Can memory and output limits be enforced and tested on supported Android versions?

Chaquopy is a candidate for packaging CPython in an Android application, but its compatibility with this isolated-service design must be demonstrated. Do not initialize untrusted player code inside the main application process.

### Candidate B — WebAssembly Python runtime

Investigate a CPython WebAssembly runtime hosted in a tightly configured WebView or another renderer sandbox.

Questions to validate:

- Can the interpreter and required standard-library modules run fully offline?
- Can player code be prevented from reaching Android JavaScript bridges, host files, and unrestricted network resources?
- Can the renderer be terminated and recreated after timeouts or runaway execution?
- Are memory, execution time, and output size bounded?
- Is the package size and performance acceptable on mid-range Android devices?

A WebView must not expose a privileged JavaScript bridge to player-controlled code.

### Evidence-based candidate notes (research update)

**Chaquopy / CPython**
- Chaquopy 17.2 currently documents support for Python 3.10–3.15, AGP 7.3–9.5, and Android API 24+.
- Its FAQ states that Python normally runs in the main app process. It mentions a separate process as an advanced option, but a separate process alone is not equivalent to Android's isolated-process security boundary.
- Therefore, Chaquopy is a strong compatibility candidate for real CPython, but it is **not approved as the player-code sandbox**. The proof of concept must demonstrate initialization inside a genuinely isolated process and test filesystem, permission, and process boundaries.

**Pyodide / WebAssembly**
- Pyodide documents running Python in a Web Worker to keep long-running execution off the UI thread.
- Its WebAssembly environment does not provide working native sockets, threading, or multiprocessing in the same way as desktop CPython. This is a meaningful limitation for the curriculum, but it may be acceptable if networking lessons use explicit game-owned simulation APIs rather than real sockets.
- Pyodide's interrupt mechanism has additional requirements (including a SharedArrayBuffer setup); the prototype must also test whether terminating and recreating a worker is reliable on Android WebView.
- WebView must not expose a privileged JavaScript interface to player-controlled Python/JavaScript. Android's security guidance warns that JavaScript interfaces can expose host-app methods to all frames.

**Current assessment (not a final runtime selection)**
- Chaquopy offers the strongest CPython compatibility, but its process model requires careful validation before it can satisfy the isolation requirement.
- Pyodide offers a WebAssembly execution boundary and worker model, but has compatibility and interruption constraints.
- Neither candidate is approved yet. The POC must compare offline startup, package size, Android memory use, bounded execution, worker/process termination, virtual-file integration, and resistance to host access.
- The game curriculum must not depend on real internet sockets. Simulated networking remains the intended educational interface unless a future safe runtime proves otherwise.

Research references:
- [Chaquopy version compatibility](https://chaquo.com/chaquopy/doc/current/versions.html)
- [Chaquopy FAQ — Python process and interruption](https://chaquo.com/chaquopy/doc/current/faq.html)
- [Pyodide — Web Workers](https://pyodide.org/en/stable/usage/webworker.html)
- [Pyodide — interrupting execution](https://pyodide.org/en/stable/usage/keyboard-interrupts.html)
- [Pyodide — WebAssembly constraints](https://pyodide.org/en/stable/usage/wasm-constraints.html)
- [Android — WebView native bridge security](https://developer.android.com/privacy-and-security/risks/insecure-webview-native-bridges)
- [Android — application security tips](https://developer.android.com/privacy-and-security/security-tips)

### Runtime decision gate

Do not select either candidate based on convenience or a successful “hello world” alone. Build a small disposable proof of concept and compare both approaches where practical. Select a runtime only after the tests below pass. If neither candidate provides adequate isolation, stop and propose a safer alternative rather than weakening the security requirement.

### Mandatory runtime tests

| Test | Required result |
|---|---|
| Basic Python | Real Python code runs and returns correct output |
| Syntax/runtime errors | Structured, understandable diagnostics are returned |
| Infinite loop | Execution is terminated within a defined deadline |
| Excessive output | Output is capped without freezing the UI |
| Memory exhaustion | The game process remains responsive and recovers safely |
| Host file access | Player code cannot read or modify application/private host files |
| Environment/credentials | Player code cannot inspect app secrets or environment data |
| Network access | Direct external network access is unavailable |
| Virtual files | Only explicitly exposed game-owned files are accessible |
| Simulation API | Access is limited to explicit, validated game APIs |
| Repeated runs | Runtime can reset cleanly between missions or execution sessions |
| Process failure | Main game survives, reports failure, and can restart the execution worker |

## 5. Game-owned simulation APIs

Player code should interact with fictional resources through a small, explicit API. Examples may include:

- Reading a mission-provided virtual file.
- Inspecting fictional device telemetry.
- Querying a simulated service.
- Reading or writing an isolated mission database.
- Reviewing fictional system logs.

The API must validate inputs, enforce mission permissions, and return structured data. It must never silently map virtual paths or simulated network destinations to real host resources.

## 6. First playable vertical slice

The first implementation milestone should prove one complete experience rather than create many disconnected screens:

1. Application launches into a readable CYBER//TRACE terminal interface.
2. Player can navigate a minimal workspace.
3. Opening scene introduces NEXUS CITY and the first contact.
4. ARIA or NLAS explains what Python is and what the interpreter does.
5. Player opens an editor and enters a short Python program.
6. The real interpreter executes it and displays output.
7. The player introduces a deliberate syntax or runtime error and receives understandable feedback.
8. A first mission uses only concepts already introduced.
9. Mission completion is checked using program behavior, not exact text matching alone.
10. Story and learning progress can be saved and restored.
11. Critical behavior is covered by automated tests.

Do not expand the campaign until this vertical slice is reliable.

## 7. Initial implementation modules

The repository now has the initial Android application scaffold. The following is the intended direction as the project grows:

```text
app/
  src/main/java/com/mazbayid/cybertrace/
    presentation/
      navigation/
      terminal/
      editor/
      dialogue/
      mission/
    domain/
      game/
      learning/
      simulation/
    data/
      persistence/
      content/
    execution/
      api/
      android/
content/
  story/
  missions/
  lessons/
docs/
  testing/
```

This is a target structure, not a claim that every listed module already exists.

## 8. Phase 0 findings and current progress

- The repository originally contained the CYBER//TRACE design documents and no verified Android application implementation.
- The concept requires real Python execution and controlled fictional systems.
- Android is the working platform direction.
- A native Kotlin/Compose application scaffold and an initial terminal-themed launch screen have been added.
- GitHub Actions run **#43259419839** completed successfully for commit `046feb3c0d8ce59fb001162863c107037087a31f`.
- The workflow successfully compiled the app, compiled and executed the unit-test source set, assembled a debug APK, and uploaded the `cyber-trace-debug` artifact.
- Three unit tests currently cover the lesson prerequisite access policy: an initial lesson is unlocked, a lesson remains locked when a prerequisite is incomplete, and it unlocks when all prerequisites are complete.
- This CI result does **not** verify installation, launch, or behavior on a physical Android device. Those checks remain outstanding.
- Python runtime isolation is the main technical risk and must be validated before it becomes a dependency of the game.
- The first runtime proof of concept must compare Android isolated-service execution with a WebAssembly-based option before a final runtime decision.

CI evidence: [GitHub Actions run #43259419839](https://github.com/MazBayID/SecGame/actions/runs/43259419839).

## 9. References

- [Android service manifest element — isolatedProcess](https://developer.android.com/guide/topics/manifest/service-element)
- [Android security tips — application sandbox](https://developer.android.com/privacy-and-security/security-tips)
- [Android built-in Kotlin support](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [Chaquopy — Python SDK for Android](https://chaquo.com/chaquopy/)
- [Chaquopy version compatibility](https://chaquo.com/chaquopy/doc/current/versions.html)
- [Python documentation — using Python on Android](https://docs.python.org/3/using/android.html)

---

**Next engineering milestone:** verify the app on a physical Android device, then build the Python-runtime isolation proof of concept as separate, testable work. Do not integrate player-code execution into the main game process.
