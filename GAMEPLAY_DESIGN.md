# AEGIS-Net — GAMEPLAY DESIGN

> Status: approved gameplay direction · 12 November 2026  
> `GAME_CONCEPT.md` governs product identity, canon, safety, and technical constraints. This document governs gameplay systems and evaluation within that canon. `UI_ARCHITECTURE.md` governs UI responsibilities; `Story_Line.md` governs narrative continuity; `IMPLEMENTATION_ROADMAP.md` governs delivery order/status only. If wording conflicts, apply the decision hierarchy in `GAME_CONCEPT.md` and reconcile the affected documents.

## 1. Gameplay identity

AEGIS-Net is an **Intelligence Operations RPG** combining:

- **Adventure RPG:** first-person story, characters, dialogue, campaign progression, rank, clearance, and meaningful decisions.
- **Investigation Simulator:** evidence discovery, source comparison, timelines, relationships, hypotheses, and defensible conclusions.
- **Technical Operations:** an in-world workstation with a real simulated Terminal, controlled virtual file system, Python editor/runtime, and other tools introduced when the story needs them.

The player should feel like an intelligence operative solving consequential problems—not a student completing detached coding exercises and not a user navigating a decorative hacker dashboard.

Programming is one instrument in the operative's toolkit. It must solve a believable problem in the fiction. Not every mission requires code, and not every problem has one prescribed technical solution.

## 2. Core design pillars

1. **A question drives every operation.** The player begins with an uncertainty, not an arbitrary command list.
2. **Evidence before certainty.** Conclusions must be traceable to sources. Observation, inference, allegation, and confirmed fact are distinct.
3. **Agency with understandable consequences.** Players can choose approaches; important risks are signposted, and decisions can change later content.
4. **Tools have purpose.** Messenger, Intranet, Database, Dossier, Analysis, Editor, and Terminal each contribute a useful capability.
5. **Multiple defensible approaches.** Missions validate objectives and evidence, not one memorized sequence of clicks or exact code.
6. **Learning is embedded.** New concepts are introduced when useful, practiced safely, then applied in an operation.
7. **Failure teaches; it does not arbitrarily punish.** A failed run or submission gives actionable feedback and preserves working context.
8. **The world remembers.** Important discoveries, commitments, and decisions persist across operations.

## 3. Core operation loop — The Intelligence Cycle

The standard cycle is:

```text
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

### 3.1 Observe

The operation begins with a briefing, incident, message, report, alert, or character interaction. The player learns:

- What is known.
- What is unknown.
- Why the question matters.
- What authority and constraints apply.
- What the operation is expected to deliver.

Briefings should not reveal the answer in advance.

### 3.2 Investigate

The player explores relevant sources: messages, files, logs, dossiers, database records, intranet pages, and character testimony. The player can inspect, compare, bookmark, and annotate information.

Do not force every operation through every app. Only surface tools that have a reason to exist in that operation.

### 3.3 Analyze

The player distinguishes observations from interpretations, compares sources, builds a timeline, and connects evidence. The game can offer prompts and optional guidance, but should not automatically reveal the complete solution.

### 3.4 Plan and execute

The player chooses an approach within the operation's authorized fictional scope. Possible actions include requesting information, using an unlocked contact, searching or filtering records, writing a Python script, using a Terminal command, or choosing a non-code investigative route.

The game communicates meaningful risks before irreversible decisions where the fiction allows it.

### 3.5 Decide and report

The player chooses what to conclude, what evidence supports it, what remains uncertain, and what action A.E.G.I.S. should take. A good report can explicitly say that evidence is insufficient.

### 3.6 Debrief and update

The game explains what worked, what was missed, which conclusions were supported, what consequences followed, and which skills were demonstrated. It updates campaign state, archives, unlocks, and relevant character/world state.

This is a flexible cycle, not a mandatory six-step checklist. A short operation may combine stages; a complex operation may revisit investigation after new evidence appears.

## 4. Evidence system — Evidence Graph

Evidence is more than a list of files. It forms a navigable graph of information and relationships.

### 4.1 Evidence objects

Each evidence object should have, where applicable:

- Stable content ID and source/provenance.
- Type (message, file, log, record, testimony, observation, dataset, report).
- Creation/receipt time and relevant event time, kept distinct.
- Integrity and access classification.
- Whether it is original, derived, copied, or player-authored.
- Operation and mission association.
- Player annotations and discovery state.

### 4.2 Relationships

The player may connect evidence through relationships such as:

- Supports / contradicts a claim.
- Refers to the same person, account, device, event, or identifier.
- Occurred before / after another event.
- Derived from / copied from another source.
- Conflicts with another source.
- Shares a pattern with another record.

The UI can visualize these relationships as an Evidence Graph, timeline, comparison view, or dossier links. The graph must not invent relationships: confirmed links, player hypotheses, and unresolved possibilities must look different.

### 4.3 Claims and confidence

Players form claims from evidence. Each claim records supporting and conflicting sources, confidence, and unresolved gaps. The game rewards honest uncertainty; unsupported certainty is not better than a cautious, evidence-based conclusion.

Do not require players to draw a graph for every mission. The graph is a tool for cases where relationships matter.

## 5. Multiple solution paths

A mission defines **objectives, constraints, required proof, and acceptable outcomes**, not one mandatory command sequence.

For example, an information-gathering objective might be satisfied by:
- Discovering a valid source through investigation.
- Obtaining an authorized handoff from a contact.
- Finding an alternate record that independently establishes the fact.
- Writing a script to process a dataset and identify the relevant records.

These paths may differ in time, exposure, completeness, relationship effects, or downstream opportunities.

### Rules

- Each accepted path must be deliberately authored and tested.
- Equivalent solutions must satisfy the same underlying objective and proof requirements.
- The evaluator checks semantic outcomes and evidence provenance, not exact source-code text.
- A mission may require a specific skill when teaching that skill is the explicit objective. In that case, explain the learning requirement and provide practice first.
- Do not create fake freedom by presenting choices that all silently produce the same result.
- Do not require Python for a task that is more naturally solved through observation or a simple in-world tool.

## 6. Risk, failure, and consequences

### 6.1 Risk model

Operations may track relevant risks such as:
- Exposure: whether an action attracts attention inside the fictional simulation.
- Evidence integrity: whether original evidence remains preserved.
- Time pressure: whether delay changes an opportunity or target state.
- Source confidence: whether the information provider is reliable.
- Relationship impact: whether a character's trust or willingness to help changes.
- Operational scope: whether the player's action remains within granted authorization.

Use only risks that matter to the operation. Do not add a universal suspicion meter merely for decoration.

### 6.2 Fair warning

Before a consequential choice, communicate the known cost or uncertainty when the character could reasonably know it. Hidden information may exist as a mystery, but the game must not disguise an obvious irreversible penalty.

### 6.3 Failure versus consequence

- A syntax error, failed RUN, or unmet submission criterion is a learning failure. It does not remove XP, rank, clearance, or story progress. Preserve code, evidence, and context; explain how to recover.
- A deliberate story/operational decision may have consequences: a source may disengage, an opportunity may close, a report may remain incomplete, or a later scene may change.
- Consequences should create new narrative or investigative conditions, not trap the player in an unwinnable state.
- Where possible, provide recovery routes, later remediation, or an honest debrief.
- Do not equate every imperfect outcome with Game Over.

## 7. Persistent world state and character relationships

The campaign stores meaningful state across operations, including:

- Discovered evidence and archived reports.
- Claims submitted and confidence stated.
- Operation outcomes and unresolved questions.
- Important decisions and commitments.
- Character knowledge, trust conditions, and willingness to assist.
- Unlocks, rank, clearance, and demonstrated skills.

Character relationships should be expressed through authored state and behavior, not only a visible numeric affection meter. Characters may remember what the player told them, whether the player protected a source, or whether the player overstated a conclusion.

A character must not know information the player has not shared and the character has not independently discovered. State changes must be deterministic, saved, and testable. No live AI service is required for core continuity.

## 8. Mission evaluation and debrief

Evaluate the operation across five dimensions where relevant:

| Dimension | What it measures |
|---|---|
| Intelligence | Quality and accuracy of the conclusion; responsible treatment of uncertainty |
| Evidence | Relevance, integrity, provenance, and sufficiency of supporting material |
| Technical | Correct use of required tools or concepts; actual execution result |
| Stealth / Exposure | Avoidable traces or operational exposure, only when relevant |
| Judgment | Whether the chosen action respected constraints and balanced known risks |

Not every mission needs all five dimensions. The mission definition specifies which are active and how they are weighted. Avoid a universal score formula that makes every mission feel identical.

### Evaluation rules

- Technical correctness and investigative reasoning are evaluated separately, then combined for the operation result.
- Many implementations may be accepted; identical code is not required.
- Different defensible conclusions may be accepted when supported by evidence and appropriately qualified.
- Criteria and required proof are authored, deterministic, and testable. An AI model is never the authority.
- Hard-coded answers fail a mission when the stated learning objective is to demonstrate variables, loops, conditions, indexing, or another specific concept. Multiple fixtures should prevent memorized output from passing.
- The debrief explains strengths, gaps, accepted alternatives, consequences, and optional next learning steps.
- Rewards are idempotent: loading or replaying cannot grant the same progression reward twice.

## 9. Learning and adaptive difficulty

Learning follows **guided practice → independent experiment → applied operation**.

Difficulty adapts through transparent authored support, not an opaque model that changes mission rules unpredictably.

Possible adaptations:
- If a concept is new, Freya explains it and offers a small practice task.
- If the player repeatedly struggles, hints become more explicit in stages.
- If the player demonstrates mastery, repetitive scaffolding can be reduced and optional advanced approaches can be offered.
- Experienced players may take an accelerated path, but required story evidence and fair evaluation remain intact.
- The player can revisit explanations and practice without losing mission progress.

Progress is measured by demonstrated understanding and successful use, not typing speed, number of commands, or time spent.

## 10. RUN, SUBMIT, and mission integrity

The existing RUN/SUBMIT contract remains mandatory:

- RUN atomically saves an immutable snapshot of the exact current editor buffer, then executes that same snapshot through the controlled runtime and VFS. This includes unsaved edits; RUN must never execute a different or stale saved version. The Terminal displays actual output or errors. The editor's explicit Save action remains separately available for manual persistence without execution.
- A successful execution record is tied to the operation and exact source version/hash of the RUN snapshot. The editor and execution coordinator expose that identity so SUBMIT can verify it.
- SUBMIT occurs through Mission Control. The domain layer revalidates that the submitted source matches a successful RUN and then evaluates the mission's authored requirements.
- RUN alone never completes a mission.
- Investigative claims, evidence provenance, and player decisions may also be required submission artifacts; a successful script is not automatically a correct intelligence report.

## 11. Mission design template

Every operation should define:

1. **Dramatic question:** what uncertainty drives the mission?
2. **Briefing:** what the player knows, does not know, and is authorized to do.
3. **Learning objective:** what new or reinforced capability is relevant, if any.
4. **Evidence sources:** provenance, reliability, access, and possible contradictions.
5. **Primary objective and proof:** what must be established or delivered.
6. **Approaches:** at least one primary route and any deliberately supported alternatives.
7. **Risks:** which actions can affect exposure, evidence, time, or relationships.
8. **Decision points:** meaningful choices and their authored state changes.
9. **Evaluation:** active dimensions, deterministic criteria, and accepted outcomes.
10. **Recovery:** how the player can learn from errors or recover from a poor outcome.
11. **Debrief:** explanation, consequences, and next narrative/learning lead.
12. **Persistence:** exactly which world, character, archive, and progression states change.

A mission should not be considered designed until its accepted paths, evaluation fixtures, failure recovery, and state transitions are specified.

## 12. Illustrative operation (not canon story content)

**OPERATION: GHOST SIGNAL** is an example of how the systems could work; it does not establish a new canonical chapter, culprit, organization, or story event.

A suspicious data transmission has been detected. Its source and meaning are unknown.

- The briefing establishes the question and authorized scope.
- Messenger provides an initial lead.
- Intranet and Database expose separate records.
- The player compares timestamps and source provenance in Dossier/Analysis.
- A Python script may filter a large log, but a manually verified alternate record may also establish the key fact if the mission permits it.
- The player submits a conclusion, identifies uncertainty, and recommends an action.
- The debrief distinguishes what was proven from what remains unknown. The selected action changes a later opportunity or contact response.

The mission is successful when its defined intelligence objective is met—not simply when the player prints a predetermined string.

## 13. Anti-patterns to avoid

- Arbitrary command typing without an investigative purpose.
- Repeating the same linear sequence in every mission.
- Forcing Python into every problem.
- Decorative terminal output, fake telemetry, or nonfunctional app screens presented as real.
- Choices that claim to matter but have no state consequence.
- Unsignposted irreversible punishment.
- A single opaque score that hides why an operation succeeded or failed.
- Evidence graphs that auto-solve the case or present speculation as fact.
- Difficulty increases based only on longer code or more files.
- Excessive app switching that interrupts the player's understanding of the case.

## 14. Delivery priorities

Build the gameplay in verified vertical slices:

1. **First Contact foundation:** preserve the approved eight-mission learning sequence while adding clear objectives, evidence provenance, and debrief quality.
2. **Investigation tools:** evidence metadata, compare view, timeline, player annotations, and the first Evidence Graph use case.
3. **Meaningful decisions:** a small number of authored choices with persistent, testable consequences.
4. **Alternative approaches:** add and test a second valid route in a suitable operation.
5. **Risk and evaluation:** introduce only the risk dimensions required by authored missions; implement the five evaluation dimensions as configurable mission criteria.
6. **Campaign continuity:** persist character knowledge, operation outcomes, reports, and unlock state across chapters.

Do not implement all systems as empty UI placeholders. Each delivered mechanic must have domain behavior, persistence where required, tests, and an honest implementation status.
