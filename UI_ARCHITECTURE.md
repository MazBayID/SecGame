# AEGIS-Net — UI ARCHITECTURE BLUEPRINT

> Status: approved architectural direction / implementation blueprint  
> Platform: Android · Kotlin · Jetpack Compose  
> Visual identity: Hacker Black & Green  
> Reference: Hacknet interaction and system-boundary patterns, reinterpreted for Android  
> This document defines how the in-game interface is organized. GAME_CONCEPT.md remains the product and canon source of truth. If a conflict appears, the owner's latest approved direction and GAME_CONCEPT.md take precedence.

## 1. Purpose and design principles

AEGIS-Net should feel like operating a coherent fictional intelligence workstation—not like navigating a generic productivity dashboard. The interface supports a first-person adventure RPG in which the player investigates, communicates, studies evidence, writes code, and submits operations.

The UI architecture follows these principles:

1. **In-world first:** menus, applications, notifications, files, and status messages belong to the fictional A.E.G.I.S. environment.
2. **Application boundaries:** Terminal, Messenger, Intranet, Database, Dossier, Analysis, and Operations are distinct application surfaces with shared visual language.
3. **Persistent operation context:** opening another application must not silently discard the active operation, editor contents, terminal history, or selected evidence.
4. **State-driven UI:** composables render state and emit user actions; gameplay rules and execution logic do not live inside composables.
5. **Offline-first:** core navigation, content, messages, missions, and saved state work without network access.
6. **Progressive disclosure:** tools and information become available through story, rank, clearance, and learning progress.
7. **Accessible restraint:** green highlights communicate hierarchy and state; they are not applied indiscriminately. Motion, contrast, touch targets, and text scaling remain usable.

## 2. Hacknet reference translated to Android

Hacknet is a reference for interaction patterns and separation of responsibilities, not a source to copy or port.

| Observed pattern | AEGIS-Net Android interpretation |
|---|---|
| ScreenManager coordinates screens and transitions | Navigation graph, app-level coordinator, screen-scoped ViewModels, explicit transition state |
| GameScreen owns lifecycle/focus/transition behavior | Lifecycle-aware screen state and navigation events; transient UI state stays scoped to its destination |
| DisplayModule separates OS workspace surfaces | Independent in-game application destinations and shared app-shell components |
| Terminal owns command-line interaction presentation | Terminal feature owns prompt, input, history, output, and scroll behavior; engine owns parsing and execution |
| ThemeManager / OSTheme centralize visual themes | Central design-system tokens, semantic colors, typography, shapes, spacing, and motion |
| Reusable GUI controls and scroll regions | Reusable Compose components for tabs, panels, file rows, message cards, dialogs, status labels, and evidence viewers |

**Important:** the inspected Hacknet repositories contain decompiled C# / XNA code. Do not copy their source, proprietary assets, exact layouts, or distinctive artwork. Implement original Kotlin and Compose code based on high-level UX principles only. The Android rendering and lifecycle model is not a literal port of XNA.

### 2.1 Workstation visual direction (binding implementation rule)

The active-operation interface must communicate the feeling of a dedicated intelligence workstation, using Hacknet as a high-level UX reference while retaining AEGIS-Net's own identity.

- **Workstation, not dashboard:** do not present the operation workspace as a stack of generic rounded Material cards. Prefer a coherent application frame, restrained separators, compact technical metadata, clear panel hierarchy, and purposeful negative space.
- **Terminal-forward hierarchy:** when an operation involves technical work, the terminal and code/evidence context must be visually prominent. The terminal is a real interaction surface, not a decorative green text panel.
- **Distinct application surfaces:** Messenger, Intranet, Terminal, Database, Dossier, Analysis, and Operations should feel like tools inside one fictional system. Each may have its own content layout, but shares the same shell, typography rules, status language, and navigation behavior.
- **Original AEGIS visual language:** use the approved black-green palette, original typography choices, original iconography, and original panel geometry. Do not reproduce Hacknet screenshots, exact window arrangements, distinctive graphics, or proprietary assets.
- **Functional density:** technical screens may show compact logs, paths, timestamps, file metadata, and status indicators where useful. Never add fake telemetry or decorative data that implies functionality that does not exist.
- **Visual effects serve information:** grid lines, scanlines, glow, cursor blink, and transitions are restrained accents. They must not reduce text contrast, conceal state, or run continuously at high cost behind dense content.

