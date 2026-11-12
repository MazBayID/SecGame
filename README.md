# AEGIS-Net

Android-first adventure RPG and tactical intelligence simulator built with Kotlin and Jetpack Compose.

## Current implementation

The `main` branch currently contains:

- Kotlin + Jetpack Compose application entry point and black-green A.E.G.I.S. theme
- Main menu prototype, local secure-system status panel, and App Hub; Terminal, Editor, Operations, and Database have working first-slice routes while Messenger, Intranet, Dossier, and Analysis remain placeholders
- First Contact opening vertical slice: sequential official recruitment, mission activation, Freya message, and player-triggered NEXUS contact unlock
- Pure Kotlin campaign progression rules with idempotent actions, out-of-order protection, corrupted-save prefix recovery, and JVM unit tests
- First Contact Mission 01 has a playable slice: discover the message fragment, edit/run its Python script in the mission workspace, attach evidence, submit against the operation-bound source-hash receipt, and persist completion offline; missions 02–08 are authored in the catalog but are not yet playable
- Room-backed, operation-scoped Evidence Graph storage with normalized evidence, annotation, relationship, claim, and claim-link tables; original evidence is insert-only and source-ID conflicts are rejected
- Pure Kotlin mission/objective contracts and deterministic SUBMIT evaluation for accepted approaches/outcomes, evidence availability, successful operation-bound RUN receipts, and exact source-hash integrity; the canonical eight-mission First Contact learning sequence is authored with stable IDs and uncertainty requirements
- Game-controlled VFS with isolated personal and per-operation mission workspaces, validated paths, immutable read-only mission sources, deterministic snapshots/codec, and an offline SharedPreferences adapter; the bounded Terminal UI dispatches only VFS commands and never exposes the Android shell; the Compose Editor supports workspace switching, dirty-buffer editing, save, SHA-256 display, and exact-buffer RUN snapshots; Chaquopy 17.2.0 embeds Python 3.13 with a restricted teaching-language surface, bounded source/output/collection/step budgets, and VFS-only bridge calls; RUN receipts are persisted per operation and display the exact source hash/output; this in-process runtime is not a hardened security sandbox
- Offline campaign milestone persistence through a dedicated SharedPreferences adapter; campaign domain remains Android-independent
- Code-drawn tactical grid background and reusable panel primitive
- Initial Evidence Graph domain contract: typed evidence records, provenance metadata, separate player annotations, relationship states, and claims
- Pure Kotlin graph operations for idempotent evidence creation, annotation, relationship/claim updates, and deterministic filtering
- JVM unit tests for graph validation, immutable source evidence, mutation behavior, and evidence search
- The `DATABASE` App Hub route now opens an operation-scoped Evidence Archive: discovered evidence can be inspected with provenance, source reference, integrity, access/origin metadata, and private player notes saved separately from immutable source records
- The single `android-ci.yml` workflow runs Kotlin JVM tests and applicable Python-test checks; on every successful push to `main`, it then builds and uploads exactly one installable Debug APK
- Pull requests targeting `main` run the same source checks but do not publish APK artifacts; `workflow_dispatch` can also run the combined workflow manually on `main`
- The Debug APK is signed by the Android Gradle Plugin's automatically generated debug key; no personal/permanent keystore, signing password, or GitHub signing secret is used
- The combined workflow validates the APK archive, package, versionCode, debug certificate, ABI metadata, and SHA-256 checksum before uploading the artifact
- Debug APKs are development/testing artifacts only, not production releases. Ephemeral CI runners may generate different debug certificates, so a new candidate may require uninstalling the previous build before installation; uninstalling can erase local app data
- APK artifacts are produced only after the required checks pass; each successful `main` push uploads one APK and its SHA-256 checksum for 14 days



The Evidence Graph domain and Room persistence are implemented; the first Evidence Archive list/detail/annotation UI is now connected, while relationship editing, graph visualization, timeline, and comparison UI remain pending. The first mission is playable, but the other seven missions are catalog definitions only. Full Messenger history, Intranet routing, later campaign continuity, full mission progression, broader Compose UI tests, device-level runtime/security checks, and physical-device smoke testing remain incomplete. The app is not yet a complete end-to-end game.

## Implementation plan

See [IMPLEMENTATION_ROADMAP.md](IMPLEMENTATION_ROADMAP.md) for the phased delivery plan and completion criteria. All Markdown files form one coordinated specification set; read the relevant documents together and apply the document roles and conflict hierarchy defined in section 15 of [GAME_CONCEPT.md](GAME_CONCEPT.md). The binding single-APK build/release rules are in section 14 of [GAME_CONCEPT.md](GAME_CONCEPT.md).

## Design references

- [GAME_CONCEPT.md](GAME_CONCEPT.md) — product identity, story canon, and technical constraints.
- [GAMEPLAY_DESIGN.md](GAMEPLAY_DESIGN.md) — approved gameplay systems, Evidence Graph rules, multiple solution paths, risk, and evaluation.
- [UI_ARCHITECTURE.md](UI_ARCHITECTURE.md) — Android interface and workstation architecture.
- [Story_Line.md](Story_Line.md) — narrative content and continuity.

`GAME_CONCEPT.md` is the product/technical canon; `GAMEPLAY_DESIGN.md` and `UI_ARCHITECTURE.md` define their specialist domains; `IMPLEMENTATION_ROADMAP.md` tracks delivery order and verified completion; this README summarizes the repository state. A summary or roadmap checkbox never overrides the detailed specification.

Hacknet is a high-level UX/architecture reference only. Do not copy its decompiled code, assets, or distinctive layouts. The game is offline-first; UI placeholders must not pretend that unimplemented systems are functional. The first mission now has a separate Editor RUN and Mission Control SUBMIT flow. RUN saves and executes the exact editor snapshot through the embedded Python runtime; SUBMIT independently checks the operation-bound receipt, source hash, authored outcomes, and evidence. This integration currently covers Mission 01 only.

## Requirements

- Android Studio with Android SDK 36
- JDK 17
- Gradle 8.13-compatible environment

Open the repository in Android Studio and run the `app` configuration on an Android device or emulator. Local/debug runs are development-only. The combined Android CI workflow automatically builds an installable Debug APK after all required checks pass on each push to `main`. Pull requests run tests without publishing an APK. A manual run is also available through the `Android CI and Installable Debug APK` workflow on `main`.

## Provisional technical identifier

The initial Android application ID is `com.mazbayid.aegisnet`. It is a technical placeholder and can be changed before the first public release.
