package com.mazbayid.aegisnet.data.execution

import android.content.Context
import com.chaquo.python.android.AndroidPlatform
import com.chaquo.python.Python
import com.mazbayid.aegisnet.domain.execution.ScriptExecutionEngine
import com.mazbayid.aegisnet.domain.execution.ScriptExecutionResult
import com.mazbayid.aegisnet.domain.editor.EditorRunSnapshot
import com.mazbayid.aegisnet.domain.vfs.VfsEntryInfo
import com.mazbayid.aegisnet.domain.vfs.VfsResult
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem
import org.json.JSONObject

/** Narrow bridge: Python scripts can only access the active game VFS. */
class AegisVfsBridge(
    private val fileSystem: VirtualFileSystem
) {
    fun read_text(path: String): String =
        when (val result = fileSystem.cat(path)) {
            is VfsResult.Success -> {
                require(result.value.length <= MAX_READ_CHARS) { "Virtual file exceeds the read limit." }
                result.value
            }
            is VfsResult.Failure -> throw IllegalArgumentException("${result.code}: ${result.message}")
        }

    fun write_text(path: String, content: String): String {
        require(content.length <= MAX_WRITE_CHARS) { "Virtual file write limit exceeded." }
        return when (val result = fileSystem.writeFile(path, content)) {
            is VfsResult.Success -> result.value.path
            is VfsResult.Failure -> throw IllegalArgumentException("${result.code}: ${result.message}")
        }
    }

    @JvmOverloads
    fun list_files(path: String = "."): List<String> =
        when (val result = fileSystem.ls(path)) {
            is VfsResult.Success -> result.value.take(MAX_LIST_ITEMS).map { entry ->
                if (entry.isDirectory) "${entry.name}/" else entry.name
            }
            is VfsResult.Failure -> throw IllegalArgumentException("${result.code}: ${result.message}")
        }

    private companion object {
        const val MAX_READ_CHARS = 65_536
        const val MAX_WRITE_CHARS = 16_384
        const val MAX_LIST_ITEMS = 100
    }
}

/**
 * Chaquopy adapter for the in-process Python teaching runtime.
 * This is not a hardened sandbox; never execute untrusted network-delivered code.
 */
class ChaquopyScriptExecutionEngine(
    context: Context
) : ScriptExecutionEngine {

    private val applicationContext = context.applicationContext

    override fun execute(
        snapshot: EditorRunSnapshot,
        fileSystem: VirtualFileSystem
    ): ScriptExecutionResult {
        if (snapshot.operationId != fileSystem.operationId) {
            return ScriptExecutionResult(
                exitCode = -1,
                stderr = "RUN snapshot belongs to a different operation."
            )
        }
        return try {
            ensurePythonStarted()
            synchronized(PYTHON_EXECUTION_LOCK) {
                val result = Python.getInstance()
                    .getModule("aegis_runtime")
                    .callAttr("run_script", snapshot.source, AegisVfsBridge(fileSystem))
                    .toJava(String::class.java)
                val payload = JSONObject(result)
                ScriptExecutionResult(
                    exitCode = payload.getInt("exit_code"),
                    stdout = payload.optString("stdout"),
                    stderr = payload.optString("stderr"),
                    timedOut = payload.optBoolean("timed_out")
                )
            }
        } catch (error: Exception) {
            ScriptExecutionResult(
                exitCode = -1,
                stderr = error.message ?: "Embedded Python runtime failed to start."
            )
        }
    }

    private fun ensurePythonStarted() {
        if (Python.isStarted()) return
        synchronized(PYTHON_START_LOCK) {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(applicationContext))
            }
        }
    }

    private companion object {
        val PYTHON_START_LOCK = Any()
        val PYTHON_EXECUTION_LOCK = Any()
    }
}
