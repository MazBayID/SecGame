# AEGIS-Net — IMPLEMENTATION ROADMAP

> Status: active implementation plan · 12 November 2026
> Specification authority: follow the document roles and conflict hierarchy in `GAME_CONCEPT.md`; `GAMEPLAY_DESIGN.md` and `UI_ARCHITECTURE.md` own their respective specialist domains, while `Story_Line.md` owns narrative continuity. This roadmap orders implementation work and records verified status only; it cannot weaken requirements or mark a feature complete until code and tests exist.

## Delivery strategy

Build vertical slices: each slice should introduce a real domain capability, a usable interface when appropriate, persistence where required, and automated tests. Do not build decorative screens ahead of their behavior. Keep the app offline-first and preserve the approved eight-mission First Contact sequence.

## Phase 0 — Engineering foundation

- [x] Inspect the existing Android shell and current CI workflow.
- [x] Establish an implementation roadmap aligned with the approved gameplay design.
- [x] Add automated Kotlin unit-test execution to the combined Android CI workflow.
- [x] Configure the same workflow to build and upload exactly one installable Debug APK after all applicable checks pass on every push to `main`; pull requests run checks without publishing APKs.
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

- [x] Select Room 2.8.5 with KSP 2.2.20-2.0.4 for structured relational evidence on the existing Kotlin 2.2.20 / AGP 8.13.0 build.
- [x] Define the EvidenceGraphRepository boundary independently of Android UI.
- [x] Add normalized Room tables for evidence, annotations, relationships, claims, and claim-evidence links, scoped by operation.
- [x] Add database schema version 1 and schema export through the Room Gradle plugin.
- [x] Ensure source evidence is insert-only and conflicting reuse of an evidence ID is rejected; player annotations and graph links remain separate.
- [ ] Add Android database integration/migration tests for insert, update, query, archive, and process-restart recovery.

### Slice 1.3 — Evidence service/use cases

- [x] Add evidence records with idempotent stable-ID handling and reject conflicting ID reuse.
- [x] Add player annotations as separate records without mutating source evidence.
- [x] Add typed relationships after validating both evidence endpoints.
- [x] Explicitly confirm a player hypothesis only through a dedicated operation invoked after a player-confirmed action or authored mission rule; never silently infer confirmation.
- [x] Remove unconfirmed player hypotheses while protecting confirmed relationships.
- [ ] Add editing of hypothesis notes and confidence without changing their status.
- [x] Create and revise claims while retaining their evidence references.
- [x] Filter evidence in memory by source, type, event time, integrity, and discovery state.
- [ ] Add repository-backed filtering and Android database integration tests.
- [x] Test entity mapping and source-evidence immutability policy at the repository boundary.
- [ ] Test authorization and process-restart recovery against an actual Room database.

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

- [x] Define pure Kotlin mission/objective contracts with authored accepted approaches, outcomes, evidence requirements, and optional successful-RUN requirements.
- [x] Add deterministic SUBMIT evaluation for objective outcomes, evidence existence/proof, operation-bound RUN receipts, and exact source-hash matching.
- [ ] Define the remaining mission constraints, risk dimensions, decision/recovery routes, and debrief criteria.
- [ ] Implement an operation context shared by Mission Control, Evidence, Editor, and Terminal.
- [x] Separate authored mission/objective definitions from player submission state.
- [ ] Integrate the deterministic domain evaluator with an authoritative operation/evidence repository and Mission Control.
- [ ] Keep RUN and SUBMIT separate; a successful RUN alone never completes an operation.
- [ ] Add debrief output with Intelligence, Evidence, Technical, Stealth / Exposure, and Judgment dimensions only when active for that mission.
- [ ] Test multiple valid approaches, incomplete evidence, responsible uncertainty, and repeat submission idempotency.

## Phase 3 — First Contact vertical slice

Preserve all eight approved learning missions and their existing learning order.

- [x] Build the opening recruitment interaction sequence with sequential, interruption-safe local progress.
- [x] Author the canonical eight-mission First Contact sequence with stable IDs, briefings, learning concepts, semantic outcomes, and explicit uncertainty in the final report.
- [ ] Implement the first operation end-to-end with evidence fixtures, investigation, decision/report, debrief, and saved progress.
- [x] Add the opening Freya notification interaction and unlock NEXUS only after the player taps its link.
- [ ] Build the full Messenger inbox and conversation history; the current opening interaction is a focused vertical slice, not the complete Messenger app.
- [ ] Add only the tools needed by each mission; no forced app-hopping.
- [ ] Validate the first operation on a physical Android phone in portrait orientation.