### 2.2 Mobile adaptation — preserve the workstation, not desktop limitations

AEGIS-Net is Android-first. The Hacknet reference does not require copying a desktop multi-window interaction model onto a phone.

- On portrait phones, show one primary application surface at a time inside a persistent AEGIS shell. Use tabs, a compact app switcher, contextual drawers, or stacked navigation to move between tools.
- Keep essential operation context visible or one deliberate action away: operation title/status, current file/evidence context, and unsaved editor state.
- Do not shrink several desktop-style windows until they become unreadable. A secondary panel should become a sheet, tab, or dedicated view on narrow screens.
- On tablets and sufficiently wide screens, allow a main work surface plus one supporting panel, or a carefully constrained multi-panel layout, when it improves investigation.
- Switching applications must preserve operation state and restore each app's relevant scroll/selection state where feasible.
- Touch targets, keyboard/IME behavior, system insets, text scaling, TalkBack, and reduced-motion behavior remain first-class requirements.

## 3. Application and navigation model

### 3.1 Top-level flow

```text
App Launch
  └─ Loading / local data initialization
      └─ Main Menu
          ├─ Run Campaign
          │   └─ Secure Connection Transition
          │       └─ Campaign / Chapter Overview
          │           └─ Active Operation Workspace
          ├─ Profile
          ├─ Archives
          ├─ Achievements
          ├─ Settings
          └─ Language
```

The App Hub is an overlay or secondary navigation surface available from the active game context. It is not a replacement for the campaign flow.

### 3.2 Main menu

Main menu destinations:

- Run Campaign
- Profile
- Settings
- Language
- Archives
- Achievements

The menu uses the approved cinematic A.E.G.I.S. headquarters background direction and a classic mobile-game-inspired menu composition. This menu treatment is specific to the main menu; operation screens use a functional workstation composition. The exact HQ scene, camera, and animation remain open design decisions.

### 3.3 Campaign and operation navigation

- Campaign overview communicates chapter, current operation, status, and progress.
- Chapters and missions appear progressively as story flags and access rules permit.
- Continue resumes the last valid checkpoint.
- New Campaign follows the product's approved campaign/reset policy; reset behavior must be explicit before implementation.
- An active operation has a stable operation context shared by its editor, terminal, evidence, and Mission Control.

### 3.4 App Hub

A compact control in the upper-right area opens a smartphone-style application panel containing these seven applications:

1. Secure Messenger
2. A.E.G.I.S. Browser / Intranet
3. Terminal
4. Intelligence Database
5. Dossier
6. Analysis
7. Operations

Apps unlock progressively. Locked apps should communicate their locked state without exposing unavailable content. The Code Editor is part of the active operation workspace, not a separate App Hub icon.

Opening an app must preserve the active operation context. The hub can be closed without resetting the current screen's domain state.

## 4. Screen responsibilities

### 4.1 Main Menu

**Owns:** menu navigation, campaign entry, settings/profile/archive destinations.  
**Does not own:** mission evaluation, Python execution, VFS operations, or campaign progression rules.

### 4.2 Campaign Overview

**Owns:** presentation of chapter/mission availability, campaign progress, and entry into the active operation.  
**Reads:** campaign state and story unlock state from domain-facing ViewModels/repositories.  
**Does not own:** deciding whether a mission is complete.

### 4.3 Active Operation Workspace

The operation workspace is a persistent context, not a single giant screen. It hosts or navigates between:

- Briefing / Mission Control
- Evidence and dossier views
- Code Editor
- Terminal
- Messenger
- Intranet resources relevant to the operation

The workspace must preserve the current operation identifier and unsent work when the player changes application. Domain state is retained outside composable memory so configuration changes or process recreation can restore valid checkpoints.

### 4.4 Secure Messenger

**Owns:** inbox, conversation list, conversation view, unread presentation, search, message delivery state, tappable link cards, and attachment cards.

