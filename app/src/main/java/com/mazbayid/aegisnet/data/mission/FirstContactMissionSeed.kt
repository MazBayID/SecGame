package com.mazbayid.aegisnet.data.mission

import com.mazbayid.aegisnet.domain.evidence.*
import com.mazbayid.aegisnet.domain.mission.FirstContactMissionCatalog
import com.mazbayid.aegisnet.domain.vfs.*

object FirstContactMissionSeed {
    fun evidenceGraph() = EvidenceGraph(
        operationId = FirstContactMissionCatalog.OPERATION_ID,
        evidence = listOf(
            EvidenceRecord(
                id = "message_fragment",
                title = "Message Fragment",
                description = "The original file contains ACCESS GRANTED. Freya's attachment and the Intranet File Repository refer to the same fragment, not two independent evidence records.",
                type = EvidenceType.FILE,
                sourceId = "freya_intranet_message_fragment",
                sourceLabel = "Freya attachment / Intranet File Repository",
                sourceReference = "message_fragment.txt",
                integrity = EvidenceIntegrity.UNKNOWN,
                access = EvidenceAccess.INTERNAL,
                origin = EvidenceOrigin.ORIGINAL,
                operationId = FirstContactMissionCatalog.OPERATION_ID
            ),
            EvidenceRecord(
                id = "identity_record",
                title = "Recruitment Identity Record",
                description = "An official Recruitment Office record listing the candidate identifier and temporary access code. The record is source material, not proof of the candidate's real-world identity.",
                type = EvidenceType.FILE,
                sourceId = "recruitment_office_record_ro_014",
                sourceLabel = "A.E.G.I.S. Recruitment Office",
                sourceReference = "identity_record.txt",
                integrity = EvidenceIntegrity.UNKNOWN,
                access = EvidenceAccess.INTERNAL,
                origin = EvidenceOrigin.ORIGINAL,
                operationId = FirstContactMissionCatalog.OPERATION_ID
            )
        )
    )

    fun mergeEvidenceGraph(graph: EvidenceGraph): EvidenceGraph {
        require(graph.operationId == FirstContactMissionCatalog.OPERATION_ID)
        val existingIds = graph.evidence.map { it.id }.toSet()
        val missing = evidenceGraph().evidence.filterNot { it.id in existingIds }
        return graph.copy(evidence = graph.evidence + missing)
    }

    fun mergeMissionWorkspace(snapshot: VfsWorkspaceSnapshot): VfsWorkspaceSnapshot {
        val existingPaths = snapshot.files.map { it.path }.toSet()
        val missing = missionWorkspace().files.filterNot { it.path in existingPaths }
        return snapshot.copy(
            files = snapshot.files + missing,
            directories = snapshot.directories + missionWorkspace().directories
        )
    }

    fun missionWorkspace() = VfsWorkspaceSnapshot(
        files = listOf(
            VfsFile(
                path = "briefing/message_fragment.txt",
                content = "ACCESS GRANTED",
                access = VfsAccess.READ_ONLY,
                origin = VfsOrigin.MISSION_SOURCE
            ),
            VfsFile(
                path = "scripts/analysis.py",
                content = "message = aegis.read_text('briefing/message_fragment.txt')\nprint(message)\n",
                access = VfsAccess.READ_WRITE,
                origin = VfsOrigin.PLAYER_CREATED
            ),
            VfsFile(
                path = "briefing/identity_record.txt",
                content = "A.E.G.I.S. RECRUITMENT OFFICE\nCandidate: RECRUIT-014\nAccess Code: FIRSTCONTACT\nStatus: PENDING VERIFICATION\nRecord: RO-014",
                access = VfsAccess.READ_ONLY,
                origin = VfsOrigin.MISSION_SOURCE
            ),
            VfsFile(
                path = "scripts/identity_check.py",
                content = "record = aegis.read_text('briefing/identity_record.txt')\nfields = record.split('\\n')\ncandidate_line = fields[1]\naccess_line = fields[2]\ncandidate_id = candidate_line.split(': ')[1]\naccess_code = access_line.split(': ')[1]\nprint(candidate_id)\nprint(access_code)\n",
                access = VfsAccess.READ_WRITE,
                origin = VfsOrigin.PLAYER_CREATED
            )
        )
    )
}
