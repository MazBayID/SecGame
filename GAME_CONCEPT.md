# AEGIS-NET — GAME CONCEPT

> Primary design reference for AEGIS-Net. Migrated and revised from the earlier project concept · Prepared: November 12, 2026.  
> The narrative outline lives in [Story_Line.md](Story_Line.md). This file is the product and technical canon. The repository's Markdown documents are a coordinated specification set: this file governs product identity, canon, safety, and technical constraints; `GAMEPLAY_DESIGN.md` governs gameplay systems; `UI_ARCHITECTURE.md` governs interface boundaries and behavior; `Story_Line.md` governs narrative details within canon; `IMPLEMENTATION_ROADMAP.md` governs delivery order and verified status; `README.md` is an entry point and status summary, not an independent source of requirements. Owner's latest explicitly approved direction overrides older conflicting rules, and affected documents must then be reconciled.  
> **[OPEN]** = not yet decided, not canon. In-game strings that are canon in Indonesian are kept in Indonesian with an English gloss.

## 1. Short Summary

AEGIS-Net is an Android Adventure RPG about an ordinary person
recruited into A.E.G.I.S., a fictional intelligence organization
headquartered in Manila. The player investigates fictional incidents,
examines evidence, and learns the terminal and Python from zero
through missions.

Programming is part of the world and the gameplay. It is NOT a
programming course in a game costume. Every lesson must have a
believable purpose inside the story.

## 2. Product Identity

- Title        : AEGIS-Net
- Genre        : Adventure RPG (cinematic story RPG x tactical
  intelligence simulator)
- Platform     : Android-first, Kotlin + Jetpack Compose
- Perspective  : first-person. The player is the operative; there is
  no player avatar or portrait.
- Art style    : cinematic, semi-realistic, anime-inspired, mature
  futuristic atmosphere.
- Quality bar  : cohesion and polish comparable to premium/AAA games
  (a quality direction, not a claim about budget).

**What "RPG" means here:**

- NOT a sword-fighting or action game. RPG depth comes from
  characters, dialogue, choices, skills, rank, and clearance.
- Tension comes from mysteries, intelligence risks, hard decisions,
  and time-sensitive operations. Any physical action must serve the
  investigative story.

**Two halves that reinforce each other:**

- Cinematic Story RPG: recruitment, characters, documents,
  investigation.
- Tactical Intelligence Simulator: terminal, code editor, simulated
  evidence, databases, agent communication.

## 3. The World: A.E.G.I.S.

- Name   : Advanced Espionage & Global Intelligence Service
- Status : independent international intelligence organization
  (fictional)
- HQ     : Manila, Philippines. Fictional founding year: 2008.
- Motto  : "Knowledge is Power. Intelligence is Responsibility."
- Fictional cases: transnational corruption, money laundering,
  disinformation, digital espionage, illicit tech infrastructure.
- Not omniscient and not always right: uncertainty, conflicting
  interests, and internal secrets exist.

**Six divisions:**

```
 01 Intelligence   - collects, verifies, analyzes information
 02 Cyber Ops      - digital security, controlled technical
                     simulations
 03 Investigation  - evidence, timelines, relationships
 04 Technical      - devices, software, infrastructure
 05 Field Ops      - fictional field assignments, teams, logistics
 06 Command        - strategy, authorization, resources
```

HQ areas: Central Command, Intelligence Room, Cyber Lab, Evidence
Archive, Agent Quarters, Training Facility, Secure Communication.
Access to divisions, tools, and files is earned through story, rank,
clearance, and demonstrated competence.

## 4. Characters and Communication

### Freya Knox — primary human mentor

- Woman; approved look direction: youthful and energetic. Exact age,
  face, clothing, detailed history [OPEN].
- Role: primary teacher of terminal and Python from zero; guide
  through the whole campaign.
- Personality inspired by Power (Chainsaw Man), without copying:
  highly intelligent, confident to the point of ridiculousness,
  boastful, mischievous, expressive.
- Humor is a teaching method (funny analogies, playful challenges).
  Teasing is affectionate; it never shames the player or trivializes
  victims and real danger.
- Genuinely competent: when things get dangerous she turns focused
  and professional at once.
- First impression is professional; her goofy side emerges gradually.
- She secretly issued the recruitment order (SECRET, see section 5).

### NEXUS — AI communication and technical-support system