- Contacts appear only when unlocked by story events.
- Initial contact sequence is Recruitment Office, then Freya, then NEXUS after the player taps the introduction link.
- Messenger links route to in-game destinations through a controlled internal route resolver.
- Attachments refer to shared content assets; do not create duplicate copies of the same evidence file.
- Messenger remains a fictional local simulation, not Android SMS or contacts.

### 4.5 A.E.G.I.S. Intranet Browser

**Owns:** the in-game browser frame, address/navigation presentation, local page rendering, login UI, and internal page routing.

Initial internal destinations include:

- `aegis://login`
- `aegis://mission-control`
- `aegis://repository`
- `aegis://intelligence-reports`
- `aegis://operations-archive`
- `aegis://agent-profile`

These are fictional local routes, not internet URLs. The route resolver must allow only registered internal destinations. Login and clearance checks are domain rules, not UI-only checks.

### 4.6 Terminal

**Owns:** terminal prompt, command input, command history presentation, output/error rendering, scroll position, and terminal-specific interaction state.

**Delegates to engine interfaces:** command parsing, command dispatch, authorization, VFS access, and Python execution. The UI must never implement shell commands itself.

### 4.7 Code Editor

**Owns:** editable source presentation, cursor/selection state, editor controls, explicit Save, and Run request initiation.

- Editor capabilities can grow progressively.
- Valid code must not be blocked merely because advanced editor features are locked.
- RUN and SUBMIT remain separate actions.
- RUN sends the exact saved source/version to the execution layer.
- The editor displays saved/unsaved state and the last saved time where appropriate.

### 4.8 Mission Control

**Owns:** briefing and requirement presentation, submission action, evaluation result, and debrief display.

SUBMIT is enabled only when the exact current source has completed a successful RUN, according to the authoritative execution record. Mission Control requests evaluation; it does not itself decide correctness.

### 4.9 Intelligence Database, Dossier, Analysis, Operations

These are separate application surfaces with their own ViewModels and presentation state. Their data access is permission-aware and story-aware. The precise feature scope and screen layouts are still subject to detailed feature design; do not invent operational capabilities merely to fill a screen.

## 5. Shared visual system

### 5.1 Approved palette

| Token | Hex | Intended use |
|---|---|---|
| Deep Black | `#050807` | Primary background |
| Carbon | `#0B120E` | Panels and surfaces |
| Dark Terminal | `#10241A` | Elevated surfaces and terminal panels |
| Signal Green | `#39FF88` | Primary emphasis, active states, key actions |
| Matrix Green | `#1F6B45` | Secondary accents, borders, selected surfaces |
| Soft Mint | `#A7FFC8` | Primary readable text |
| Muted Green | `#648A73` | Secondary text and inactive metadata |

These are semantic starting tokens. Validate contrast and OLED readability on a physical Android device before treating the final values as immutable.

### 5.2 Semantic color roles

Compose components should consume semantic roles rather than hard-coded hex values:

- `background`, `surface`, `surfaceElevated`
- `textPrimary`, `textSecondary`, `textDisabled`
- `accent`, `accentSecondary`
- `border`, `divider`
- `success`, `warning`, `error`, `info`

Status colors must remain distinguishable without relying on color alone; pair them with labels or icons.

### 5.3 Typography and layout

- Use a readable UI typeface for body content and controls.
- Use a restrained monospaced typeface for terminal output, code, paths, IDs, and technical metadata.
- Establish a consistent spacing scale, shape scale, and elevation policy in the design system.
- Avoid tiny text and dense all-caps paragraphs on phone screens.
- Support scrolling for long evidence, logs, and conversations.
- Respect system font scaling and display cutouts/insets.

### 5.4 Motion

Use motion to communicate meaningful state changes: secure connection, opening an app, incoming fictional messages, command execution, and mission completion. Avoid continuous high-cost effects behind text. Provide reduced-motion behavior and never use animation as the only way to communicate a state.

## 6. Reusable Compose component library

Build reusable components around repeated visual/interaction patterns, not around every individual screen.

Suggested initial components:

- `AegisScaffold` — shared safe-area and background handling
- `AegisTopBar` — title, back/navigation action, optional status
- `AppHubPanel` and `AppIconTile` — app launcher surface
- `TerminalPanel`, `TerminalPrompt`, `TerminalOutputBlock`
- `StatusIndicator` and `ClearanceBadge`
- `MessageBubble`, `MessageLinkCard`, `AttachmentCard`
- `FileRow`, `EvidenceCard`, `MetadataLine`
- `AegisTabRow`, `AegisDialog`, `AegisButton`
- `EmptyState`, `LoadingState`, `ErrorState`

Components receive data and callbacks. They must not reach directly into repositories, databases, the VFS, or Python runtime.

## 7. State ownership and lifecycle

Use unidirectional data flow:

```text
User action
   ↓
Composable callback
   ↓
ViewModel intent/action
   ↓
Use case / domain interface
   ↓
Repository or engine interface
   ↓
Result / domain event
   ↓
ViewModel UI state
   ↓
Composable renders updated state
```

Rules:

- ViewModels expose immutable UI state (for example, StateFlow) and accept explicit user actions.
- Composables are state renderers; keep business decisions out of UI code.
- Screen-local ephemeral state (for example, an open menu) may stay in Compose state.
- Durable state (mission progress, saved scripts, messages, unlocks, execution records) belongs in repositories or domain storage.
- Navigation arguments identify destinations; do not pass large mutable domain objects through routes.
- Handle lifecycle-aware collection and process recreation.
- Never rely on a composable staying alive to preserve mission state.

## 8. Proposed package and module boundaries

Keep the initial Gradle setup practical. Do not create a separate Gradle module for every screen. Start with a small number of real system boundaries and use packages for feature organization.

Illustrative source organization:

```text
app/
  navigation/
  AegisNetApp.kt

core/
  common/
  model/
  database/
  datastore/
  designsystem/
    color/
    typography/
    shape/
    component/

domain/
  campaign/
  story/
  mission/
  evaluation/
  progression/

data/
  campaign/
  content/
  player/
  communication/

engine/
  vfs/
  terminal/
    parser/
    execution/
    commands/
  python/
  security/

feature/
  menu/
  campaign/
  workspace/
  messenger/
  intranet/
  terminal/
  editor/
  missioncontrol/
  database/
  dossier/
  analysis/
  operations/
  profile/
  settings/

content/
  story/
  missions/
  evidence/
  dialogues/
  localization/
```

This is a logical boundary proposal, not a demand to create all directories immediately. Split into Gradle modules only when dependency direction, build time, ownership, or test isolation justifies it.

Dependency direction:

- UI features depend on domain-facing interfaces and the design system.
- Domain rules do not depend on Android UI or Compose.
- Data implementations satisfy repository interfaces.
- Terminal and Python engines access files only through the controlled VFS interfaces.
- The app module composes dependencies and navigation; it should not become the home of gameplay rules.

## 9. Critical interaction contracts

### 9.1 RUN and SUBMIT

RUN flow:

1. Editor requests an explicit save of the current source.
2. The execution coordinator receives the operation ID, script path, source version/hash, and permitted execution context.
3. Python execution runs through the controlled runtime/VFS bridge.
4. A successful run creates an immutable execution record tied to that exact source version/hash and operation.
5. Terminal displays the actual output or error.

SUBMIT flow:

1. Mission Control requests submission for the active operation and current script.
2. The domain layer verifies that the latest successful execution record matches the exact submitted source version/hash and operation.
3. If it does not match, submission is rejected with an actionable explanation.
4. If it matches, the evaluator checks explicit mission criteria using controlled fixtures and evidence.
5. The result and debrief are returned to the UI; progression is committed idempotently.

A UI button being enabled is not a security or correctness boundary. The domain layer must revalidate the contract.

### 9.2 Internal links and attachments

- Resolve internal links using an allowlisted route registry.
- Check story unlocks and clearance at the destination/domain boundary.
- Attachments point to shared content IDs, not duplicated file blobs.
- Unknown or locked destinations show a clear in-world response and do not crash navigation.

### 9.3 Workspace continuity

- Personal Workspace is persistent.
- Mission Workspace is operation-scoped and archived after the operation according to the VFS rules.
- App navigation must not change VFS permissions or bypass access checks.
- UI state is not the authority for file permissions or evidence provenance.

## 10. Responsive behavior and accessibility

The primary target is portrait Android phone use, but layouts should adapt to wider screens without changing gameplay rules.

