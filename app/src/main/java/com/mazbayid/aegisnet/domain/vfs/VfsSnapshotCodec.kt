package com.mazbayid.aegisnet.domain.vfs

import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Versioned, line-oriented codec for small local workspace snapshots.
 * Base64 fields preserve arbitrary UTF-8 content, including newlines and tabs.
 */
object VfsSnapshotCodec {
    private const val HEADER = "AEGIS_VFS\t1"

    fun encode(snapshot: VfsWorkspaceSnapshot): String = buildString {
        append(HEADER).append('\n')
        snapshot.directories.sorted().forEach { path ->
            append("D\t").append(encodeField(path)).append('\n')
        }
        snapshot.files.sortedBy { it.path }.forEach { file ->
            append("F\t")
                .append(encodeField(file.path)).append('\t')
                .append(encodeField(file.content)).append('\t')
                .append(file.access.name).append('\t')
                .append(file.origin.name).append('\n')
        }
    }

    fun decode(serialized: String): VfsWorkspaceSnapshot {
        val lines = serialized.lineSequence().toList()
        require(lines.firstOrNull() == HEADER) { "Unsupported or missing VFS snapshot header." }

        val directories = linkedSetOf<String>()
        val files = mutableListOf<VfsFile>()

        lines.drop(1).filter { it.isNotEmpty() }.forEachIndexed { index, line ->
            val fields = line.split('\t')
            when (fields.firstOrNull()) {
                "D" -> {
                    require(fields.size == 2) { "Malformed directory record at line ${index + 2}." }
                    require(directories.add(decodeField(fields[1]))) {
                        "Duplicate directory record at line ${index + 2}."
                    }
                }
                "F" -> {
                    require(fields.size == 5) { "Malformed file record at line ${index + 2}." }
                    files += VfsFile(
                        path = decodeField(fields[1]),
                        content = decodeField(fields[2]),
                        access = VfsAccess.valueOf(fields[3]),
                        origin = VfsOrigin.valueOf(fields[4])
                    )
                }
                else -> throw IllegalArgumentException("Unknown VFS record at line ${index + 2}.")
            }
        }

        // Reuse VFS seed validation to reject traversal, duplicates, and file/directory conflicts.
        VirtualFileSystem(
            operationId = "snapshot_validation",
            personalFiles = files,
            personalDirectories = directories
        )
        return VfsWorkspaceSnapshot(files = files.sortedBy { it.path }, directories = directories.toSortedSet())
    }

    private fun encodeField(value: String): String =
        Base64.getUrlEncoder().withoutPadding()
            .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeField(value: String): String =
        String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)
}
