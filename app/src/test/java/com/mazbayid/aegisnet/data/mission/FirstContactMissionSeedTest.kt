package com.mazbayid.aegisnet.data.mission

import com.mazbayid.aegisnet.domain.evidence.EvidenceType
import com.mazbayid.aegisnet.domain.vfs.VfsAccess
import com.mazbayid.aegisnet.domain.vfs.VfsFile
import com.mazbayid.aegisnet.domain.vfs.VfsOrigin
import com.mazbayid.aegisnet.domain.vfs.VfsWorkspaceSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstContactMissionSeedTest {

    @Test
    fun firstFourMissionsHaveDistinctSourceEvidence() {
        val graph = FirstContactMissionSeed.evidenceGraph()
        val ids = graph.evidence.map { it.id }.toSet()

        assertTrue(ids.contains("message_fragment"))
        assertTrue(ids.contains("identity_record"))
        assertTrue(ids.contains("system_trace_log"))
        assertTrue(ids.contains("record_inventory"))
        assertEquals(
            EvidenceType.SYSTEM_LOG,
            graph.evidence.single { it.id == "system_trace_log" }.type
        )
        assertEquals(
            EvidenceType.DATASET,
            graph.evidence.single { it.id == "record_inventory" }.type
        )
    }

    @Test
    fun traceLogIsReadOnlyAndContainsExactlyTwoErrorEntries() {
        val log = FirstContactMissionSeed.missionWorkspace().files
            .single { it.path == "briefing/system_trace.log" }

        assertEquals(VfsAccess.READ_ONLY, log.access)
        assertEquals(VfsOrigin.MISSION_SOURCE, log.origin)
        assertEquals(2, log.content.lineSequence().count { it.contains("ERROR") })
    }

    @Test
    fun traceAuditStarterUsesLoopConditionAndReportsCount() {
        val script = FirstContactMissionSeed.missionWorkspace().files
            .single { it.path == "scripts/trace_audit.py" }

        assertEquals(VfsAccess.READ_WRITE, script.access)
        assertTrue(script.content.contains("for line in lines:"))
        assertTrue(script.content.contains("if 'ERROR' in line:"))
        assertTrue(script.content.contains("ERROR_COUNT="))
    }

    @Test
    fun workspaceMigrationAddsNewMissionFilesWithoutOverwritingPlayerWork() {
        val customScript = VfsFile(
            path = "scripts/trace_audit.py",
            content = "print('player work')",
            access = VfsAccess.READ_WRITE,
            origin = VfsOrigin.PLAYER_CREATED
        )
        val existing = VfsWorkspaceSnapshot(files = listOf(customScript))

        val merged = FirstContactMissionSeed.mergeMissionWorkspace(existing)

        assertEquals(
            "print('player work')",
            merged.files.single { it.path == "scripts/trace_audit.py" }.content
        )
        assertNotNull(merged.files.find { it.path == "briefing/system_trace.log" })
        assertNotNull(merged.files.find { it.path == "scripts/identity_check.py" })
        assertFalse(merged.files.any { it.path == "briefing/system_trace.log" && it.access == VfsAccess.READ_WRITE })
    }

    @Test
    fun recordInventoryIsReadOnlyAndContainsFiveRecords() {
        val inventory = FirstContactMissionSeed.missionWorkspace().files
            .single { it.path == "briefing/record_inventory.txt" }

        assertEquals(VfsAccess.READ_ONLY, inventory.access)
        assertEquals(5, inventory.content.lineSequence().count())
    }

}