- A system, not a human. NOT the primary teacher.
- Duties: system alerts, operation status, error diagnostics,
  references, secondary hints. It does not solve missions.
- Text-only. Icon: neutral human silhouette. No face portrait.
- The whole core campaign works offline with no live AI service.

Specialists: when an operation needs expertise outside Freya's field,
Freya introduces a specialist through a handoff in chat. The
specialist helps temporarily; Freya stays the primary mentor.
Specialist names and details [OPEN].

Dialogue: a chat interface (not a visual-novel dialogue box), no face
portraits. Player replies are predefined written options; selected
responses may update authored character knowledge, trust conditions,
access, or later scenes when the choice is explicitly meaningful.
Do not add choices that pretend to matter but have no consequence.
No free-text dialogue in the initial version. Core dialogue and
continuity are deterministic and offline; optional AI-assisted
dialogue through an API may be added later but must never be required.

## 5. Campaign 01 — The Recruitment / Chapter 001 — First Contact

### 5.1 Opening (two separate notifications; never merge them)

1. Black screen, a pause, then an ordinary-looking "Pesan Aman" (Secure Message) notification from A.E.G.I.S. / Recruitment Office. The player must tap it; it opens that official conversation directly (not Freya, not a generic inbox). Core line: "Seseorang telah memberikan perintah kepada A.E.G.I.S. untuk merekrutmu." (Someone has ordered A.E.G.I.S. to recruit you.) The identity of that person is withheld. It contains a tappable link, BUKA RANGKAIAN MISI (Open Mission Sequence), that the player must tap.

2. When the first mission becomes active, a second notification from Freya Knox appears over the mission screen. The player must tap it; it never opens automatically. Freya greets the player and introduces herself as the mentor. She must not claim, or hint, that she issued the recruitment order.

3. Freya introduces NEXUS and gives a link, "NEXUS / KANAL SISTEM" (NEXUS / System Channel). The NEXUS contact appears in the inbox only AFTER that link is tapped.

- No "Unknown Sender" prologue.
- All notifications and links are in-game simulations only: no
  Android notification permission, no real messages or contacts.
- Every meaningful tap is saved so an interruption cannot duplicate
  or skip steps.

### 5.2 Opening sequence

1 Official recruitment -> 2 The invitation -> 3 First mission activation -> 4 Freya's contact -> 5 NEXUS introduction -> 6 Technical orientation -> 7 A.E.G.I.S. Intranet access -> 8 First operation -> 9 Recruitment verification. The chapter has 14 story beats and 8 technical missions that form one continuous investigation.

### 5.3 The eight missions (all data fictional, local, read-only)

```
 1 Sinyal Pertama (First Signal)             variables, print()
 2 Verifikasi Identitas (Identity Check)     named variables, output
 3 Audit Jejak Sistem (System Trace Audit)   for, if, counting ERROR
                                             entries in a log
 4 Inventaris Rekaman (Record Inventory)     len()
 5 Penelusuran Referensi (Reference Trace)   enumerate(), filtering,
                                             source indices
 6 Perbandingan Dua Sumber (Two-Source       list indexing, comparing
   Comparison)                               two records
 7 Normalisasi Waktu (Time Normalization)    int(), indexing, signed
                                             difference
 8 Validasi Selisih (Delta Validation)       abs(), absolute difference
```

The final report records a 4-second difference between the A.E.G.I.S. and ARCHIVE receipt times. The cause is NOT established: no culprit, no tampering claim, no new case until approved. Rank Recruit is awarded at the earlier recruitment milestone; the later missions extend the chapter and do not change that event. (English glosses of mission names were added for this summary and are not canon.)

### 5.4 Narrative principles

- Story must not feel pasted onto unrelated coding questions.
- Every mission has a believable narrative reason to exist.
- Documents, messages, and evidence advance the case or the world.
- Characters keep consistent identities, motives, and voices.
- Failure gives meaningful feedback, not a generic "wrong" screen.
- Continuity is preserved across chapters; no silent new story facts.

## 6. Core Gameplay

AEGIS-Net is an **Intelligence Operations RPG**: Adventure RPG,
Investigation Simulator, and Technical Operations work as one
connected experience. The player should feel like an operative
solving consequential problems—not a student completing detached
coding exercises and not a user navigating a decorative dashboard.

