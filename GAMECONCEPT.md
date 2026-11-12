# GAMECONCEPT.md

# CYBER//TRACE
## Game Concept & Gameplay Design

> Status: Initial concept document  
> Working title: CYBER//TRACE  
> Genre: Story-driven Cyberpunk Hacking Simulator · Python Educational RPG  
> Primary learning language: Python  
> Presentation: Text-based screens, terminal interfaces, cyberpunk UI, and terminal animations  
> Audience: People who have never programmed before

---

## 1. High Concept

CYBER//TRACE is a story-driven cyberpunk hacking simulator designed to teach Python from absolute beginner level to advanced topics.

The player enters a dystopian digital city as an ordinary person with no coding experience. Through story missions, investigations, jobs, and virtual hacking operations, the player gradually learns to use Python as a practical tool.

The game combines two experiences:

1. **Cyberpunk adventure:** explore locations, meet characters, investigate mysteries, accept missions, build relationships, earn money, improve equipment, and uncover a larger conspiracy.
2. **Real programming education:** learn concepts in a carefully ordered curriculum, write actual Python code, execute it through an interpreter, understand errors, and build increasingly capable programs.

Python is not a decorative minigame or fictional command language. It is the central tool the player learns and uses to solve problems in the game world.

## 2. Design Pillars

### Learn Python by using Python
Players write and run real Python code in an in-game editor and terminal. Challenges teach transferable programming skills rather than game-specific commands.

### Story gives learning a purpose
Every major learning milestone has a narrative reason. The player learns variables to organize information, conditions to make decisions, loops to automate repetitive work, and networking to communicate with virtual systems.

### Beginner-first education
The intended player may not know what coding, a program, a terminal, an interpreter, a variable, or an error is. The game introduces these ideas in plain language before expecting the player to use them.

No mission may require an unintroduced concept without first teaching it or providing an explicit learning path.

### Hacking simulation, not real-world intrusion
All hacking takes place inside fictional, controlled game systems. Files, services, networks, databases, and devices are simulated by the game. Attacking real systems is never required.

### Progress through understanding
Character level, money, reputation, and equipment are separate from Python mastery. Better equipment may improve convenience or unlock facilities, but it never replaces the player's ability to understand and write code.

### Immersive terminal presentation
The interface uses terminal windows, a code editor, system logs, network panels, status indicators, scan effects, typing effects, progress bars, and restrained glitch effects. Animations must not make code difficult to read or edit.

---

## 3. Player Experience

The player begins as an ordinary resident of NEXUS CITY with an old computer and no programming knowledge.

A mysterious message introduces the player to an underground network. A technical mentor helps the player understand the computer and learn Python. Small tasks gradually become investigations into suspicious data changes and the powerful HELIX CORPORATION.

The intended emotional progression:

- **Beginning:** “I do not know how to write a program.”
- **Middle:** “I understand how this system works, and I can build tools to investigate it.”
- **End:** “I can design my own program to solve a complex problem.”

The player should feel that they are becoming capable, not merely unlocking scripted abilities.

## 4. World and Setting

### NEXUS CITY

The story takes place in NEXUS CITY in 2089. It is a cyberpunk metropolis where everyday life depends on connected digital infrastructure.

Digital identity, financial transactions, employment records, public services, transport, communication, and archives are managed through interconnected systems.

### HELIX CORPORATION

HELIX CORPORATION is the dominant technology company responsible for the central NEXUS infrastructure. It publicly presents NEXUS as a secure and efficient platform, while underground groups question how the system uses and controls information.

### The Underground Network

The Underground Network is a loose community of independent programmers, information brokers, technicians, and people seeking access to information outside corporate control.

### World Locations

The world is represented through navigable screens, text descriptions, menus, maps, documents, dialogue, and interactive systems. A 3D world or animated character movement is not required.

- **Underground District:** the player's starting neighborhood and first safehouse.
- **Digital Market:** a hub for equipment, software, information, and services.
- **Industrial Zone:** facilities containing automation systems and connected devices.
- **Corporate Sector:** HELIX offices and more restricted infrastructure.
- **Restricted Network Zone:** hidden virtual locations tied to the central mystery.

