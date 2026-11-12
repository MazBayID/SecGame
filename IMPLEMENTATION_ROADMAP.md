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

- [x] Build an operation-scoped Evidence Archive screen using the AEGIS workstation visual language.
- [x] Provide a readable discovered-evidence list and detail view before adding a graph visualization.
- [x] Show provenance, event time versus receipt/creation time when present, integrity, and source classification.
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
- [x] Integrate the deterministic domain evaluator with the Room evidence repository and Mission Control for First Contact Missions 01–08; the second operation has its own evidence requirement, output requirements, exact-source RUN receipt check, and separate SUBMIT. Generalize the same flow to missions 03–08.
- [ ] Keep RUN and SUBMIT separate; a successful RUN alone never completes an operation.
- [x] Add criteria-based debrief output grouped into Intelligence, Evidence, Technical, and Judgment dimensions only when active; Stealth / Exposure is omitted from missions that do not use it.
- [ ] Test multiple valid approaches, incomplete evidence, responsible uncertainty, and repeat submission idempotency.

## Phase 3 — First Contact vertical slice

Preserve all eight approved learning missions and their existing learning order.

- [x] Build the opening recruitment interaction sequence with sequential, interruption-safe local progress.
- [x] Author the canonical eight-mission First Contact sequence with stable IDs, briefings, learning concepts, semantic outcomes, and explicit uncertainty in the final report.
- [x] Add the first playable First Contact mission slice: seeded read-only message fragment, Room discovery state, mission workspace starter script, Editor RUN, source-hash receipt check, deterministic SUBMIT evaluation, and saved completion.
- [x] Add the second playable mission, Identity Check: official Recruitment Office record, distinct read-only evidence, named-variable extraction starter script, preselected mission editor path, output/evidence requirements, and persisted campaign completion.
- [x] Add the third playable mission, System Trace Audit: read-only gateway log, two authored ERROR entries, a `for`/`if` starter script, exact `ERROR_COUNT=2` output requirement, evidence attachment, and ordered campaign completion.
- [x] Add the fourth playable mission, Record Inventory: read-only five-record dataset, `len()` starter script, exact `RECORD_COUNT=5` output requirement, evidence attachment, and ordered campaign completion.
- [x] Add the fifth playable mission, Reference Trace: read-only ordered catalog, `enumerate()` filtering, preserved zero-based source index, exact RX-14 output requirement, evidence attachment, and ordered campaign completion.
- [x] Add the sixth playable mission, Two-Source Comparison: paired A.E.G.I.S./ARCHIVE receipt datasets, list indexing, exact reference/time comparison output, both evidence attachments, and ordered completion.
- [x] Add the seventh playable mission, Time Normalization: convert timestamp components with `int()`, calculate a signed delta, and require the verified `SIGNED_DELTA=4` result.
- [x] Add the eighth playable mission, Delta Validation: calculate the absolute difference with `abs()`, require `ABSOLUTE_DELTA=4`, and separately submit the responsible-uncertainty objective.
- [x] Add Evidence Archive controls for player-authored evidence relationships (hypothesis → explicit confirmation or removal) and investigative claims with distinct proposed/supported/disputed/insufficient states; persist through the Room graph repository.
- [x] Add the evidence timeline and two-record source-comparison workspace in Analysis, plus a claim and relationship register based on persisted operation data.
- [ ] Complete the Evidence Graph investigation UI with a visual graph, richer claim editing, authored narrative debriefs, and campaign continuity beyond technical submissions.
- [x] Add the opening Freya notification interaction and unlock NEXUS only after the player taps its link.
- [x] Add a campaign-gated Messenger inbox with separate Recruitment Office, Freya, and NEXUS conversation threads; contacts appear only after their authored introduction/unlock milestone.
- [x] Persist per-message Messenger read receipts locally and show per-thread unread counts in the inbox; opening a thread records its authored messages as read.
- [ ] Expand Messenger with a full authored conversation history, player dialogue choices, and character relationship continuity.
- [x] Add the offline Intranet bulletin workstation with authored recruitment, RX-14, and evidence-handling notices plus direct navigation to Operations, Database, and Messenger.
- [x] Add the Dossier workstation with evidence/annotation/claim summaries and explicit distinction between confirmed links and hypotheses.
- [x] Add the Analysis workstation with a discovered-evidence timeline, two-record metadata comparison, and persisted claim/relationship registers.
- [x] Add a Mission Control selector that derives COMPLETE / AVAILABLE / LOCKED state from persisted ordered progression, permits review of completed missions, and seals the final report until all eight are complete.
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
- [x] Integrate the Chaquopy Python runtime with Editor RUN feedback and a separate Mission 01 SUBMIT evaluation flow; extend this integration across the remaining missions.
- [ ] Enforce authorization and resource limits; document that in-process Python is not a hardened security sandbox.

