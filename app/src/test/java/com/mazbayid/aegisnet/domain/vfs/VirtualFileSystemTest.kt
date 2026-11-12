package com.mazbayid.aegisnet.domain.vfs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VirtualFileSystemTest {

    private fun <T> success(result: VfsResult<T>): T {
        assertTrue("Expected success but received $result", result is VfsResult.Success)
        return (result as VfsResult.Success<T>).value
    }

    private fun failureCode(result: VfsResult<*>): String {
        assertTrue("Expected failure but received $result", result is VfsResult.Failure)
        return (result as VfsResult.Failure).code
    }

    private fun fileSystem() = VirtualFileSystem(
        operationId = "first_contact_01",
        personalFiles = listOf(
            VfsFile("notes/readme.txt", "Personal notes", VfsAccess.READ_WRITE, VfsOrigin.PLAYER_CREATED)
        ),
        missionFiles = listOf(
            VfsFile("logs/system.log", "09:41:12 RX-14 received", VfsAccess.READ_ONLY, VfsOrigin.MISSION_SOURCE)
        ),
        missionDirectories = setOf("reports")
    )

    @Test
    fun startsInPersonalWorkspaceAndListsSeededFiles() {
        val vfs = fileSystem()

        assertEquals("/personal", vfs.pwd())
        val entries = success(vfs.ls())
        assertEquals(listOf("notes"), entries.map { it.name })
        assertTrue(entries.single().isDirectory)
    }

    @Test
    fun supportsRelativeNavigationAndParentDirectory() {
        val vfs = fileSystem()

        assertEquals("/personal/notes", success(vfs.cd("notes")))
        assertEquals("Personal notes", success(vfs.cat("readme.txt")))
        assertEquals("/personal", success(vfs.cd("..")))
    }

    @Test
    fun missionWorkspaceIsExplicitAndIsolated() {
        val vfs = fileSystem()
        success(vfs.touch("private.txt"))

        assertEquals("/mission/first_contact_01", vfs.switchWorkspace(VfsWorkspace.MISSION))
        assertEquals(listOf("logs", "reports"), success(vfs.ls()).map { it.name })
        assertEquals(
            "workspace_boundary",
            failureCode(vfs.cat("/personal/private.txt"))
        )

        vfs.switchWorkspace(VfsWorkspace.PERSONAL)
        assertEquals("", success(vfs.cat("private.txt")))
    }

    @Test
    fun missionSourceEvidenceIsReadableButCannotBeModified() {
        val vfs = fileSystem()
        vfs.switchWorkspace(VfsWorkspace.MISSION)

        assertEquals("09:41:12 RX-14 received", success(vfs.cat("logs/system.log")))
        assertEquals(
            "read_only",
            failureCode(vfs.writeFile("logs/system.log", "tampered"))
        )
        assertEquals("09:41:12 RX-14 received", success(vfs.cat("logs/system.log")))
    }

    @Test
    fun createsAndWritesPlayerOwnedFiles() {
        val vfs = fileSystem()

        success(vfs.mkdir("scripts"))
        val created = success(vfs.touch("scripts/analysis.py"))
        assertEquals(VfsAccess.READ_WRITE, created.access)
        assertEquals(VfsOrigin.PLAYER_CREATED, created.origin)

        success(vfs.writeFile("scripts/analysis.py", "print('RX-14')"))
        assertEquals("print('RX-14')", success(vfs.cat("scripts/analysis.py")))
    }

    @Test
    fun rejectsTraversalOutsideWorkspaceRoot() {
        val vfs = fileSystem()

        assertEquals("path_escape", failureCode(vfs.cd("../../mission")))
        assertEquals("/personal", vfs.pwd())
    }

    @Test
    fun rejectsAbsolutePathIntoAnotherWorkspace() {
        val vfs = fileSystem()
        vfs.switchWorkspace(VfsWorkspace.MISSION)

        assertEquals(
            "workspace_boundary",
            failureCode(vfs.cat("/personal/notes/readme.txt"))
        )
    }

    @Test
    fun rejectsMissingParentAndDirectoryAsFile() {
        val vfs = fileSystem()

        assertEquals("parent_not_found", failureCode(vfs.touch("missing/file.txt")))
        assertEquals("is_a_directory", failureCode(vfs.cat("notes")))
    }

    @Test
    fun listingShowsOnlyDirectChildrenInStableOrder() {
        val vfs = fileSystem()
        success(vfs.mkdir("notes/archive"))
        success(vfs.touch("notes/archive/old.txt"))
        success(vfs.cd("notes"))

        val entries = success(vfs.ls())
        assertEquals(listOf("archive", "readme.txt"), entries.map { it.name })
        assertTrue(entries.first().isDirectory)
        assertFalse(entries.last().isDirectory)
    }

    @Test
    fun repeatedTouchDoesNotEraseExistingPlayerFile() {
        val vfs = fileSystem()
        success(vfs.writeFile("draft.txt", "keep this"))
        success(vfs.touch("draft.txt"))

        assertEquals("keep this", success(vfs.cat("draft.txt")))
    }
}
