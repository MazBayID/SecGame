package com.mazbayid.aegisnet.domain.editor

import com.mazbayid.aegisnet.domain.vfs.VfsAccess
import com.mazbayid.aegisnet.domain.vfs.VfsFile
import com.mazbayid.aegisnet.domain.vfs.VfsOrigin
import com.mazbayid.aegisnet.domain.vfs.VfsResult
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

data class EditorDocument(
    val path: String,
    val buffer: String,
    val savedContent: String,
    val access: VfsAccess,
    val origin: VfsOrigin
) {
    val isDirty: Boolean get() = buffer != savedContent
}

data class EditorRunSnapshot(
    val operationId: String,
    val path: String,
    val source: String,
    val sourceHash: String
)

object EditorDocumentService {

    fun open(fileSystem: VirtualFileSystem, path: String): VfsResult<EditorDocument> =
        when (val result = fileSystem.readFile(path)) {
            is VfsResult.Success -> VfsResult.Success(result.value.toDocument())
            is VfsResult.Failure -> result
        }

    fun update(document: EditorDocument, newBuffer: String): EditorDocument =
        document.copy(buffer = newBuffer)

    /** Saves the exact current buffer; read-only mission sources remain protected by VFS. */
    fun save(
        fileSystem: VirtualFileSystem,
        document: EditorDocument
    ): VfsResult<EditorDocument> =
        when (val result = fileSystem.writeFile(document.path, document.buffer)) {
            is VfsResult.Success -> VfsResult.Success(result.value.toDocument())
            is VfsResult.Failure -> result
        }

    /**
     * Creates the exact immutable source snapshot required by RUN.
     * The snapshot is not an execution result; a runtime coordinator must execute
     * this exact source and issue the operation-bound receipt after real execution.
     */
    fun prepareRun(
        fileSystem: VirtualFileSystem,
        operationId: String,
        document: EditorDocument
    ): VfsResult<EditorRunSnapshot> {
        return when (val saved = save(fileSystem, document)) {
            is VfsResult.Failure -> saved
            is VfsResult.Success -> {
                val source = saved.value.savedContent
                VfsResult.Success(
                    EditorRunSnapshot(
                        operationId = operationId,
                        path = saved.value.path,
                        source = source,
                        sourceHash = sha256(source)
                    )
                )
            }
        }
    }

    fun sha256(source: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun VfsFile.toDocument() = EditorDocument(
        path = path,
        buffer = content,
        savedContent = content,
        access = access,
        origin = origin
    )
}
