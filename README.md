# AEGIS-Net

**A Python-powered intelligence operations RPG for Android.**

AEGIS-Net blends narrative-driven role-playing, investigation, and hands-on Python programming. The player begins as an ordinary civilian whose device is compromised, then follows an escalating chain of events that leads to recruitment by A.E.G.I.S. and eventually to high-stakes cyber operations.

The goal is to make programming feel like a practical tool in the world of the game—not a sequence of detached classroom exercises.

## Game Concept

The story follows the player's journey through three major stages:

### Prologue — The Awakening

The player's device receives an encrypted message from A.E.G.I.S. while an unknown entity locks down its security system. With guidance from mentor Freya, the player explores a terminal, checks files, discovers `message_fragment.txt`, and writes a first Python script using `print()` and a string variable.

**Learning focus:** terminal basics, files, output, strings, and variables.

### Act 1 — The Recruit

The player arrives at A.E.G.I.S. Manila HQ as a Recruit. An early intelligence task involves checking an access code and filtering corrupted information. The player begins using input and conditional logic to make decisions that affect the operation.

**Learning focus:** `input()`, `if`, and `else`.

### Act 2 — The Analyst

A cybercrime syndicate floods the intelligence network with junk data to conceal illegal money transfers. Manual inspection is too slow. The player builds a log parser that processes large collections of records, while an escalating server-trace threat adds pressure to the investigation.

**Learning focus:** `for`, `while`, lists, indexing, and file-data processing.

### Act 3 — The Operative

The investigation reaches a defended enemy network. The player studies a tactical map of network nodes, uses intelligence gathered in earlier missions, and creates reusable tools to handle different security protocols.

**Learning focus:** dictionaries, custom functions, basic modules, and Virtual File System integration.

## Core Gameplay Principles

- **Story-led learning:** programming tasks exist because the operation needs them.
- **Experiment without punishment:** RUN is the player's workbench. Errors during experimentation should help the player understand and improve their solution.
- **Operational evaluation:** SUBMIT evaluates the player's work as part of the mission, separate from free experimentation.
- **Evidence and consequences:** intelligence gathered during missions should inform later decisions and operations.
- **Character guidance:** Freya Knox provides human mentorship and encouragement; NEXUS provides concise technical diagnostics.
- **RPG progression:** the player grows from a civilian learning basic commands into an operative capable of building automation tools.

## Main Characters

- **Freya Knox** — the player's mentor, guiding the player through operations and programming challenges.
- **NEXUS** — an AI support system focused on objective technical diagnostics.

## Platform and Technology

- **Platform:** Android
- **Application:** Kotlin and Jetpack Compose
- **In-game programming language:** Python
- **Execution integration:** Python runtime embedded in the Android application

## Project Direction

AEGIS-Net is intended to become a complete, playable campaign—not merely a collection of coding screens or a successful APK build. The experience should connect its narrative, missions, programming environment, investigation tools, progression, and persistent player decisions into one coherent game.

The detailed creative direction is maintained in [`GameConcept/lang_en_concept.md`](GameConcept/lang_en_concept.md).

## Project Status

The project is under active development. Features and systems should only be described as complete when they are implemented and verified in the game. A successful build alone does not mean that the gameplay experience is finished.
