# AEGIS-Net

Android-first adventure RPG and tactical intelligence simulator built with Kotlin and Jetpack Compose.

## Current implementation

The `feat/android-foundation` branch contains the first Android application shell:

- Kotlin + Jetpack Compose entry point
- Black-green A.E.G.I.S. semantic theme
- Main menu prototype
- Local secure-system status panel
- Campaign entry placeholder
- Seven-tile App Hub prototype
- Code-drawn tactical grid background and reusable panel primitive

This is a UI foundation only. It does not yet implement campaign persistence, story progression, messenger, intranet routing, terminal execution, VFS, Python runtime, or mission evaluation.

## Requirements

- Android Studio with Android SDK 36
- JDK 17
- Gradle 8.13-compatible environment

Open the repository in Android Studio and run the `app` configuration on an Android device or emulator.

## Important design constraints

- `GAME_CONCEPT.md` is the product and technical source of truth.
- `GAMEPLAY_DESIGN.md` defines the approved Intelligence Operations RPG gameplay systems and mission-design rules.
- `UI_ARCHITECTURE.md` defines the UI implementation blueprint and must present those gameplay systems clearly.
- `Story_Line.md` supplies narrative content; meaningful choices and continuity must follow the game concept and gameplay design.
- Hacknet is a high-level UX/architecture reference only. Do not copy its decompiled code, assets, or distinctive layouts.
- The game is offline-first. UI placeholders must not pretend that unimplemented systems are functional.
- RUN and SUBMIT must remain separate when their real execution and evaluation systems are implemented.

## Provisional technical identifier

The initial Android application ID is `com.mazbayid.aegisnet`. It is a technical placeholder and can be changed before the first public release.
