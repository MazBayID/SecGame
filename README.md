# AEGIS-Net

Android-first adventure RPG and tactical intelligence simulator built with Kotlin and Jetpack Compose.

## Current implementation

The `main` branch currently contains:

- Kotlin + Jetpack Compose application entry point and black-green A.E.G.I.S. theme
- Main menu prototype, local secure-system status panel, and seven-tile App Hub prototype
- Code-drawn tactical grid background and reusable panel primitive
- Initial Evidence Graph domain contract: typed evidence records, provenance metadata, separate player annotations, relationship states, and claims
- Pure Kotlin graph operations for idempotent evidence creation, annotation, relationship/claim updates, and deterministic filtering
- JVM unit tests for graph validation, immutable source evidence, mutation behavior, and evidence search
- Android CI runs Kotlin JVM source validation only; it does not create an APK artifact
- `debug-apk-candidate.yml` is the manual, main-only workflow for building one installable Debug APK after Kotlin tests and applicable Python-test checks pass
- The Debug APK is signed by the Android Gradle Plugin's automatically generated debug key; no personal/permanent keystore, signing password, or GitHub signing secret is used
- The candidate workflow validates the APK archive, package, versionCode, debug certificate, ABI metadata, and SHA-256 checksum before uploading the artifact
- Debug APKs are development/testing artifacts only, not production releases. Ephemeral CI runners may generate different debug certificates, so a new candidate may require uninstalling the previous build before installation; uninstalling can erase local app data
- The candidate workflow has been added but has not yet been run successfully; no installable artifact is available until a successful workflow run

Planned technical target: Python 3.13 through Chaquopy for local in-game execution; this runtime is not implemented in the current foundation.

The Evidence Graph is currently a domain foundation only. Evidence persistence, repository operations, graph/timeline UI, mission integration, campaign persistence, story progression, Messenger, Intranet routing, functional Terminal, VFS, Python runtime, and mission evaluation are not implemented yet. The app is not a playable game.

## Implementation plan

See [IMPLEMENTATION_ROADMAP.md](IMPLEMENTATION_ROADMAP.md) for the phased delivery plan and completion criteria. All Markdown files form one coordinated specification set; read the relevant documents together and apply the document roles and conflict hierarchy defined in section 15 of [GAME_CONCEPT.md](GAME_CONCEPT.md). The binding single-APK build/release rules are in section 14 of [GAME_CONCEPT.md](GAME_CONCEPT.md).

## Design references

- [GAME_CONCEPT.md](GAME_CONCEPT.md) — product identity, story canon, and technical constraints.
- [GAMEPLAY_DESIGN.md](GAMEPLAY_DESIGN.md) — approved gameplay systems, Evidence Graph rules, multiple solution paths, risk, and evaluation.
- [UI_ARCHITECTURE.md](UI_ARCHITECTURE.md) — Android interface and workstation architecture.
- [Story_Line.md](Story_Line.md) — narrative content and continuity.

`GAME_CONCEPT.md` is the product/technical canon; `GAMEPLAY_DESIGN.md` and `UI_ARCHITECTURE.md` define their specialist domains; `IMPLEMENTATION_ROADMAP.md` tracks delivery order and verified completion; this README summarizes the repository state. A summary or roadmap checkbox never overrides the detailed specification.

Hacknet is a high-level UX/architecture reference only. Do not copy its decompiled code, assets, or distinctive layouts. The game is offline-first; UI placeholders must not pretend that unimplemented systems are functional. RUN and SUBMIT must remain separate when their real execution and evaluation systems are implemented.

## Requirements

- Android Studio with Android SDK 36
- JDK 17
- Gradle 8.13-compatible environment

Open the repository in Android Studio and run the `app` configuration on an Android device or emulator. Local/debug runs are development-only. The intended CI test artifact is an installable Debug APK signed with an automatically generated debug key; it is not a production release. Run the `Installable Debug APK Candidate` workflow manually from the Actions tab on the `main` branch to create it.

## Provisional technical identifier

The initial Android application ID is `com.mazbayid.aegisnet`. It is a technical placeholder and can be changed before the first public release.
