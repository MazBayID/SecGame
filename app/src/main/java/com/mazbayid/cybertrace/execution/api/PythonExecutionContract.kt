package com.mazbayid.cybertrace.execution.api

import java.nio.charset.StandardCharsets

/**
 * Resource limits requested for one player-code execution.
 *
 * These values are part of the execution contract, not a security boundary by themselves.
 * The concrete runtime must enforce them outside the game's main process.
 */
data class PythonExecutionLimits(
    val timeoutMillis: Long = 3_000,
    val memoryLimitMiB: Int = 64,
    val maxOutputBytes: Int = 16_384,
    val maxSourceBytes: Int = 16_384
) {
    init {
        require(timeoutMillis in MIN_TIMEOUT_MILLIS..MAX_TIMEOUT_MILLIS) {
            "Execution timeout must be between " + MIN_TIMEOUT_MILLIS + " and " + MAX_TIMEOUT_MILLIS + " ms."
        }
        require(memoryLimitMiB in MIN_MEMORY_MIB..MAX_MEMORY_MIB) {
            "Memory limit must be between " + MIN_MEMORY_MIB + " and " + MAX_MEMORY_MIB + " MiB."
        }
        require(maxOutputBytes in MIN_OUTPUT_BYTES..MAX_OUTPUT_BYTES) {
            "Output limit must be between " + MIN_OUTPUT_BYTES + " and " + MAX_OUTPUT_BYTES + " bytes."
        }
        require(maxSourceBytes in MIN_SOURCE_BYTES..MAX_SOURCE_BYTES) {
            "Source limit must be between " + MIN_SOURCE_BYTES + " and " + MAX_SOURCE_BYTES + " bytes."
        }
    }

    companion object {
        const val MIN_TIMEOUT_MILLIS = 100L
        const val MAX_TIMEOUT_MILLIS = 10_000L
        const val MIN_MEMORY_MIB = 16
        const val MAX_MEMORY_MIB = 256
        const val MIN_OUTPUT_BYTES = 1_024
        const val MAX_OUTPUT_BYTES = 65_536
        const val MIN_SOURCE_BYTES = 1
        const val MAX_SOURCE_BYTES = 32_768
    }
}

/**
 * A bounded request for the isolated Python runtime.
 *
 * workspaceId identifies a game-owned virtual workspace. It must never be interpreted
 * as a host filesystem path.
 */
data class PythonExecutionRequest(
    val source: String,
    val workspaceId: String,
    val limits: PythonExecutionLimits = PythonExecutionLimits()
) {
    init {
        require(workspaceId.isNotBlank()) { "Workspace ID must not be blank." }

        val sourceSize = source.toByteArray(StandardCharsets.UTF_8).size
        require(sourceSize <= limits.maxSourceBytes) {
            "Python source exceeds the configured " + limits.maxSourceBytes + "-byte limit."
        }
    }
}

enum class PythonExecutionStatus {
    COMPLETED,
    SYNTAX_ERROR,
    RUNTIME_ERROR,
    TIMED_OUT,
    OUTPUT_LIMIT,
    RESOURCE_LIMIT,
    CANCELLED,
    WORKER_CRASHED
}

enum class PythonDiagnosticKind {
    SYNTAX,
    RUNTIME,
    TIMEOUT,
    OUTPUT_LIMIT,
    RESOURCE_LIMIT,
    INTERNAL
}

/** Structured diagnostic returned by the runtime; it is not parsed from UI text. */
data class PythonDiagnostic(
    val kind: PythonDiagnosticKind,
    val message: String,
    val line: Int? = null,
    val column: Int? = null
) {
    init {
        require(message.isNotBlank()) { "Diagnostic message must not be blank." }
        require(line == null || line > 0) { "Diagnostic line must be positive." }
        require(column == null || column > 0) { "Diagnostic column must be positive." }
    }
}

/**
 * Terminal result for one execution.
 *
 * The runtime adapter must cap stdout/stderr before constructing this result and set
 * outputTruncated when it discards output beyond the configured limit.
 */
data class PythonExecutionResult(
    val status: PythonExecutionStatus,
    val stdout: String = "",
    val stderr: String = "",
    val durationMillis: Long,
    val diagnostic: PythonDiagnostic? = null,
    val outputTruncated: Boolean = false
) {
    init {
        require(durationMillis >= 0) { "Execution duration cannot be negative." }
    }
}

/**
 * Handle for a running execution. Cancellation is best-effort; the implementation must
 * still guarantee that the worker is eventually stopped and cannot affect later runs.
 */
interface PythonExecutionHandle {
    fun cancel()
}

/**
 * Narrow asynchronous boundary between the game and the selected Python runtime.
 *
 * Implementations must invoke onResult exactly once for a terminal outcome. Player code
 * must never execute in the Android application's main process.
 */
interface PythonExecutionEngine {
    fun execute(
        request: PythonExecutionRequest,
        onResult: (PythonExecutionResult) -> Unit
    ): PythonExecutionHandle
}