The detailed, approved gameplay systems are specified in
[GAMEPLAY_DESIGN.md](GAMEPLAY_DESIGN.md). That document defines the
Intelligence Cycle, Evidence Graph, multiple solution paths, risk
and consequences, persistent world state, mission evaluation,
adaptive learning, mission design requirements, and delivery
priorities. This section summarizes the binding product rules.

**Core operation cycle — The Intelligence Cycle:**

```
 QUESTION
    ↓
 OBSERVE → INVESTIGATE → ANALYZE
                          ↓
                     PLAN APPROACH
                          ↓
                  EXECUTE / INTERACT
                          ↓
                   DECIDE / REPORT
                          ↓
                   DEBRIEF / CONSEQUENCE
                          ↓
                  WORLD & SKILL UPDATE
```

This is a flexible cycle, not a rigid checklist. Short operations
may combine stages; complex cases may return to investigation after
new evidence appears. Each operation begins with a meaningful
uncertainty or objective. Tools and apps appear only when they serve
that operation.

**Three connected gameplay layers:**

- **Adventure RPG:** first-person story, characters, dialogue,
  campaign progression, rank, clearance, and meaningful decisions.
- **Investigation Simulator:** discovery, provenance, source
  comparison, timelines, evidence relationships, hypotheses, and
  defensible conclusions.
- **Technical Operations:** in-world Terminal, controlled VFS,
  Python editor/runtime, and other authorized simulated tools.

**Binding gameplay rules:**

- Evidence is more than a checklist. Sources have provenance and
  reliability; observations, inferences, hypotheses, and confirmed
  facts must remain distinguishable. The Evidence Graph visualizes
  relationships without inventing or automatically solving them.
- Missions define objectives, constraints, and acceptable proof,
  not one mandatory click or command sequence. Multiple deliberately
  authored and tested approaches may be accepted. Python is required
  only when it is a meaningful mission objective or tool.
- Important decisions may change later opportunities, evidence,
  character knowledge/trust conditions, or story scenes. Choices
  must not pretend to matter when they do not. The world state is
  persistent, deterministic, testable, and works offline.
- Consequences are signposted when reasonably knowable. A failed
  RUN, syntax error, or unmet submission criterion is a learning
  failure: it does not remove XP, rank, clearance, or story progress.
  Preserve working context and provide actionable recovery. Story
  decisions may have consequences, but must not create arbitrary
  unwinnable dead ends.
- Mission evaluation can assess Intelligence, Evidence, Technical,
  Stealth/Exposure, and Judgment where relevant. Each mission defines
  its active dimensions and deterministic criteria; no universal
  score formula is required. Technical correctness and investigative
  reasoning are evaluated separately.
- Responsible uncertainty is rewarded. A cautious conclusion
  supported by evidence is better than an unsupported accusation.
  Multiple defensible conclusions may be accepted.
- Difficulty adapts through authored explanations, optional
  graduated hints, practice, and reduced scaffolding after mastery.
  It must not unpredictably change mission rules or rely on a live
  AI model.

**RUN and SUBMIT always stay separate:**

- RUN saves and executes the exact current script through the
  controlled runtime and VFS, then displays actual output/errors in
  Terminal. A successful run record is tied to the operation and
  exact source version/hash.
- SUBMIT occurs through Mission Control. The domain layer rechecks
  that the submitted source matches a successful RUN, then evaluates
  the authored mission criteria and any required evidence/report.
- RUN alone never completes a mission. A successful script does not
  automatically mean the intelligence conclusion is correct.

**Failure, retry, and support:**

- Explain which requirement was not met and why.
- The player can correct the work and submit again. Technical retry
  does not reduce XP, rank, access, or story progress; preserve
  working context.
- Graduated hints: concept reminder -> syntax cue -> more specific
  direction. A complete solution is not the default.
- Consequences from deliberate story/operational decisions are
  distinct from technical retry penalties and follow the authored
  mission state rules.

Mission-to-mission clues: each mission may carry a short code/syntax
or investigative clue toward the next (surfaced in the debrief),
matched to the player's level and never a blocker.

Campaign navigation: menu -> A.E.G.I.S. secure-connection transition
-> campaign title, status, progress. Continue and New Campaign are
offered. Chapters and missions are revealed progressively.

## 7. Learning: Freya Teaches From Zero

The player never needs to have used a terminal or written code.

**Principles:**

