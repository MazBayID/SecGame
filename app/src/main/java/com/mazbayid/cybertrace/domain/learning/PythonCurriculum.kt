package com.mazbayid.cybertrace.domain.learning

/**
 * Curriculum metadata is kept outside the UI so story, progression, and lesson
 * screens can consume the same ordered source of truth.
 */
data class CurriculumChapter(
    val id: String,
    val title: String,
    val focus: String,
    val narrativeTheme: String,
    val prerequisiteChapterIds: Set<String> = emptySet(),
    val learningObjectives: List<String>
) {
    fun accessRequirement(): LessonRequirement = LessonRequirement(
        id = id,
        prerequisiteIds = prerequisiteChapterIds
    )
}

data class LessonStep(
    val heading: String,
    val explanation: String,
    val exampleCode: String? = null,
    val expectedOutput: String? = null
)

data class LearningLesson(
    val id: String,
    val chapterId: String,
    val title: String,
    val storyIntroduction: String,
    val steps: List<LessonStep>,
    val practicePrompt: String,
    val starterCode: String,
    val expectedPracticeOutput: String
)

/**
 * Canonical beginner-to-advanced Python curriculum for CYBER//TRACE.
 *
 * Chapter identifiers and prerequisite order are stable save-data identifiers;
 * do not rename them after player progress has shipped.
 */
object PythonCurriculum {
    val chapters: List<CurriculumChapter> = listOf(
        CurriculumChapter(
            id = "00",
            title = "The Beginning",
            focus = "Programming fundamentals, Python, interpreter, syntax, output, and basic errors",
            narrativeTheme = "A signal reaches the player's old computer",
            learningObjectives = listOf(
                "Explain what a program is in simple terms",
                "Recognize Python code and the role of an interpreter",
                "Run a small Python instruction and read its output",
                "Recognize that syntax errors mean the code's writing rules were not followed"
            )
        ),
        CurriculumChapter(
            id = "01",
            title = "First Contact",
            focus = "Variables and data types",
            narrativeTheme = "The player records the first identity clues",
            prerequisiteChapterIds = setOf("00"),
            learningObjectives = listOf("Create variables", "Recognize strings, integers, floats, and booleans")
        ),
        CurriculumChapter(
            id = "02",
            title = "Data Identity",
            focus = "Operators and expressions",
            narrativeTheme = "The player compares and transforms collected data",
            prerequisiteChapterIds = setOf("01"),
            learningObjectives = listOf("Use arithmetic operators", "Evaluate expressions and operator precedence")
        ),
        CurriculumChapter(
            id = "03",
            title = "Conditional Access",
            focus = "Conditions, comparisons, and Boolean logic",
            narrativeTheme = "A virtual access gate applies decision rules",
            prerequisiteChapterIds = setOf("02"),
            learningObjectives = listOf("Write if, elif, and else branches", "Combine comparisons with Boolean operators")
        ),
        CurriculumChapter(
            id = "04",
            title = "Endless Process",
            focus = "while, for, range, and loop control",
            narrativeTheme = "The player automates repetitive system checks",
            prerequisiteChapterIds = setOf("03"),
            learningObjectives = listOf("Repeat instructions with loops", "Control loop execution safely")
        ),
        CurriculumChapter(
            id = "05",
            title = "Data Repository",
            focus = "Lists, tuples, sets, dictionaries, and nested data",
            narrativeTheme = "Evidence is organized into structured collections",
            prerequisiteChapterIds = setOf("04"),
            learningObjectives = listOf("Choose suitable collection types", "Read and update structured collections")
        ),
        CurriculumChapter(
            id = "06",
            title = "Modular Operations",
            focus = "Functions, parameters, return values, scope, and modules",
            narrativeTheme = "The player builds reusable investigation tools",
            prerequisiteChapterIds = setOf("05"),
            learningObjectives = listOf("Define and call functions", "Use parameters and return values")
        ),
        CurriculumChapter(
            id = "07",
            title = "Digital Archives",
            focus = "File handling, paths, and exceptions",
            narrativeTheme = "The player examines files in a controlled archive",
            prerequisiteChapterIds = setOf("06"),
            learningObjectives = listOf("Read and write virtual files", "Handle expected errors with exceptions")
        ),
        CurriculumChapter(
            id = "08",
            title = "Object Architecture",
            focus = "Classes, objects, methods, and object-oriented principles",
            narrativeTheme = "The player models connected city systems",
            prerequisiteChapterIds = setOf("07"),
            learningObjectives = listOf("Create classes and objects", "Use attributes and methods")
        ),
        CurriculumChapter(
            id = "09",
            title = "Hacker Toolkit",
            focus = "Useful Python standard-library modules",
            narrativeTheme = "The player assembles reliable tools from Python's library",
            prerequisiteChapterIds = setOf("08"),
            learningObjectives = listOf("Import standard-library modules", "Choose existing tools instead of rebuilding them")
        ),
        CurriculumChapter(
            id = "10",
            title = "Network Frontier",
            focus = "IP, ports, client/server, HTTP, APIs, sockets, and network errors",
            narrativeTheme = "The player maps the fictional NEXUS network",
            prerequisiteChapterIds = setOf("09"),
            learningObjectives = listOf("Explain basic network communication", "Use game-controlled network APIs")
        ),
        CurriculumChapter(
            id = "11",
            title = "Data Vault",
            focus = "SQL, SQLite, CRUD, relations, and transactions",
            narrativeTheme = "The player investigates a simulated records database",
            prerequisiteChapterIds = setOf("10"),
            learningObjectives = listOf("Query and modify SQLite data", "Understand CRUD and transactions")
        ),
        CurriculumChapter(
            id = "12",
            title = "System Integrity",
            focus = "Tracebacks, debugging, assertions, unit tests, and refactoring",
            narrativeTheme = "The player verifies tools before trusting their findings",
            prerequisiteChapterIds = setOf("11"),
            learningObjectives = listOf("Trace and fix program errors", "Write tests and improve existing code")
        ),
        CurriculumChapter(
            id = "13",
            title = "Beyond the Interpreter",
            focus = "Comprehensions, iterators, generators, decorators, context managers, and async",
            narrativeTheme = "The player studies advanced system behavior",
            prerequisiteChapterIds = setOf("12"),
            learningObjectives = listOf("Use advanced Python constructs", "Reason about iteration and asynchronous work")
        ),
        CurriculumChapter(
            id = "14",
            title = "The Final Architecture",
            focus = "Integrated project using multiple Python concepts",
            narrativeTheme = "The player builds a complete investigative system",
            prerequisiteChapterIds = setOf("13"),
            learningObjectives = listOf("Design an integrated Python project", "Explain and verify the program's behavior")
        )
    )

