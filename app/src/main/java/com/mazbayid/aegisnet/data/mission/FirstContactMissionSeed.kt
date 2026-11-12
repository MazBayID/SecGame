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
            ),
            EvidenceRecord(
                id = "system_trace_log",
                title = "Training System Trace Log",
                description = "A read-only diagnostic log from the training transmission gateway. It contains informational entries, a warning, and error records; the original lines must remain unchanged.",
                type = EvidenceType.SYSTEM_LOG,
                sourceId = "training_gateway_trace_014",
                sourceLabel = "A.E.G.I.S. Technical Operations",
                sourceReference = "system_trace.log",
                integrity = EvidenceIntegrity.UNKNOWN,
                access = EvidenceAccess.INTERNAL,
                origin = EvidenceOrigin.ORIGINAL,
                operationId = FirstContactMissionCatalog.OPERATION_ID
            ),
            EvidenceRecord(
                id = "record_inventory",
                title = "Training Record Inventory",
                description = "A source export containing five distinct training record references. The player must report the collection size, not infer additional records.",
                type = EvidenceType.DATASET,
                sourceId = "training_archive_inventory_015",
                sourceLabel = "A.E.G.I.S. Records Office",
                sourceReference = "record_inventory.txt",
                integrity = EvidenceIntegrity.UNKNOWN,
                access = EvidenceAccess.INTERNAL,
                origin = EvidenceOrigin.ORIGINAL,
                operationId = FirstContactMissionCatalog.OPERATION_ID
            ),
            EvidenceRecord(
                id = "reference_catalog",
                title = "Reference Routing Catalog",
                description = "A read-only catalog preserving the source order of transmission references. RX-14 appears once at a specific zero-based source index.",
                type = EvidenceType.DATASET,
                sourceId = "training_reference_catalog_016",
                sourceLabel = "A.E.G.I.S. Technical Registry",
                sourceReference = "reference_catalog.txt",
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
            ),
            VfsFile(
                path = "briefing/system_trace.log",
                content = "09:40:58 INFO channel initialized\n09:41:02 ERROR retry queue overflow\n09:41:04 WARNING delayed packet\n09:41:08 ERROR checksum mismatch\n09:41:12 INFO receipt stored",
                access = VfsAccess.READ_ONLY,
                origin = VfsOrigin.MISSION_SOURCE
            ),
            VfsFile(
                path = "scripts/trace_audit.py",
                content = "log_text = aegis.read_text('briefing/system_trace.log')\nlines = log_text.split('\\n')\nerror_count = 0\nfor line in lines:\n    if 'ERROR' in line:\n        error_count = error_count + 1\nprint('ERROR_COUNT=' + str(error_count))\n",
                access = VfsAccess.READ_WRITE,
                origin = VfsOrigin.PLAYER_CREATED
            ),
            VfsFile(
                path = "briefing/record_inventory.txt",
                content = "RX-11\nRX-12\nRX-13\nRX-14\nRX-15",
                access = VfsAccess.READ_ONLY,
                origin = VfsOrigin.MISSION_SOURCE
            ),
            VfsFile(
                path = "scripts/inventory_count.py",
                content = "inventory_text = aegis.read_text('briefing/record_inventory.txt')\nrecords = inventory_text.split('\\n')\nrecord_count = len(records)\nprint('RECORD_COUNT=' + str(record_count))\n",
                access = VfsAccess.READ_WRITE,
                origin = VfsOrigin.PLAYER_CREATED
            ),
            VfsFile(
                path = "briefing/reference_catalog.txt",
                content = "RX-11|ROUTINE\\nRX-12|ROUTINE\\nRX-14|RECEIPT\\nRX-15|ROUTINE\\nRX-16|RECEIPT",
                access = VfsAccess.READ_ONLY,
                origin = VfsOrigin.MISSION_SOURCE
            ),
            VfsFile(
                path = "scripts/reference_trace.py",
                content = "catalog_text = aegis.read_text('briefing/reference_catalog.txt')\\nentries = catalog_text.split('\\\\n')\\nfor source_index, entry in enumerate(entries):\\n    if 'RX-14' in entry:\\n        print('SOURCE_INDEX=' + str(source_index) + '|' + entry)\\n",
                access = VfsAccess.READ_WRITE,
                origin = VfsOrigin.PLAYER_CREATED
            )
        )
    )
}
