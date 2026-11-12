# AEGIS-Net — IMPLEMENTATION ROADMAP

> Status: active implementation plan · 12 November 2026
> Source of truth: `GAME_CONCEPT.md`, `GAMEPLAY_DESIGN.md`, and `UI_ARCHITECTURE.md`.
> This roadmap orders implementation work. It does not mark a feature complete until code and tests exist.

## Delivery strategy

Build vertical slices: each slice should introduce a real domain capability, a usable interface when appropriate, persistence where required, and automated tests. Do not build decorative screens ahead of their behavior. Keep the app offline-first and preserve the approved eight-mission First Contact sequence.

## Phase 0 — Engineering foundation

- [x] Inspect the existing Android shell and current CI workflow.
- [x] Establish an implementation roadmap aligned with the approved gameplay design.
- [x] Add automated Kotlin unit-test execution to Android CI.
- [x] Verify successful Kotlin JVM unit tests on the merge candidate (GitHub Actions PR check).
- [ ] Establish feature package boundaries and test conventions.
- [ ] Add a small shared domain-result/error convention only when a concrete feature needs it.

**Exit criteria:** the project can compile and execute local JVM unit tests in CI; no claim of success before a completed workflow run.

## Phase 1 — Evidence domain foundation

### Slice 1.1 — Evidence data contract

- [x] Define typed evidence records with stable IDs, source/provenance, timestamps, integrity, access classification, origin, operation association, discovery state, and annotations.
- [x] Define typed relationships for support/conflict, entity identity, chronology, derivation/copy, and patterns.
- [x] Distinguish confirmed relationships, player hypotheses, and unresolved links.
- [x] Define claims with separate supporting and contradicting evidence references.
- [x] Add deterministic graph validation for duplicate IDs, missing references, invalid self-links, duplicate relationships, and claim consistency.
- [x] Add JVM unit tests for valid and invalid graph cases.
- [ ] Review domain contract before wiring UI or persistence.

### Slice 1.2 — Evidence repository and persistence

- [ ] Select the local persistence approach based on the existing dependency/build setup (Room is preferred for structured relational evidence if dependency and migration costs remain acceptable).
- [ ] Define repository interface independently of Android UI.
- [ ] Persist evidence, relations, claims, annotations, and discovery state by operation.
- [ ] Add schema versioning and migration tests.
- [ ] Ensure source evidence is immutable; annotations and graph links are separate player-owned data.
- [ ] Add repository tests for insert, update, query, archive, and process-restart recovery.

### Slice 1.3 — Evidence service/use cases

- [x] Add evidence records with idempotent stable-ID handling and reject conflicting ID reuse.
- [x] Add player annotations as separate records without mutating source evidence.
- [x] Add typed relationships after validating both evidence endpoints.
- [ ] Add, update, and remove player hypotheses with explicit status.
- [ ] Promote a hypothesis to confirmed only through an explicit authored rule or player-confirmed action; never silently infer it.
- [x] Create and revise claims while retaining their evidence references.
- [x] Filter evidence in memory by source, type, event time, integrity, and discovery state.
- [ ] Add repository-backed filtering and persistence tests.
- [ ] Test authorization, immutability, and idempotency rules across persistence.

### Slice 1.4 — Evidence Graph interface

- [ ] Build an operation-scoped Evidence screen using the AEGIS workstation visual language.
- [ ] Provide a readable evidence list and detail view before adding a graph visualization.
- [ ] Show provenance, event time versus receipt/creation time, integrity, and source classification.
- [ ] Add a relationship editor with explicit relation type and status.
- [ ] Visualize confirmed links, hypotheses, and unresolved links differently.
- [ ] Provide a timeline and source-comparison view for mobile layouts.
- [ ] Preserve selection and scroll state when switching apps.
- [ ] Add Compose UI tests for core interactions and accessibility.

**Phase exit criteria:** a player can discover, inspect, annotate, relate, compare, and revisit evidence; every relationship is traceable and survives app restart.

## Phase 2 — Mission and operation foundation

- [ ] Define typed mission objectives, constraints, proof requirements, accepted approaches, risk dimensions, and recovery routes.
- [ ] Implement an operation context shared by Mission Control, Evidence, Editor, and Terminal.
- [ ] Separate mission definition/content from runtime player state.
- [ ] Implement deterministic mission evaluation against semantic outcomes and evidence provenance.
- [ ] Keep RUN and SUBMIT separate; a successful RUN alone never completes an operation.
- [ ] Add debrief output with intelligence, evidence, technical, exposure, and judgment dimensions only when active for that mission.
- [ ] Test multiple valid approaches, incomplete evidence, responsible uncertainty, and repeat submission idempotency.

## Phase 3 — First Contact vertical slice

Preserve all eight approved learning missions and their existing learning order.

- [ ] Build the opening recruitment and notification sequence with interruption-safe state.
- [ ] Implement the first operation end-to-end with briefing, evidence, investigation, decision/report, debrief, and saved progress.
- [ ] Add Freya and NEXUS message delivery/unlock rules.
- [ ] Add only the tools needed by each mission; no forced app-hopping.
- [ ] Validate the first operation on a physical Android phone in portrait orientation.

**Exit criteria:** at least one complete playable operation with persistent state, meaningful evidence, deterministic evaluation, and no fake terminal or telemetry behavior.

## Phase 4 — Technical workstation

- [ ] Implement the game-controlled virtual file system with separate personal and mission workspaces.
- [ ] Implement a real in-game terminal parser/dispatcher; never expose the Android shell.
- [ ] Implement editor state and exact script-version/hash tracking.
- [ ] Integrate the controlled Python runtime only after operation and VFS boundaries are tested.
- [ ] Add RUN feedback and SUBMIT evaluation as separate flows.
- [ ] Enforce authorization and resource limits; document that in-process Python is not a hardened security sandbox.

## Phase 5 — Campaign continuity and expanded routes

- [ ] Persist meaningful decisions, claims, reports, character knowledge, trust conditions, rank, clearance, and unlocks.
- [ ] Add alternate accepted approaches to selected operations and test each route.
- [ ] Add authored operational consequences with fair warnings and recovery routes.
- [ ] Ensure characters only know information they received or independently discovered.
- [ ] Add archive/replay behavior without duplicate rewards.

## Phase 6 — Polish and release readiness

- [ ] Complete Android portrait usability, IME, touch-target, text-scaling, TalkBack, and reduced-motion checks.
- [ ] Add regression tests for process death, rotation/configuration change, and offline behavior.
- [ ] Verify clean debug and release builds.
- [ ] Review original art/audio/license provenance and app permissions.
- [ ] Update project status and release notes from verified implementation only.

## Current implementation boundary

The Android app is still an initial shell. The Evidence domain contract and validation are the first real gameplay-domain implementation slice. Persistence, graph UI, mission integration, terminal, VFS, Python runtime, campaign continuity, and playable operations remain unimplemented until their corresponding checklist items are completed and verified.

## Engineering rules

1. Domain rules do not live inside Compose composables.
2. UI state is a projection of domain/repository state, not the source of truth.
3. Stable IDs and provenance are preserved across imports, copies, derived artifacts, and archives.
4. Source evidence is not mutated by player annotations or hypotheses.
5. Confirmed facts, player hypotheses, and unresolved questions remain distinct in both model and UI.
6. All core campaign behavior is deterministic and offline-capable.
7. Tests must cover domain rules before UI polish.
8. A checked item means implemented and reviewed; a build/test item additionally requires a recorded successful run.