Each location can contain NPCs, jobs, clues, documents, devices, virtual computers, and access restrictions.

---

## 5. Main Gameplay Systems

### Exploration
Players select and visit locations, inspect objects and systems, discover documents, find contacts, and unlock new areas. Exploration is based on meaningful choices and information rather than physical movement.

### Missions and Jobs
The mission system connects narrative, education, and simulation. Missions may ask players to investigate a problem, write a program, analyze virtual data, automate a task, repair a simulated process, or make a consequential decision.

Mission categories:

- **Main Story:** advances the central narrative.
- **Side Jobs:** optional work offered by NPCs.
- **Training:** introduces or reinforces Python concepts.
- **Investigation:** gathers and connects clues.
- **System Operations:** solves technical problems in virtual environments.
- **Development:** builds reusable software tools.
- **Mastery Challenges:** tests independent understanding.

### Hacking Simulation
Hacking is a multi-step problem-solving activity, not a single button press.

1. **Reconnaissance:** gather information about a virtual target.
2. **System analysis:** inspect its files, services, data, or structure.
3. **Problem understanding:** identify what needs to be solved.
4. **Programming:** write or adapt Python code.
5. **Execution:** run the program in the controlled environment.
6. **Verification:** inspect output and confirm the objective.
7. **Trace analysis:** review logs and understand the operation's result.

Operation types include data extraction from fictional files, access-rule analysis, log analysis, virtual network analysis, automation, database operations, and system diagnostics.

### Python Workspace

The player's computer gradually provides integrated tools:

| Tool | Purpose |
|---|---|
| Terminal | Run commands and Python programs |
| Code Editor | Write, edit, and save Python files |
| File System | Browse and manage virtual files |
| Network Interface | Inspect the fictional network |
| Database Interface | Work with simulated databases |
| System Monitor | View processes, events, and logs |
| Mission Board | Review objectives and jobs |
| Profile | View character and learning progression |

Not every tool is available at the start. New facilities are introduced through missions and learning milestones.

### Real Python Execution

The in-game editor and terminal execute actual Python through a real interpreter. A successful solution is based on program behavior and mission requirements, not merely matching a predefined text answer.

The runtime must be isolated from the host device. File access, network access, execution time, memory, and other resources must be restricted to the game's controlled environment.

### Character Progression

Character progression may include character level and experience, underground reputation, money and resources, equipment and software, storage or workstation upgrades, access privileges, and NPC relationships.

### Learning Progression

Learning progression is tracked separately from character progression. It records the player's understanding and practical capability across Python topics. A high character level does not automatically mean Python mastery.

### NPCs and Relationships

NPCs have goals, knowledge, loyalties, secrets, and relationships. Player choices may affect trust, available work, information, prices, access, and story branches.

---

## 6. Python Curriculum and Chapter Structure

The curriculum is the educational backbone of the game. Topics are introduced in a sensible dependency order, with practice and review before advanced concepts are expected.

| Chapter | Learning focus | Narrative theme |
|---|---|---|
| 00 | Programming fundamentals, Python, interpreter, syntax, output, basic errors | The Beginning |
| 01 | Variables and data types | First Contact |
| 02 | Operators and expressions | Data Identity |
| 03 | Conditions, comparisons, and Boolean logic | Conditional Access |
| 04 | while, for, range, and loop control | Endless Process |
| 05 | Lists, tuples, sets, dictionaries, nested data | Data Repository |
| 06 | Functions, parameters, return values, scope, modules | Modular Operations |
| 07 | File handling, paths, exceptions | Digital Archives |
| 08 | Classes, objects, methods, OOP principles | Object Architecture |
| 09 | Useful Python standard-library modules | Hacker Toolkit |
| 10 | IP, ports, client/server, HTTP, APIs, sockets, network errors | Network Frontier |
| 11 | SQL, SQLite, CRUD, relations, transactions | Data Vault |
| 12 | Tracebacks, debugging, assertions, unit testing, refactoring | System Integrity |
| 13 | Comprehensions, iterators, generators, decorators, context managers, async and other advanced topics | Beyond the Interpreter |
| 14 | Integrated project using multiple Python concepts | The Final Architecture |

