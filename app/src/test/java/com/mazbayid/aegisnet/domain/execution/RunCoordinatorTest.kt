package com.mazbayid.aegisnet.domain.execution

import com.mazbayid.aegisnet.domain.editor.EditorDocument
import com.mazbayid.aegisnet.domain.editor.EditorDocumentService
import com.mazbayid.aegisnet.domain.mission.MissionRunReceipt
import com.mazbayid.aegisnet.domain.vfs.VfsAccess
import com.mazbayid.aegisnet.domain.vfs.VfsFile
import com.mazbayid.aegisnet.domain.vfs.VfsOrigin
import com.mazbayid.aegisnet.domain.vfs.VfsResult
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunCoordinatorTest {

    private fun fileSystem() = VirtualFileSystem(
        operationId = "first_contact_01",
        personalFiles = listOf(
            VfsFile("scripts/task.py", "print('old')", VfsAccess.READ_WRITE, VfsOrigin.PLAYER_CREATED)
        )
    )

    private fun document(vfs: VirtualFileSystem): EditorDocument {
        return (EditorDocumentService.open(vfs, "scripts/task.py") as VfsResult.Success).value
    }

    @Test
    fun savesAndExecutesTheExactUnsavedBufferSnapshot() {
        val vfs = fileSystem()
        val changed = EditorDocumentService.update(document(vfs), "print('latest unsaved edit')")

        var receivedSource: String? = null
        val engine = ScriptExecutionEngine { snapshot, fileSystem ->
            receivedSource = snapshot.source
            assertEquals(snapshot.source, (fileSystem.cat("scripts/task.py") as VfsResult.Success).value)
            ScriptExecutionResult(exitCode = 0, stdout = "latest unsaved edit")
        }

        val result = RunCoordinator("first_contact_01", vfs, engine).run(changed)
        assertTrue(result is RunCoordinatorResult.Executed)
        val attempt = (result as RunCoordinatorResult.Executed).attempt

        assertEquals("print('latest unsaved edit')", receivedSource)
        assertEquals(receivedSource, attempt.snapshot.source)
        assertEquals(EditorDocumentService.sha256(receivedSource!!), attempt.receipt.sourceHash)
        assertEquals(
            MissionRunReceipt("first_contact_01", attempt.snapshot.sourceHash, true, "latest unsaved edit"),
            attempt.receipt
        )
    }

    @Test
    fun failedExecutionCreatesAnUnsuccessfulReceipt() {
        val vfs = fileSystem()
        val result = RunCoordinator(
            "first_contact_01",
            vfs,
            ScriptExecutionEngine { _, _ ->
                ScriptExecutionResult(exitCode = 1, stderr = "SyntaxError")
            }
        ).run(document(vfs))

        assertTrue(result is RunCoordinatorResult.Executed)
        val attempt = (result as RunCoordinatorResult.Executed).attempt
        assertFalse(attempt.receipt.successful)
        assertEquals("SyntaxError", attempt.execution.stderr)
    }

    @Test
    fun timeoutIsNotReportedAsSuccessfulEvenWithZeroExitCode() {
        val vfs = fileSystem()
        val result = RunCoordinator(
            "first_contact_01",
            vfs,
            ScriptExecutionEngine { _, _ ->
                ScriptExecutionResult(exitCode = 0, timedOut = true)
            }
        ).run(document(vfs)) as RunCoordinatorResult.Executed

        assertFalse(result.attempt.receipt.successful)
    }

    @Test
    fun engineExceptionBecomesFailedRunInsteadOfCrashingCoordinator() {
        val vfs = fileSystem()
        val result = RunCoordinator(
            "first_contact_01",
            vfs,
            ScriptExecutionEngine { _, _ -> error("runtime unavailable") }
        ).run(document(vfs)) as RunCoordinatorResult.Executed

        assertFalse(result.attempt.receipt.successful)
        assertEquals("runtime unavailable", result.attempt.execution.stderr)
    }

    @Test
    fun readOnlyMissionSourceCannotBePreparedForExecution() {
        val vfs = VirtualFileSystem(
            operationId = "first_contact_01",
            missionFiles = listOf(
                VfsFile("source/task.py", "print('read only')", VfsAccess.READ_ONLY, VfsOrigin.MISSION_SOURCE)
            )
        )
        vfs.switchWorkspace(com.mazbayid.aegisnet.domain.vfs.VfsWorkspace.MISSION)
        val doc = documentForMission(vfs)

        val result = RunCoordinator(
            "first_contact_01",
            vfs,
            ScriptExecutionEngine { _, _ -> error("must not execute read-only source") }
        ).run(doc)

        assertTrue(result is RunCoordinatorResult.Rejected)
    }

    private fun documentForMission(vfs: VirtualFileSystem): EditorDocument =
        (EditorDocumentService.open(vfs, "source/task.py") as VfsResult.Success).value
    @Test
    fun coordinatorRejectsWorkspaceFromAnotherOperationAtConstruction() {
        val vfs = fileSystem()
        val failure = runCatching {
            RunCoordinator(
                operationId = "another_operation",
                fileSystem = vfs,
                engine = ScriptExecutionEngine { _, _ -> error("must not execute") }
            )
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

}

