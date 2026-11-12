package com.mazbayid.aegisnet.domain.execution

import com.mazbayid.aegisnet.domain.editor.EditorDocument
import com.mazbayid.aegisnet.domain.editor.EditorDocumentService
import com.mazbayid.aegisnet.domain.editor.EditorRunSnapshot
import com.mazbayid.aegisnet.domain.mission.MissionRunReceipt
import com.mazbayid.aegisnet.domain.vfs.VfsResult
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem

data class ScriptExecutionResult(
    val exitCode: Int,
    val stdout: String = "",
    val stderr: String = "",
    val timedOut: Boolean = false
) {
    val successful: Boolean get() = exitCode == 0 && !timedOut
}

/**
 * Runtime boundary. Implementations must execute snapshot.source exactly,
 * never reread a possibly newer editor buffer or host filesystem file.
 */
fun interface ScriptExecutionEngine {
    fun execute(snapshot: EditorRunSnapshot, fileSystem: VirtualFileSystem): ScriptExecutionResult
}

data class RunAttempt(
    val snapshot: EditorRunSnapshot,
    val execution: ScriptExecutionResult,
    val receipt: MissionRunReceipt
)

sealed interface RunCoordinatorResult {
    data class Executed(val attempt: RunAttempt) : RunCoordinatorResult
    data class Rejected(val code: String, val message: String) : RunCoordinatorResult
}

/**
 * Coordinates exact-buffer save -> immutable source snapshot -> runtime call.
 * This is orchestration only; a real Python engine must be supplied separately.
 */
class RunCoordinator(
    private val operationId: String,
    private val fileSystem: VirtualFileSystem,
    private val engine: ScriptExecutionEngine
) {

    fun run(document: EditorDocument): RunCoordinatorResult {
        val prepared = EditorDocumentService.prepareRun(fileSystem, operationId, document)
        if (prepared is VfsResult.Failure) {
            return RunCoordinatorResult.Rejected(prepared.code, prepared.message)
        }

        val snapshot = (prepared as VfsResult.Success).value
        val execution = try {
            engine.execute(snapshot, fileSystem)
        } catch (error: Exception) {
            ScriptExecutionResult(
                exitCode = -1,
                stderr = error.message ?: "Execution engine failed unexpectedly."
            )
        }

        return RunCoordinatorResult.Executed(
            RunAttempt(
                snapshot = snapshot,
                execution = execution,
                receipt = MissionRunReceipt(
                    operationId = operationId,
                    sourceHash = snapshot.sourceHash,
                    successful = execution.successful
                )
            )
        )
    }
}
