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
            )
        )
    )

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
            )
        )
    )
}
