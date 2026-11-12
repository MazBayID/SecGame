package com.mazbayid.aegisnet.domain.terminal

import com.mazbayid.aegisnet.domain.vfs.VfsAccess
import com.mazbayid.aegisnet.domain.vfs.VfsFile
import com.mazbayid.aegisnet.domain.vfs.VfsOrigin
import com.mazbayid.aegisnet.domain.vfs.VfsWorkspace
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalDispatcherTest {

    private fun terminal() = TerminalDispatcher(
        VirtualFileSystem(
            operationId = "first_contact_01",
            missionFiles = listOf(
                VfsFile(
                    "logs/system.log",
                    "RX-14 online",
                    VfsAccess.READ_ONLY,
                    VfsOrigin.MISSION_SOURCE
                )
            )
        )
    )

    @Test
    fun parserSupportsQuotedPathsAndContent() {
        val parsed = TerminalCommandParser.parse("write \"notes/my file.txt\" \"two words\"")
        assertTrue(parsed is TerminalParseResult.Parsed)
        val command = (parsed as TerminalParseResult.Parsed).command
        assertEquals("write", command.name)
        assertEquals(listOf("notes/my file.txt", "two words"), command.arguments)
    }

    @Test
    fun parserRejectsCommandChainingPipesAndExpansion() {
        listOf(
            "pwd; cat logs/system.log",
            "cat logs/system.log | help",
            "pwd && ls",
            "cat \$(whoami)",
            "cat `id`",
            "cat file > output"
        ).forEach { input ->
            val result = TerminalCommandParser.parse(input)
            assertTrue("$input should be rejected", result is TerminalParseResult.Rejected)
        }
    }

    @Test
    fun parserRejectsUnclosedQuotesAndMultipleLines() {
        assertTrue(TerminalCommandParser.parse("cat \"unfinished").let {
            it is TerminalParseResult.Rejected && it.code == "unclosed_quote"
        })
        assertTrue(TerminalCommandParser.parse("pwd\nls").let {
            it is TerminalParseResult.Rejected && it.code == "multiple_or_invalid_lines"
        })
    }

    @Test
    fun readsMissionEvidenceButCannotOverwriteIt() {
        val terminal = terminal()
        assertTrue(terminal.execute("workspace mission").text.contains("/mission/first_contact_01"))
        assertEquals("RX-14 online", terminal.execute("cat logs/system.log").text)
    }

    @Test
    fun commandSetReadsAndWritesOnlyInsideTheGameVfs() {
        val terminal = terminal()

        assertEquals(TerminalOutputKind.NORMAL, terminal.execute("mkdir notes").kind)
        assertEquals(TerminalOutputKind.NORMAL, terminal.execute("write notes/result.txt \"signal found\"").kind)
        assertEquals("signal found", terminal.execute("cat notes/result.txt").text)
        assertEquals(TerminalOutputKind.ERROR, terminal.execute("cat /etc/passwd").kind)
        assertEquals(TerminalOutputKind.ERROR, terminal.execute("rm notes/result.txt").kind)
    }

    @Test
    fun workspaceCommandSwitchesMountsWithoutCrossAccess() {
        val terminal = terminal()

        assertTrue(terminal.execute("workspace mission").text.contains("/mission/first_contact_01"))
        assertEquals("RX-14 online", terminal.execute("cat logs/system.log").text)
        assertEquals(TerminalOutputKind.ERROR, terminal.execute("cat /personal/notes.txt").kind)

        assertTrue(terminal.execute("workspace personal").text.contains("/personal"))
    }

    @Test
    fun clearIsAnOutputInstructionAndDoesNotTouchFiles() {
        val terminal = terminal()
        terminal.execute("mkdir notes")
        val output = terminal.execute("clear")

        assertEquals(TerminalOutputKind.CLEAR, output.kind)
        assertEquals("notes/", terminal.execute("ls").text)
    }

    @Test
    fun reportsReadOnlyWriteErrorsAndUnknownCommands() {
        val terminal = terminal()
        terminal.execute("workspace mission")

        assertEquals(TerminalOutputKind.ERROR, terminal.execute("write logs/system.log \"tampered\"").kind)
        assertEquals(TerminalOutputKind.ERROR, terminal.execute("unknown-command").kind)
    }
}