- Assume zero knowledge; explain terms (command, folder, path,
  variable, string, loop, error) in plain language before use.
- Teach purpose and meaning, not only the text to type.
- One new idea at a time. The player types and runs things; the game
  never silently executes everything.
- Order: guided practice -> small independent task -> real mission.
- Tutorials and missions use the SAME files, interfaces, and
  execution rules. No mock screens.
- Never tell the player to use a file, command, or feature that is
  not supported. Optional accelerated path for experienced players.
- Progress is measured by concepts actually used, not screens seen.
  Not typing speed, not memorization.

Terminal sequence: what a terminal is -> prompt and location -> files,
folders, paths -> pwd, ls -> cd -> cat -> creating practice files ->
editor, save, run -> help and recovering from mistakes. Practice
files are clearly distinct from mission evidence.

Python sequence: what a script is -> print() -> strings -> variables
and "=" -> input/output -> reading syntax errors -> if/else -> lists
and zero-based indexing -> for, range(), enumerate() -> len() ->
dictionaries, functions, files when missions need them.

Other tracks (SQL, networking, C++, IoT, automation) open
progressively when the story gives a reason. The first release
focuses on a complete, polished Python experience; other tracks must
be real systems, not decorative locked cards.

Three learning areas: LEARN (optional reference), PRACTICE (safe
exercises), MISSION (applied in the story).

## 8. Interface and Applications

**Detailed UI implementation blueprint:** see [UI_ARCHITECTURE.md](UI_ARCHITECTURE.md). It translates the approved in-game interface direction and Hacknet reference patterns into Android/Jetpack Compose screen boundaries, navigation, state ownership, reusable components, interaction contracts, and an implementation sequence. GAME_CONCEPT.md remains the product/canon source of truth.


**In-game UI architecture reference — Hacknet (research-backed, original implementation required):**

Hacknet is used as a reference for interaction patterns and system boundaries, not as code or asset material to copy. Inspection of publicly accessible decompiled C# source identified these useful architectural patterns:

- **Screen lifecycle and orchestration:** `ScreenManager` coordinates a stack/list of screens; `GameScreen` provides screen lifecycle, focus, popup behavior, and transition state. AEGIS-Net should express the same separation with Compose navigation destinations, screen-scoped ViewModels, explicit UI state, and transition policies.
- **OS workspace composition:** `DisplayModule` and related modules represent focused parts of the simulated operating-system workspace. AEGIS-Net should compose its in-game workspace from independent application surfaces (Terminal, Messenger, Intranet, Database, Dossier) rather than one monolithic screen.
- **Terminal as a dedicated interaction surface:** Hacknet's `Terminal` owns command-line presentation concerns such as input, history, prompt, and output rendering. In AEGIS-Net, keep those concerns in the Terminal feature UI while parsing, command dispatch, virtual filesystem access, and Python execution remain in separate engine interfaces.
- **Reusable interface primitives:** the inspected GUI layer includes buttons, text boxes, scrollable panels, text regions, and lists. AEGIS-Net should build a reusable Compose design system for terminal panels, message cards, file rows, tabs, status indicators, dialogs, and scrollable evidence views.
- **Centralized themes:** `ThemeManager` and `OSTheme` show a centralized theme concept. AEGIS-Net should use semantic color and typography tokens in one design system; the approved black-green palette must apply consistently across in-game apps.
- **Visual feedback and transitions:** screen transitions, focus states, command feedback, scroll regions, and subtle animated backgrounds contribute to the feeling of operating a coherent fictional system. Use these ideas selectively and preserve accessibility and performance.

**Android translation rule:** Hacknet is built with C# / XNA and its rendering model must not be ported literally. Recreate the underlying UX principles with Kotlin, Jetpack Compose, Navigation, ViewModels, unidirectional state flow, and reusable composables. Do not copy decompiled source, proprietary assets, exact layouts, or distinctive artwork. All AEGIS-Net code and visual assets must be original.