A chapter contains multiple missions, exercises, optional jobs, and mastery challenges. A chapter is not reduced to a single quiz.

### Standard Chapter Flow

1. **Story Introduction:** a narrative problem creates the need to learn.
2. **Concept Discovery:** a new programming concept is explained in beginner-friendly language.
3. **Demonstration:** the player sees and runs a small working example.
4. **Guided Practice:** the player modifies or completes code with support.
5. **Independent Task:** the player solves a related problem with fewer hints.
6. **Mission Application:** the concept is used in a story or hacking-simulation task.
7. **Mastery Challenge:** a changed scenario tests whether the player can transfer the concept.
8. **Chapter Conclusion:** results are explained and the next opportunity is introduced.

---

## 7. Beginner Learning and Assistance System

### Absolute Beginner Onboarding

The game assumes no prior coding knowledge. Early onboarding explains, demonstrates, and lets the player interact with:

- What coding and programming mean.
- What a program is.
- What Python is.
- What a terminal does.
- What a code editor does.
- What an interpreter does.
- What a .py file is.
- What output and errors mean.
- How to run, edit, and save a simple program.

Syntax punctuation is explained when introduced, including quotes, parentheses, commas, colons, indentation, and assignment symbols.

### NLAS — NEO Learning Assistance System

NLAS is the working name for the in-game learning companion. It remains available throughout the game, not only during the opening tutorial.

Four assistance levels are proposed:

1. **Concept Explanation:** explains the relevant idea in plain language.
2. **Guided Assistance:** breaks the task into manageable steps and explains why each step matters.
3. **Code Example:** presents a relevant example and explains each part.
4. **Full Walkthrough:** demonstrates the complete process from understanding the task to executing and checking a solution.

Players may request help without being shamed or blocked. The system encourages learning while allowing players to choose how much guidance they need.

### Code Explanation

When a new construct appears, NLAS explains what each meaningful part does. For example, it can explain a variable name, assignment, string literal, function call, and the difference between referring to a variable and writing literal text.

### Error Assistance

Error feedback identifies the kind of problem and explains it in beginner-friendly language. Examples include syntax errors, name errors, type errors, index errors, runtime errors, and logic errors.

The assistant guides the player toward diagnosis rather than always replacing the player's code with a finished answer.

### Adaptive Assistance

- New learners receive detailed explanations and guided practice.
- Developing learners receive focused hints.
- Experienced learners receive more independent challenges.
- Players who struggle can revisit prerequisite explanations and exercises.

Assistance remains available on demand.

### Fair Learning Rules

No learning mission should require a concept that has not been introduced or made accessible through an explicit teaching path. Players should not be punished for needing help. Assessment values understanding and transfer to new situations, not speed alone.

---

## 8. Mission Design

Missions should have a narrative reason, a clear objective, relevant learning goals, and verifiable completion criteria.

A mission specification should include:

- Mission title and story context.
- Characters and location.
- What the player knows at the start.
- Required Python concepts.
- New concepts, if any, and how they are taught.
- Available virtual tools and data.
- Objectives and constraints.
- Optional hints and assistance.
- Expected program behavior.
- Success and failure feedback.
- Rewards and story consequences.
- A follow-up task or changed scenario when useful.

### Example: Ghost in the Directory

A contact has lost track of an important record on a virtual computer. The player is asked to create a simple program that displays information in the requested format.

The mission introduces variables, strings, and output. It begins with a concept explanation and demonstration, continues with guided practice, and ends with an independent variation.

The mission checks whether the program runs and produces the required output. A changed record can then test whether the player understands how to adapt the code.

---

## 9. Challenge, Failure, and Tension

There is **no combat system**. Tension comes from investigation, technical uncertainty, resource constraints, time-sensitive work, relationships, and consequences.