    val firstLesson = LearningLesson(
        id = "00.01",
        chapterId = "00",
        title = "A Program Is a Set of Instructions",
        storyIntroduction = "An unknown signal appears on your old computer. ARIA asks you to make the terminal display a short message so you can confirm that the computer can follow an instruction.",
        steps = listOf(
            LessonStep(
                heading = "What is a program?",
                explanation = "A program is a set of instructions that tells a computer what to do. The computer follows those instructions in the order the program runs."
            ),
            LessonStep(
                heading = "Python and the interpreter",
                explanation = "Python is a programming language. An interpreter is a program that reads Python instructions and runs them. In this game, your code will be sent to a real Python interpreter."
            ),
            LessonStep(
                heading = "Your first instruction",
                explanation = "print() tells Python to display a value. The parentheses hold the value that print should display. Quotation marks make the text inside them a string.",
                exampleCode = "print(\"Signal acquired\")",
                expectedOutput = "Signal acquired"
            ),
            LessonStep(
                heading = "Code can contain mistakes",
                explanation = "Python follows writing rules called syntax. If required punctuation is missing or placed incorrectly, Python reports a syntax error instead of running that instruction."
            )
        ),
        practicePrompt = "Make the terminal display the exact message: NEXUS connection ready",
        starterCode = "print(\"NEXUS connection ready\")",
        expectedPracticeOutput = "NEXUS connection ready"
    )

    fun findChapter(id: String): CurriculumChapter? =
        chapters.firstOrNull { it.id == id }

    fun isChapterUnlocked(
        chapterId: String,
        completedChapterIds: Set<String>
    ): Boolean {
        val chapter = findChapter(chapterId) ?: return false
        return LessonAccessPolicy.isUnlocked(
            chapter.accessRequirement(),
            completedChapterIds
        )
    }

    fun hasValidPrerequisiteGraph(): Boolean {
        val knownIds = chapters.map(CurriculumChapter::id).toSet()
        if (knownIds.size != chapters.size) return false

        return chapters.all { chapter ->
            chapter.prerequisiteChapterIds.all { prerequisiteId ->
                prerequisiteId in knownIds &&
                    chapters.indexOfFirst { it.id == prerequisiteId } <
                    chapters.indexOf(chapter)
            }
        }
    }
}
