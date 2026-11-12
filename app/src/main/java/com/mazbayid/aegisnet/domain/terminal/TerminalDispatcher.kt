package com.mazbayid.aegisnet.domain.terminal

import com.mazbayid.aegisnet.domain.vfs.VfsEntryInfo
import com.mazbayid.aegisnet.domain.vfs.VfsResult
import com.mazbayid.aegisnet.domain.vfs.VfsWorkspace
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem

data class TerminalCommand(
    val name: String,
    val arguments: List<String>
)

sealed interface TerminalParseResult {
    data class Parsed(val command: TerminalCommand) : TerminalParseResult
    data class Rejected(val code: String, val message: String) : TerminalParseResult
}

enum class TerminalOutputKind {
    NORMAL,
    ERROR,
    CLEAR
}

data class TerminalOutput(
    val kind: TerminalOutputKind,
    val text: String = ""
)

/**
 * A deliberately small command grammar. It tokenizes quoted arguments but
 * rejects shell operators, expansion, command chaining, and redirection.
 */
object TerminalCommandParser {

    fun parse(input: String): TerminalParseResult {
        if (input.isBlank()) {
            return TerminalParseResult.Rejected("empty_command", "Enter a command.")
        }
        if (input.any { it == '\n' || it == '\r' || it == '\u0000' }) {
            return TerminalParseResult.Rejected("multiple_or_invalid_lines", "Enter one command per line.")
        }

        val tokens = mutableListOf<String>()
        val token = StringBuilder()
        var quote: Char? = null
        var tokenStarted = false
        var index = 0

        while (index < input.length) {
            val character = input[index]
            when {
                quote != null && character == quote -> {
                    quote = null
                    tokenStarted = true
                }
                quote != null && character == '\\' -> {
                    if (index + 1 >= input.length) {
                        return TerminalParseResult.Rejected("invalid_escape", "A quoted escape is incomplete.")
                    }
                    val next = input[index + 1]
                    if (next != quote && next != '\\') {
                        token.append(character)
                    } else {
                        token.append(next)
                        index++
                    }
                    tokenStarted = true
                }
                quote == null && (character == '"' || character == '\'') -> {
                    quote = character
                    tokenStarted = true
                }
                quote == null && character.isWhitespace() -> {
                    if (tokenStarted) {
                        tokens += token.toString()
                        token.setLength(0)
                        tokenStarted = false
                    }
                }
                quote == null && character in SHELL_CONTROL_CHARACTERS -> {
                    return TerminalParseResult.Rejected(
                        "shell_syntax_not_supported",
                        "Shell operators, expansion, chaining, and redirection are not supported."
                    )
                }
                quote == null && character == '\\' -> {
                    return TerminalParseResult.Rejected("invalid_path_escape", "Use quoted arguments for spaces.")
                }
                else -> {
                    token.append(character)
                    tokenStarted = true
                }
            }
            index++
        }

        if (quote != null) {
            return TerminalParseResult.Rejected("unclosed_quote", "Close the quoted argument before submitting.")
        }
        if (tokenStarted) tokens += token.toString()
        if (tokens.isEmpty() || tokens.first().isBlank()) {
            return TerminalParseResult.Rejected("empty_command", "Enter a command.")
        }

        return TerminalParseResult.Parsed(
            TerminalCommand(
                name = tokens.first().lowercase(),
                arguments = tokens.drop(1)
            )
        )
    }

    private val SHELL_CONTROL_CHARACTERS = setOf(';', '|', '&', '<', '>', '`', '$')
}

/** Executes only the documented in-game commands against the supplied VFS. */
class TerminalDispatcher(
    private val fileSystem: VirtualFileSystem
) {

    fun execute(input: String): TerminalOutput {
        val parsed = TerminalCommandParser.parse(input)
        if (parsed is TerminalParseResult.Rejected) {
            return error(parsed.message)
        }

        val command = (parsed as TerminalParseResult.Parsed).command
        return when (command.name) {
            "help" -> noArguments(command) {
                normal(
                    "Available commands: help, pwd, ls [path], cd <path>, cat <path>, " +
                        "mkdir <path>, touch <path>, write <path> <content>, workspace <personal|mission>, clear."
                )
            }
            "pwd" -> noArguments(command) { normal(fileSystem.pwd()) }
            "ls" -> optionalSinglePath(command) { path ->
                mapResult(fileSystem.ls(path)) { entries -> renderEntries(entries) }
            }
            "cd" -> singlePath(command) { path ->
                mapResult(fileSystem.cd(path)) { it }
            }
            "cat" -> singlePath(command) { path ->
                mapResult(fileSystem.cat(path)) { it }
            }
            "mkdir" -> singlePath(command) { path ->
                mapResult(fileSystem.mkdir(path)) { "Directory ready: $it" }
            }
            "touch" -> singlePath(command) { path ->
                mapResult(fileSystem.touch(path)) { "File ready: ${it.path}" }
            }
            "write" -> write(command)
            "workspace" -> switchWorkspace(command)
            "clear" -> noArguments(command) { TerminalOutput(TerminalOutputKind.CLEAR) }
            else -> error("Unknown in-game command '${command.name}'. Type 'help' for available commands.")
        }
    }

    private fun write(command: TerminalCommand): TerminalOutput {
        if (command.arguments.size < 2) {
            return error("Usage: write <path> <content>")
        }
        val path = command.arguments.first()
        val content = command.arguments.drop(1).joinToString(" ")
        return mapResult(fileSystem.writeFile(path, content)) {
            "Wrote ${it.content.length} characters to ${it.path}."
        }
    }

    private fun switchWorkspace(command: TerminalCommand): TerminalOutput {
        if (command.arguments.size != 1) {
            return error("Usage: workspace <personal|mission>")
        }
        val workspace = when (command.arguments.single().lowercase()) {
            "personal" -> VfsWorkspace.PERSONAL
            "mission" -> VfsWorkspace.MISSION
            else -> return error("Workspace must be 'personal' or 'mission'.")
        }
        return normal("Workspace: ${fileSystem.switchWorkspace(workspace)}")
    }

    private fun singlePath(
        command: TerminalCommand,
        action: (String) -> TerminalOutput
    ): TerminalOutput {
        if (command.arguments.size != 1) return error("Usage: ${command.name} <path>")
        return action(command.arguments.single())
    }

    private fun optionalSinglePath(
        command: TerminalCommand,
        action: (String?) -> TerminalOutput
    ): TerminalOutput {
        if (command.arguments.size > 1) return error("Usage: ls [path]")
        return action(command.arguments.singleOrNull())
    }

    private fun noArguments(
        command: TerminalCommand,
        action: () -> TerminalOutput
    ): TerminalOutput {
        if (command.arguments.isNotEmpty()) return error("Usage: ${command.name}")
        return action()
    }

    private fun <T> mapResult(
        result: VfsResult<T>,
        render: (T) -> String
    ): TerminalOutput = when (result) {
        is VfsResult.Success -> normal(render(result.value))
        is VfsResult.Failure -> error("${result.code}: ${result.message}")
    }

    private fun renderEntries(entries: List<VfsEntryInfo>): String =
        if (entries.isEmpty()) {
            "(empty)"
        } else {
            entries.joinToString("\n") { entry ->
                if (entry.isDirectory) "${entry.name}/" else entry.name
            }
        }

    private fun normal(text: String) = TerminalOutput(TerminalOutputKind.NORMAL, text)
    private fun error(text: String) = TerminalOutput(TerminalOutputKind.ERROR, text)
}