Research references: [OpenHacknet decompiled source](https://github.com/cbluoss/OpenHacknet), [Hacknet Labyrinths decompiled source](https://github.com/bizkut/Hacknet), and [community Hacknet documentation](https://hacknet.wiki/).

**Main menu (a game feel, not an app dashboard):**

- Destinations: Run Campaign, Profile, Settings, Language, Archives,
  Achievements.
- Navigation style takes inspiration from classic Java-era mobile
  games by Gameloft / Glu / EA Mobile (e.g. the old Assassin's Creed
  mobile titles), adapted to modern touch. This applies to the menu
  only, not to every screen.
- Background: a cinematic view of the A.E.G.I.S. headquarters as a
  closed, mysterious complex (not an office tower). Use the approved
  black-green hacker identity. The player is not the focus. The
  menu must stay readable. Architecture, camera, animation [OPEN].

App Hub: a small button in the upper-right corner opens a
smartphone-style panel with 7 applications, unlocked progressively
through story/rank:

```
 1 Secure Messenger     2 A.E.G.I.S. Browser / Intranet
 3 Terminal             4 Intelligence Database
 5 Dossier              6 Analysis
 7 Operations
```
The Code Editor is part of the active operation workspace (not a hub
icon). Opening and closing apps never loses operation context.

Secure Messenger: feels like a familiar smartphone chat app with
A.E.G.I.S. branding (not a WhatsApp copy). Inbox with previews,
timestamps, unread markers, search. Clear incoming/outgoing bubbles.

**Tappable link and attachment cards. Contacts appear progressively:**

Recruitment Office, then Freya, then NEXUS.

Browser / Intranet: a fictional internal A.E.G.I.S. website, fully
offline (not an internet browser). Contains login, Mission Control,
File Repository, Intelligence Reports, Operations Archive, Agent
Profile. Fictional credentials: RECRUIT-014 / FIRSTCONTACT. First
file: `message_fragment.txt` (content: ACCESS GRANTED). The Messenger
attachment and the Repository download point to ONE shared asset.

Code Editor: where code is written. Progressive Editor: starts
simple; features (highlighting, line numbers, auto-indent, etc.) grow
with progress without blocking valid code.

Terminal: a real execution environment inside the game (not a mock,
not the Android shell/Termux). Standard Linux-style commands (pwd,
ls, cd, cat, mkdir, ...) only where actually supported and tested.
Hybrid input: type directly + assistance (help, suggestions,
autocomplete). Clear errors; never silently substitutes a command.

Virtual file system: managed by the game; never touches Android
storage. Two areas:

- Personal Workspace: persistent, the player's own (scripts, notes,
  tools).
- Mission Workspace: per operation (evidence, datasets, reports),
  provenance preserved, read-only or read-write, archived after the
  mission.
Paths are validated (anti-traversal); Python only accesses permitted
files through explicit interfaces.

## 9. Player Progression

Ranks (9): Civilian, Recruit, Initiate, Junior Analyst, Field
Analyst, Cyber Operative, Senior Operative, Special Agent, Elite
Operative.
Rank and clearance unlock divisions, areas, tools, learning tracks,
characters, responsibilities, achievements, and profile entries.
XP, skills, rank, clearance, completed missions, achievements, and
story flags must be coherent and persistent. Rewards must never be
granted twice on load or replay.
Archives hold dossiers, evidence, reports, and case history.
Rank thresholds, XP economy, clearance model [OPEN].

## 10. Visual and Audio

**Core palette — Hacker Black & Green:**

```
 Deep Black      #050807  primary background
 Carbon          #0B120E  panels and surfaces
 Dark Terminal   #10241A  elevated surfaces / terminal panels
 Signal Green    #39FF88  primary emphasis, active states, key actions
 Matrix Green    #1F6B45  secondary accents, borders, selected surfaces
 Soft Mint       #A7FFC8  primary readable text
 Muted Green     #648A73  secondary text, inactive metadata
```
Green is used deliberately for hierarchy, status, and interaction—not
as a flood of neon across every screen. Maintain readable contrast and
support reduced-motion settings. Exact token values may be tuned after
on-device accessibility and OLED readability tests. The black-green
hacker identity replaces the previous crimson/obsidian palette and
provisional dark-cyan menu direction.

Rules: strong hierarchy, readable text, consistent spacing;
distinguish story scenes, operation screens, and settings inside one
design system; avoid clutter and generic stock-like visuals.

"Adventure RPG" direction: an adventure with momentum, discoveries,
and a sense of place; cinematic transitions, varied composition,
meaningful animation (respect reduced-motion). Different scenes
(quiet conversation, urgent alert, classified briefing, debrief) must
not all look alike. Audio is configurable and never required to
understand the story. Clarity of controls and accessibility come
before effects.