| Challenge | Gameplay consequence |
|---|---|
| Syntax error | Program cannot be parsed or run |
| Runtime error | Program stops during execution |
| Logic error | Program runs but produces an incorrect result |
| Limited resources | Player manages virtual equipment or resources |
| Time-sensitive mission | Player works within a narrative deadline |
| Incomplete information | Player investigates before choosing a solution |
| Trust consequences | Decisions affect NPC relationships |
| System trace | Virtual operations may create logs or attract attention in the fictional world |

Failures should teach. They should not erase educational progress or make experimentation feel unsafe.

---

## 10. Story and Narrative Integration

The central story concerns information, identity, corporate control, and a hidden intelligence within the NEXUS infrastructure.

The main narrative is described in STORYLINE.md. The story makes Python useful in context: the player learns programming because it enables investigation, analysis, automation, and software development that move the story forward.

Main-story missions, side jobs, NPC relationships, optional discoveries, and player decisions reinforce the world without interrupting the learning sequence.

---

## 11. Terminal and Cyberpunk UI Direction

The presentation is screen- and text-based, with terminal interfaces as the visual identity.

Proposed effects:

- Text streaming and typing effects.
- Command prompt and terminal output.
- Scrolling system logs.
- Progress bars and loading sequences.
- Scan and analysis indicators.
- Status transitions.
- Restrained scanline and glitch effects.
- Network and system status panels.

Visual effects must never obscure code, make text unreadable, or interfere with editing. The code editor prioritizes legibility and comfortable input.

The game does not depend on animated characters, combat animations, or a 3D environment. Static visual elements may support the atmosphere, but the core experience is built around screens, text, and interactive tools.

---

## 12. Economy, Equipment, and Access

Money and equipment provide optional progression and convenience.

Potential uses include workstation and storage upgrades, virtual software and tools, access to facilities and information services, specialized environments, and resources needed for certain jobs.

Equipment must not automatically solve programming missions. The player still needs to understand the relevant concepts and produce working solutions.

---

## 13. Endings and Player Decisions

The ending may depend on evidence, relationships, decisions, and how the player responds to competing interests around NEXUS.

Initial ending directions:

- **Open Network:** expose corporate manipulation and make information more transparent.
- **Silent Control:** cooperate with HELIX and preserve centralized control.
- **Digital Liberation:** trust ECHO and help establish an alternative digital system.
- **Ghost Protocol:** uncover PHANTOM's hidden agenda and disappear with the evidence.

These are narrative directions, not finalized scripts. Their exact requirements and consequences will be developed with the full story.

---

## 14. Technical and Production Principles

1. Execute actual Python code using an interpreter.
2. Isolate player code in a restricted runtime.
3. Keep virtual files, networks, databases, and devices inside the game environment.
4. Evaluate behavior and mission objectives rather than simple text matching.
5. Separate character progression from learning mastery.
6. Keep the UI readable and suitable for text input.
7. Build a small playable prototype before expanding the world.
8. Keep the platform and implementation technology open until production requirements are evaluated.

### Suggested Development Phases

**Phase 1 — Playable Foundation**
- Terminal and code editor.
- Actual Python execution in an isolated environment.
- Basic file handling.
- First location and NPC.
- Beginner onboarding and first mission.

**Phase 2 — Core Gameplay**
- Mission and dialogue systems.
- NLAS assistance.
- Error diagnostics.
- Learning progression.
- Character profile, inventory, and economy.

**Phase 3 — Hacking Simulation**
- Virtual file system expansion.
- Network and database interfaces.
- System monitor and log analysis.
- Reusable player-built tools.

**Phase 4 — World and Story**
- Multiple districts.
- NPC relationships and organizations.
- Main story and side jobs.
- Reputation and story consequences.

**Phase 5 — Advanced Learning and Endgame**
- Advanced Python chapters.
- Integrated projects.
- Complex multi-system missions.
- Final story and mastery assessment.

---

## 15. Core Design Promise

CYBER//TRACE is for a player who may begin with absolutely no programming experience.

The game makes that player feel welcome, teaches each concept in a sensible order, provides meaningful help, and gradually gives them the confidence to solve problems independently.

**The player enters the world because of the cyberpunk story. They continue because of the missions. They leave with real Python skills.**