## Phase 5 — Campaign continuity and expanded routes

- [x] Add a deterministic ordered mission-progression domain: only the next authored mission unlocks, completion is idempotent, unknown missions are rejected, and restored saves are sanitized to a contiguous completed prefix. JVM tests cover locking, unlocking, gaps, unknown IDs, and repeated completion.
- [x] Connect Mission 01 and Mission 02 acceptance to the ordered campaign progression and persist completions through an offline SharedPreferences repository; save failure does not falsely mark a mission complete.
- [x] Migrate the previous Mission 01 boolean completion save into the ordered campaign snapshot without discarding an existing player's completed milestone.
- [x] Award Recruit rank once after both recruitment verification missions (01 and 02); derive the displayed rank from persisted ordered mission progress so later missions do not repeat the award.
- [ ] Persist meaningful decisions, claims, reports, character knowledge, trust conditions, rank, clearance, and unlocks.
- [ ] Add alternate accepted approaches to selected operations and test each route.
- [ ] Add authored operational consequences with fair warnings and recovery routes.
- [ ] Ensure characters only know information they received or independently discovered.
- [ ] Add archive/replay behavior without duplicate rewards.

## Phase 6 — Polish and release readiness

- [ ] Complete Android portrait usability, IME, touch-target, text-scaling, TalkBack, and reduced-motion checks.
- [ ] Add regression tests for process death, rotation/configuration change, and offline behavior.
- [x] Verify the combined Android CI workflow runs applicable automated source tests, then automatically builds exactly one installable Debug APK from each successful push to `main`, using the automatically generated debug signing key. Pull requests run checks only; do not create a separate unsigned Release artifact or a separate manual APK workflow. Verified on `main` commit `f71a5e998fecb85ccae08395e8d52802e907c857` (run `43166415654`).
- [x] Validate APK archive integrity, package, versionCode, and ABI metadata; record the SHA-256 checksum. The downloaded artifact archive passed `unzip -t`; the APK checksum matched `3dc0e42358d95fe9931adc5baea43968fb6138bfe2366a1a329a37e1558f3350` on commit `f71a5e998fecb85ccae08395e8d52802e907c857`.
- [ ] Install and smoke-test the Debug APK on a physical Android phone when available; report emulator and physical-device results separately. Because CI debug keys may change between ephemeral runners, uninstall an older test build before installing a differently signed candidate when required, warning that local app data may be erased.
- [ ] Keep Debug APKs strictly for development/testing. Any production or distribution signing identity requires separate explicit owner approval.
- [ ] Review original art/audio/license provenance and app permissions.
- [ ] Update project status and release notes from verified implementation only.

## Current implementation boundary

The Android app has a persisted campaign-opening sequence (official recruitment → mission activation → Freya → player-triggered NEXUS unlock), a canonical eight-mission catalog, and playable deterministic offline mission flows for Missions 01–08. The mission evidence and workspace seeds include read-only source records, while player scripts remain writable. Each mission uses Chaquopy Python execution, an operation-bound exact-source-hash RUN receipt, and a separate SUBMIT evaluation against its authored evidence and output criteria before ordered campaign completion is saved. The mission progress adapter migrates the previous Mission 01 boolean save format. Recruit rank is derived from completion of Missions 01 and 02 and displayed dynamically in the main menu. The Terminal is a bounded VFS-only interface; the Editor supports personal/mission workspaces, exact-buffer saves, and RUN. The Database route exposes discovered evidence with provenance, separately persisted private notes, player-authored relationship hypotheses with explicit confirm/remove actions, and investigative claims with evidence links and distinct assessment states. The Room graph repository persists these records transactionally. A campaign-gated Messenger exposes separate Recruitment Office, Freya, and NEXUS threads after their story unlocks. Per-message read receipts and per-thread unread counts persist locally; authored conversation history and meaningful dialogue choices remain incomplete. All eight mission submissions produce criteria-based debriefs, and the chapter report is gated by actual final-mission completion. The full visual graph, richer claim editing, authored narrative debriefs, persistent message/read history, dialogue choices, and broader campaign consequences remain incomplete. The Intranet, Dossier, Analysis, and ordered Mission Control selector now have working offline routes; these are focused first slices, not yet complete department-scale applications. Security hardening, UI/instrumentation coverage, and physical-device testing also remain incomplete.

## Engineering rules

1. Domain rules do not live inside Compose composables.
2. UI state is a projection of domain/repository state, not the source of truth.
3. Stable IDs and provenance are preserved across imports, copies, derived artifacts, and archives.
4. Source evidence is not mutated by player annotations or hypotheses.
5. Confirmed facts, player hypotheses, and unresolved questions remain distinct in both model and UI.
6. All core campaign behavior is deterministic and offline-capable.
7. Tests must cover domain rules before UI polish.
8. A checked item means implemented and reviewed; a build/test item additionally requires a recorded successful run.