- Use adaptive layout constraints rather than fixed pixel positions.
- On phones, prioritize one primary content surface at a time; use tabs, panels, and navigation surfaces where appropriate.
- On wider screens, allow a main content area with a supporting context panel where it improves investigation workflows.
- Preserve scroll position and selection when switching between related views where feasible.
- Provide touch targets suitable for mobile interaction.
- Support TalkBack semantics, clear content descriptions, focus order, text scaling, and sufficient contrast.
- Respect Android insets, keyboard/IME behavior, and back navigation.
- Reduced-motion settings must suppress or simplify nonessential transitions.

## 11. Performance and reliability

- Keep large logs, evidence lists, and message histories in lazy/virtualized lists.
- Avoid recomposing unrelated app surfaces when terminal output or message state changes.
- Do not keep multiple heavyweight editor/runtime instances alive without need.
- Avoid decoding all loading animation frames into memory at once; follow the asset streaming rule in GAME_CONCEPT.md.
- Use stable item keys for lists and stable identifiers for messages/files.
- Treat navigation restoration, save restoration, and app interruption as normal conditions.
- All core UI must remain usable offline; network availability must not gate local campaign state.

## 12. Testing strategy and acceptance criteria

### Unit tests

- ViewModel state transitions and user-action handling.
- Internal route resolution and locked/unlocked navigation.
- State restoration decisions.
- RUN/SUBMIT UI state derived from authoritative domain records.
- Error, loading, empty, and permission-denied states.

### Compose UI tests

- Main menu navigation.
- App Hub opens/closes without losing operation context.
- Messenger contact unlock sequence and link-card navigation.
- Intranet internal routes and login error states.
- Terminal input/output/history presentation.
- Editor Save and RUN actions remain distinct from Mission Control SUBMIT.
- Submission remains unavailable/rejected when the source differs from the last successful RUN.
- Font scaling, keyboard behavior, back navigation, and reduced-motion mode.

### Acceptance checklist

- [ ] UI consistently uses the black-green AEGIS-Net design system.
- [ ] Main menu feels like a game menu, not a generic dashboard.
- [ ] App Hub contains the seven approved apps and unlocks them progressively.
- [ ] Code Editor remains part of the operation workspace, not an App Hub tile.
- [ ] Switching apps does not erase active operation context.
- [ ] Messenger, Intranet, Terminal, and evidence surfaces have clear boundaries.
- [ ] UI components do not directly execute commands or access storage engines.
- [ ] RUN and SUBMIT are separate and validated by the domain layer.
- [ ] Core navigation and campaign UI work offline.
- [ ] Accessibility and physical-device readability have been checked.

## 13. Implementation sequence

Build and verify in manageable vertical slices:

1. **Foundation:** app shell, navigation, design tokens, typography, shared components, basic tests.
2. **Main menu and campaign shell:** loading, menu destinations, secure-connection transition, campaign overview.
3. **Workspace and App Hub:** persistent operation context, app switching, unlock presentation.
4. **Messenger and Intranet:** local content, contact progression, internal route registry, shared attachments.
5. **Terminal and VFS presentation:** terminal UI connected to interfaces; do not fake command results.
6. **Editor and Mission Control:** explicit save, RUN record display, separate SUBMIT path.
7. **Remaining intelligence apps:** Database, Dossier, Analysis, Operations, each backed by defined domain behavior.
8. **Polish:** accessibility, state restoration, performance, device testing, and motion refinement.

Do not mark a step complete because a screen merely renders. Completion requires working behavior, tests where applicable, and verification on the target device.

## 14. Decisions still open

This blueprint does not silently decide the following:

- Exact visual wireframes and final component dimensions.
- Final main-menu HQ composition, camera, and background animation.
- Whether the App Hub is a modal overlay, side panel, or dedicated destination after prototype testing.
- Final navigation library/version and dependency-injection choice.
- Exact Gradle module split.
- Detailed feature scope for Database, Dossier, Analysis, and Operations.
- Final adaptive layout behavior on tablets/foldables.
- Final motion timings and sound feedback.

Resolve these decisions through prototypes and tests, then update this document and GAME_CONCEPT.md where product-level canon changes.