**Exit criteria:** at least one complete playable operation with persistent state, meaningful evidence, deterministic evaluation, and no fake terminal or telemetry behavior.

## Phase 4 — Technical workstation

- [x] Implement an in-memory game-controlled VFS core with isolated personal/mission workspaces, path validation, read-only source evidence, and player-owned writable files.
- [x] Add deterministic workspace snapshots and a versioned, validated codec with round-trip JVM tests.
- [x] Add an offline SharedPreferences adapter with a global personal workspace key and per-operation mission snapshot keys.
- [ ] Add corruption recovery/diagnostics and size/resource limits before exposing persistence to player UI.
- [x] Implement a bounded in-game terminal parser/dispatcher for VFS commands; reject shell chaining, pipes, expansion, and redirection, and never expose the Android shell.
- [x] Add a Compose Terminal screen connected to the bounded dispatcher and offline workspace repository.
- [ ] Add automated Compose UI tests, persistent terminal history/session restoration, and richer output/keyboard behavior.
- [x] Add editor document state with dirty tracking, exact-buffer save, read-only source protection, and immutable SHA-256 RUN snapshot preparation.
- [x] Add a Compose Editor screen with workspace switching, file opening, dirty-buffer editing, read-only source protection, SHA-256 display, and local save.
- [ ] Add automated Compose UI tests, version history, and durable immutable RUN snapshot archive.
- [x] Integrate the Chaquopy 17.2.0 Python 3.13 runtime with a restricted teaching-language surface, bounded source/output/collection/step budgets, and VFS-only bridge calls.
- [x] Connect the Editor RUN action to Chaquopy, persist the latest receipt per operation, and display the exact executed source hash and output.
- [ ] Add on-device runtime tests and stronger process/resource isolation; the in-process runtime is not a hardened security sandbox.
- [x] Add a runtime-agnostic RUN coordinator contract that saves the exact editor buffer, hashes the immutable snapshot, calls an injected engine, and issues operation-bound success/failure receipts.
- [ ] Integrate a real controlled Python runtime and add RUN feedback and SUBMIT evaluation as separate player flows.
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
- [ ] Verify the combined Android CI workflow runs applicable automated source tests, then automatically builds exactly one installable Debug APK from each successful push to `main`, using the automatically generated debug signing key. Pull requests run checks only; do not create a separate unsigned Release artifact or a separate manual APK workflow.
- [ ] Validate APK archive integrity, package, versionCode, and ABI metadata; record the SHA-256 checksum.
- [ ] Install and smoke-test the Debug APK on a physical Android phone when available; report emulator and physical-device results separately. Because CI debug keys may change between ephemeral runners, uninstall an older test build before installing a differently signed candidate when required, warning that local app data may be erased.
- [ ] Keep Debug APKs strictly for development/testing. Any production or distribution signing identity requires separate explicit owner approval.
- [ ] Review original art/audio/license provenance and app permissions.
- [ ] Update project status and release notes from verified implementation only.

## Current implementation boundary

The Android app now has an initial campaign-opening vertical slice with a pure Kotlin progression state machine, interruption-safe local milestone persistence, and a player-driven official-message → mission-sequence → Freya → NEXUS unlock flow. A pure Kotlin mission contract and deterministic SUBMIT evaluator now enforce authored approaches/outcomes, evidence availability, operation-bound successful RUN records, and exact source-hash matching. The Evidence Graph remains a domain foundation only. Structured evidence persistence, graph UI, mission evaluation, the playable first operation, full Messenger/Intranet, terminal, VFS, Python runtime, and later campaign continuity remain unimplemented until their corresponding checklist items are completed and verified.

## Engineering rules

1. Domain rules do not live inside Compose composables.
2. UI state is a projection of domain/repository state, not the source of truth.
3. Stable IDs and provenance are preserved across imports, copies, derived artifacts, and archives.
4. Source evidence is not mutated by player annotations or hypotheses.
5. Confirmed facts, player hypotheses, and unresolved questions remain distinct in both model and UI.
6. All core campaign behavior is deterministic and offline-capable.
7. Tests must cover domain rules before UI polish.
8. A checked item means implemented and reviewed; a build/test item additionally requires a recorded successful run.