Entry loading animation: 240 frames (PNG/WebP), 1080x1920 portrait,
10 seconds, 24 fps. Played during loading before the main menu.
Preserve aspect ratio; do not stretch or crop. Never decode all
frames into memory at once. Packaging format and skip controls
[OPEN].

## 11. Language

Two required languages: Bahasa Indonesia and English, selected in
Settings (and optionally on first launch). All player-facing content
is localized (menus, dialogue, missions, hints, terminal help,
errors, debriefs, accessibility). Story data is kept separate from UI
code. Changing language never resets progress. The choice persists
locally. Code syntax and technical command names are not translated.
The English must read naturally and preserve Freya's voice.

## 12. Technical

- Android-first, Kotlin + Jetpack Compose. Python 3.13 through
  Chaquopy (per README), running locally on the device.
- Offline-first: menu, story, dialogue, missions, evaluation,
  progression, code execution, and saving all work without internet,
  an account, or an API key. Online features (optional AI, cloud
  backup, downloadable content) are only extras and must fail
  gracefully.
- Modules: UI/Presentation, Story Engine, Campaign Engine, Learning
  Engine, Practice Engine, Mission Engine, Code Runner, Evaluator,
  Progression, Player Data, Archives/Evidence, Communication.
  Content, presentation, evaluation logic, and persistence are kept
  separate; avoid one giant Compose screen. Code Runner sits behind
  an interface.
- Saving: campaign progress autosaves at meaningful checkpoints
  (beat completed, mission-stage change, successful submit, debrief,
  XP/rank/unlock changes). Player code is NOT saved on every
  keystroke. The explicit Save action persists the current editor buffer; RUN also atomically saves an immutable snapshot of the exact buffer it executes, so unsaved edits cannot be silently omitted from execution. Mission checkpoints persist operation progress and the last saved script state. Show saved/unsaved state and the last saved time. Everything resumes after the app is reopened.
- Initial Python execution limits: 3-second wall-clock budget and
  50,000 line-event budget, plus limits on source/AST/input/output/
  collections/file writes; all configurable and covered by automated
  tests.
- WARNING: Chaquopy runs in the same process. AST restrictions and
  quotas are defense-in-depth only, NOT process isolation and not a
  hardened sandbox. Do not market it as one or accept arbitrary
  third-party scripts.

## 13. Safety and Fictional Scope

- A.E.G.I.S., its members, cases, companies, and targets are all
  fictional. Make no allegations about real people or organizations.
- Technical missions use fictional data, controlled simulations, or
  explicitly authorized systems. Do not teach unauthorized access,
  credential theft, malware, surveillance, or attacks on real
  systems. Teach defensive, analytical, authorized skills.
- Early "decryption" is only a transparent toy transformation; never
  imply a beginner is breaking modern cryptography.
- Code execution must use validation and resource limits. Chaquopy runs in the application process; these controls are defense-in-depth only, not process isolation or a hardened sandbox. This is the same limitation stated in section 12.

## 14. Build and Release (Single APK)

- One candidate commit produces ONE APK: build once -> test that
  exact APK -> release that same APK (verify SHA-256). No separate
  debug APK, and no rebuilding after testing.
- Order: run all applicable source tests for technologies present in the candidate (Kotlin tests are mandatory; Python tests become mandatory when Python source/runtime is introduced) -> build the single signed Release APK -> validate signature, package, versionCode, and ABI metadata -> smoke-test that exact APK on an emulator -> physical-device acceptance using the same APK -> publish. A failed or skipped applicable check means no release. A test suite that does not yet apply must be explicitly reported as not applicable, never reported as passed.
- One permanent keystore for all releases, kept only in GitHub
  Secrets and the owner's offline backup. Never committed or printed
  to logs.
- applicationId stays fixed; versionCode always increases. Updates
  must install over the old version without uninstalling and must
  not reset local data. Save migrations must be explicit and tested.
- main is the only long-lived branch. Final acceptance is still done
  on a physical device with the same APK.

## 15. Development Rules

Before any significant task: inspect the current repository and read all relevant Markdown specifications, not only one named document. Reconcile product canon, gameplay, UI, narrative, roadmap, and README status before changing implementation. A code change must follow the currently approved specification; do not retroactively change requirements merely to justify existing code.

- No generic dashboards; no visible player avatar or agent face
  portraits; NEXUS stays text with a neutral silhouette.
