# CYBER//TRACE — Architecture & Technical Direction

> Status: Phase 0 assessment / proposed baseline  
> Decision state: Android direction accepted as the working target; Python sandbox implementation remains **unapproved until a proof of concept passes security and compatibility tests**.  
> Scope: This document describes the new CYBER//TRACE implementation in this repository only.

## 1. Repository boundary

- Canonical project: `MazBayID/SecGame`.
- This repository owns CYBER//TRACE source code, documentation, tests, and build configuration.
- No source code, assets, story material, or implementation from any other game repository is required.
- Preserve the existing concept and story documents as the creative source of truth.

## 2. Platform direction

The working target is an Android phone application.

### Proposed application stack

| Area | Proposed technology | Reason |
|---|---|---|
| Application language | Kotlin | Native Android integration and maintainable application logic |
| UI | Jetpack Compose | Screen-based terminal, editor, panels, dialogue, and responsive layouts |
| Application structure | Modular, layered architecture | Keep UI, game rules, learning content, persistence, and execution boundaries separate |
| Local persistence | Room for structured game state; DataStore for small preferences | Save missions, progress, evidence, relationships, and settings without mixing them into UI code |
| Learning content | Versioned data files plus Kotlin models | Allow missions and lessons to be tested independently from screens |
| Python execution | Isolated runtime behind a narrow IPC interface | Player code must never execute with the main application's privileges |

These are proposed choices, not claims about code already present in the repository.

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
7. Player introduces a deliberate syntax or runtime error and receives understandable feedback.
8. A first mission uses only concepts already introduced.
9. Mission completion is checked using program behavior, not exact text matching alone.
10. Story and learning progress can be saved and restored.
11. Critical behavior is covered by automated tests.

Do not expand the campaign until this vertical slice is reliable.

## 7. Initial implementation modules

A possible source layout after the Android project is scaffolded:

```text
app/
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
  execution-api/
  android-execution-host/
core/
  models/
  mission-engine/
  learning-engine/
content/
  story/
  missions/
  lessons/
docs/
  testing/
```

This is a logical target layout, not a report of existing files.

## 8. Phase 0 findings

- The repository currently contains the CYBER//TRACE design documents and no verified Android application implementation.
- The concept requires real Python execution and controlled fictional systems.
- Android is the working platform direction.
- The UI/application stack can proceed as a proposed native Kotlin architecture.
- Python runtime isolation is the main technical risk and must be validated before it becomes a dependency of the game.
- Build-tool versions, minimum Android API, supported CPU architectures, and device-performance budgets must be selected after the initial build environment and runtime proof of concept are established.

## 9. References

- [Android service manifest element — isolatedProcess](https://developer.android.com/guide/topics/manifest/service-element)
- [Android security tips — application sandbox](https://developer.android.com/privacy-and-security/security-tips)
- [Chaquopy — Python SDK for Android](https://chaquo.com/chaquopy/)
- [Chaquopy version compatibility](https://chaquo.com/chaquopy/doc/current/versions.html)
- [Python documentation — using Python on Android](https://docs.python.org/3/using/android.html)

---

**Next engineering milestone:** scaffold the native Android application and build the Python-runtime isolation proof of concept as separate, testable work. Do not integrate player-code execution into the main game process.
