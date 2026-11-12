# AEGIS-Net

Android-first adventure RPG and tactical intelligence simulator built with Kotlin and Jetpack Compose.

## Current implementation

The `main` branch currently contains:

- Kotlin + Jetpack Compose application entry point and black-green A.E.G.I.S. theme
- Main menu prototype, local secure-system status panel, and seven-tile App Hub prototype
- First Contact opening vertical slice: sequential official recruitment, mission activation, Freya message, and player-triggered NEXUS contact unlock
- Pure Kotlin campaign progression rules with idempotent actions, out-of-order protection, corrupted-save prefix recovery, and JVM unit tests
- Pure Kotlin mission/objective contracts and deterministic SUBMIT evaluation for accepted approaches/outcomes, evidence availability, successful operation-bound RUN receipts, and exact source-hash integrity; JVM tests cover valid and invalid submissions
- Offline campaign milestone persistence through a dedicated SharedPreferences adapter; campaign domain remains Android-independent
- Code-drawn tactical grid background and reusable panel primitive
- Initial Evidence Graph domain contract: typed evidence records, provenance metadata, separate player annotations, relationship states, and claims
- Pure Kotlin graph operations for idempotent evidence creation, annotation, relationship/claim updates, and deterministic filtering
- JVM unit tests for graph validation, immutable source evidence, mutation behavior, and evidence search
- The single `android-ci.yml` workflow runs Kotlin JVM tests and applicable Python-test checks; on every successful push to `main`, it then builds and uploads exactly one installable Debug APK
- Pull requests targeting `main` run the same source checks but do not publish APK artifacts; `workflow_dispatch` can also run the combined workflow manually on `main`
- The Debug APK is signed by the Android Gradle Plugin's automatically generated debug key; no personal/permanent keystore, signing password, or GitHub signing secret is used
- The combined workflow validates the APK archive, package, versionCode, debug certificate, ABI metadata, and SHA-256 checksum before uploading the artifact
- Debug APKs are development/testing artifacts only, not production releases. Ephemeral CI runners may generate different debug certificates, so a new candidate may require uninstalling the previous build before installation; uninstalling can erase local app data
- APK artifacts are produced only after the required checks pass; each successful `main` push uploads one APK and its SHA-256 checksum for 14 days

Planned technical target: Python 3.13 through Chaquopy for local in-game execution; this runtime is not implemented in the current foundation.

The Evidence Graph is currently a domain foundation only. Structured evidence persistence, graph/timeline UI, authored mission content, a complete playable operation, full Messenger history, Intranet routing, functional Terminal, VFS, and Python runtime are not implemented yet. The opening campaign flow is a persisted vertical slice, not a complete game. The app is not yet a playable end-to-end game.

## Implementation plan

See [IMPLEMENTATION_ROADMAP.md](IMPLEMENTATION_ROADMAP.md) for the phased delivery plan and completion criteria. All Markdown files form one coordinated specification set; read the relevant documents together and apply the document roles and conflict hierarchy defined in section 15 of [GAME_CONCEPT.md](GAME_CONCEPT.md). The binding single-APK build/release rules are in section 14 of [GAME_CONCEPT.md](GAME_CONCEPT.md).

## Design references

- [GAME_CONCEPT.md](GAME_CONCEPT.md) — product identity, story canon, and technical constraints.
- [GAMEPLAY_DESIGN.md](GAMEPLAY_DESIGN.md) — approved gameplay systems, Evidence Graph rules, multiple solution paths, risk, and evaluation.
- [UI_ARCHITECTURE.md](UI_ARCHITECTURE.md) — Android interface and workstation architecture.
- [Story_Line.md](Story_Line.md) — narrative content and continuity.

`GAME_CONCEPT.md` is the product/technical canon; `GAMEPLAY_DESIGN.md` and `UI_ARCHITECTURE.md` define their specialist domains; `IMPLEMENTATION_ROADMAP.md` tracks delivery order and verified completion; this README summarizes the repository state. A summary or roadmap checkbox never overrides the detailed specification.

Hacknet is a high-level UX/architecture reference only. Do not copy its decompiled code, assets, or distinctive layouts. The game is offline-first; UI placeholders must not pretend that unimplemented systems are functional. The mission domain now validates SUBMIT requirements, but the editor, real RUN coordinator, controlled VFS, and Python runtime are still unimplemented. When connected, RUN and SUBMIT must remain separate.

## Requirements

- Android Studio with Android SDK 36
- JDK 17
- Gradle 8.13-compatible environment

Open the repository in Android Studio and run the `app` configuration on an Android device or emulator. Local/debug runs are development-only. The combined Android CI workflow automatically builds an installable Debug APK after all required checks pass on each push to `main`. Pull requests run tests without publishing an APK. A manual run is also available through the `Android CI and Installable Debug APK` workflow on `main`.

## Provisional technical identifier

The initial Android application ID is `com.mazbayid.aegisnet`. It is a technical placeholder and can be changed before the first public release.