- Keep RUN separate from SUBMIT. All technical targets are fictional
  or authorized.
- Do not add features just to raise the count. A screen is not proof a
  feature is done; record true status.
- Test where feasible; distinguish tests run from tests not run.
  Verify build status before claiming success. Never force-push.
- Do not promote unapproved ideas to canon. If there is a design gap,
  update the concept document FIRST.

**Decision hierarchy when requirements conflict:**

```
1. Owner's latest explicitly approved direction
2. GAME_CONCEPT.md: product identity, canon, safety, and technical constraints
3. The relevant specialist document within its domain:
   - GAMEPLAY_DESIGN.md: gameplay rules and evaluation
   - UI_ARCHITECTURE.md: UI responsibilities and interaction contracts
   - Story_Line.md: narrative events and character continuity
4. IMPLEMENTATION_ROADMAP.md: delivery order/status; it cannot weaken requirements
5. README.md: project entry point/status only; it cannot create competing rules
6. Existing tested behavior, then technical convenience
```
If two specialist documents conflict, the relevant domain owner above decides; update the other document in the same documentation change. Existing code or tests do not override an approved requirement.
Approved strategy: Full Rebuild. Authoritative documents
(GAME_CONCEPT, README, Story_Line) and owner-supplied assets must not
be deleted.

## 16. Current Project Status

- The `main` branch contains the initial Android application shell built with Kotlin and Jetpack Compose: app entry point, semantic theme, Main Menu prototype, local system-status panel, campaign-entry placeholder, seven-tile App Hub prototype, tactical-grid background, and reusable panel primitive.
- This is an early foundation, not a playable game. The Evidence Graph currently has domain records, structural validation, in-memory editing/search operations, and JVM unit tests; persistence and player-facing graph/timeline UI are not implemented. Campaign persistence, story progression, Messenger, Intranet routing, functional Terminal, VFS, Python runtime, mission evaluation, and persistent decision consequences are also not implemented.
- The approved gameplay systems are documented in `GAMEPLAY_DESIGN.md`; implementation must be delivered as verified vertical slices rather than empty screen placeholders.
- Do not infer build, test, or release success from the presence of source files. Report only checks actually run against the relevant commit and APK.

## 17. Not Yet Decided [OPEN]

- Full chapter-by-chapter story and detailed dialogue scripts.
- Other named human characters, specialists, A.E.G.I.S. leadership
  and internal politics.
- Mission content and evidence chains beyond First Contact; the
  next case.
- Final menu and operation UI wireframes; HQ architecture and camera.
- NEXUS dialogue model and online/offline boundaries.
- Rank thresholds, XP economy, clearance model, replay rules.
- Audio direction, further accessibility, cinematic asset pipeline.
- Final technical architecture and Android performance strategy;
  the boundary between persistent and temporary workspaces;
  process isolation for the runner.

## Appendix: Notes on the Original Document's Structure

**Problems found in the source document's structure:**

- Reversed numbering: 1.2 before 1.1; 6.5 before 6.4; 10.4 before
  10.3.
- Two sections numbered "8.1" (Hybrid Mission Evaluation and
  Teaching and evaluation principles).
- Section 18 (Full Rebuild) sits in the middle of section 16, with
  16.12 and 16.13 appearing after it. There is no section 17
  (16 jumps to 18).
- "7.1" uses a "#" heading level (equal to a chapter) instead of "##".
- Sections 19-24 were added later as "updates" and override or refine
  earlier sections. Example: section 1 still lists the genre as
  "Cinematic Story RPG x Tactical Intelligence Simulator", while
  section 19 makes Adventure RPG the primary genre. The same
  information is also scattered: Freya and NEXUS in 5.3, 5.4, 6.5,
  23.1; RUN vs SUBMIT in 7, 8.1, 8.2, 16.1, 16.5, 16.8, 23.1;
  Messenger and the hub in 3.2, 3.3, 21, 22.
- Section 13 "Existing Project Continuity" is outdated.

**Proposed new order (same as this file):**

```
 1 Summary   2 Product identity   3 The world: A.E.G.I.S.
 4 Characters and communication   5 Campaign 01   6 Core gameplay
 7 Learning   8 Interface and applications   9 Progression
 10 Visual and audio   11 Language   12 Technical
 13 Safety   14 Build and release   15 Development rules
 16 Status   17 Not yet decided
```