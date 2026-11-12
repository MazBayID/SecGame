package com.mazbayid.aegisnet.domain.editor

import com.mazbayid.aegisnet.domain.vfs.VfsAccess
import com.mazbayid.aegisnet.domain.vfs.VfsFile
import com.mazbayid.aegisnet.domain.vfs.VfsOrigin
import com.mazbayid.aegisnet.domain.vfs.VfsResult
import com.mazbayid.aegisnet.domain.vfs.VfsWorkspace
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorDocumentServiceTest {

    private fun fileSystem() = VirtualFileSystem(
        operationId = "first_contact_01",
        personalFiles = listOf(
            VfsFile(
                "scripts/analysis.py",
                "print('old')",
                VfsAccess.READ_WRITE,
                VfsOrigin.PLAYER_CREATED
            )
        ),
        missionFiles = listOf(
            VfsFile("source/brief.txt", "immutable briefing", VfsAccess.READ_ONLY, VfsOrigin.MISSION_SOURCE)
        )
    )

    @Test
    fun opensFileAndTracksUnsavedEdits() {
        val vfs = fileSystem()
        val opened = EditorDocumentService.open(vfs, "scripts/analysis.py") as VfsResult.Success
        val edited = EditorDocumentService.update(opened.value, "print('new')")

        assertFalse(opened.value.isDirty)
        assertTrue(edited.isDirty)
        assertEquals("print('old')", edited.savedContent)
        assertEquals("print('new')", edited.buffer)
    }

    @Test
    fun savesExactBufferAndCanReopenIt() {
        val vfs = fileSystem()
        val opened = EditorDocumentService.open(vfs, "scripts/analysis.py") as VfsResult.Success
        val edited = EditorDocumentService.update(opened.value, "print('saved')\n")
        val saved = EditorDocumentService.save(vfs, edited) as VfsResult.Success
        val reopened = EditorDocumentService.open(vfs, "scripts/analysis.py") as VfsResult.Success

        assertFalse(saved.value.isDirty)
        assertEquals("print('saved')\n", reopened.value.buffer)
    }

    @Test
    fun cannotSaveReadOnlyMissionSource() {
        val vfs = fileSystem()
        vfs.switchWorkspace(VfsWorkspace.MISSION)
        val opened = EditorDocumentService.open(vfs, "source/brief.txt") as VfsResult.Success
        val edited = EditorDocumentService.update(opened.value, "tampered")

        val result = EditorDocumentService.save(vfs, edited)
        assertTrue(result is VfsResult.Failure)
        assertEquals("immutable briefing", (vfs.cat("source/brief.txt") as VfsResult.Success).value)
    }

    @Test
    fun runSnapshotSavesAndHashesExactUnsavedBuffer() {
        val vfs = fileSystem()
        val opened = EditorDocumentService.open(vfs, "scripts/analysis.py") as VfsResult.Success
        val unsaved = EditorDocumentService.update(opened.value, "print('exact snapshot')\n")

        val result = EditorDocumentService.prepareRun(vfs, "first_contact_01", unsaved)
        assertTrue(result is VfsResult.Success)
        val snapshot = (result as VfsResult.Success).value

        assertEquals(unsaved.buffer, snapshot.source)
        assertEquals(EditorDocumentService.sha256(unsaved.buffer), snapshot.sourceHash)
        assertEquals(unsaved.buffer, (vfs.cat("scripts/analysis.py") as VfsResult.Success).value)
    }

    @Test
    fun runSnapshotIsImmutableWhenEditorChangesLater() {
        val vfs = fileSystem()
        val opened = EditorDocumentService.open(vfs, "scripts/analysis.py") as VfsResult.Success
        val first = EditorDocumentService.prepareRun(
            vfs,
            "first_contact_01",
            EditorDocumentService.update(opened.value, "print('first')")
        ) as VfsResult.Success

        val changed = EditorDocumentService.update(opened.value, "print('second')")
        assertEquals("print('first')", first.value.source)
        assertEquals(EditorDocumentService.sha256("print('first')"), first.value.sourceHash)
        assertEquals("print('second')", changed.buffer)
    }

    @Test
    fun hashesUseSha256HexEncoding() {
        assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            EditorDocumentService.sha256("hello")
        )
    }
}
