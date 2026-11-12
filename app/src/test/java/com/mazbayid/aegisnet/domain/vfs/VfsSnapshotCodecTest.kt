package com.mazbayid.aegisnet.domain.vfs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class VfsSnapshotCodecTest {

    @Test
    fun roundTripsUnicodeNewlinesTabsAndMetadata() {
        val snapshot = VfsWorkspaceSnapshot(
            files = listOf(
                VfsFile(
                    path = "notes/hasil.txt",
                    content = "Halo, Zero!\nbaris kedua\t✓",
                    access = VfsAccess.READ_WRITE,
                    origin = VfsOrigin.PLAYER_CREATED
                ),
                VfsFile(
                    path = "logs/system.log",
                    content = "RX-14 online",
                    access = VfsAccess.READ_ONLY,
                    origin = VfsOrigin.MISSION_SOURCE
                )
            ),
            directories = setOf("notes/archive", "reports")
        )

        val restored = VfsSnapshotCodec.decode(VfsSnapshotCodec.encode(snapshot))

        assertEquals(snapshot.files.sortedBy { it.path }, restored.files)
        assertEquals(snapshot.directories, restored.directories)
    }

    @Test
    fun rejectsUnknownSnapshotVersion() {
        assertThrows(IllegalArgumentException::class.java) {
            VfsSnapshotCodec.decode("AEGIS_VFS\t99\n")
        }
    }

    @Test
    fun rejectsMalformedRecords() {
        assertThrows(IllegalArgumentException::class.java) {
            VfsSnapshotCodec.decode("AEGIS_VFS\t1\nF\tbroken\n")
        }
    }

    @Test
    fun rejectsTraversalAndDuplicateFilePaths() {
        val encodedTraversal = "AEGIS_VFS\t1\nF\tLi4vZXZpbC50eHQ\tWA\tREAD_WRITE\tPLAYER_CREATED\n"
        val path = "draft.txt"
        val field = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(path.toByteArray(Charsets.UTF_8))
        val content = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("x".toByteArray(Charsets.UTF_8))
        val duplicate = "AEGIS_VFS\t1\nF\t$field\t$content\tREAD_WRITE\tPLAYER_CREATED\nF\t$field\t$content\tREAD_WRITE\tPLAYER_CREATED\n"

        assertThrows(IllegalArgumentException::class.java) {
            VfsSnapshotCodec.decode(encodedTraversal)
        }
        assertThrows(IllegalArgumentException::class.java) {
            VfsSnapshotCodec.decode(duplicate)
        }
    }
}
