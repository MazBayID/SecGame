package com.mazbayid.aegisnet.domain.mission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstContactMissionCatalogTest {

    @Test
    fun containsTheEightCanonMissionsInApprovedOrder() {
        assertEquals(
            listOf(
                "Sinyal Pertama",
                "Verifikasi Identitas",
                "Audit Jejak Sistem",
                "Inventaris Rekaman",
                "Penelusuran Referensi",
                "Perbandingan Dua Sumber",
                "Normalisasi Waktu",
                "Validasi Selisih"
            ),
            FirstContactMissionCatalog.missions.map { it.definition.title }
        )
        assertEquals((1..8).toList(), FirstContactMissionCatalog.missions.map { it.sequenceNumber })
    }

    @Test
    fun everyAuthoredMissionDefinitionPassesValidation() {
        FirstContactMissionCatalog.missions.forEach { mission ->
            assertTrue(
                "${mission.definition.id}: ${MissionDefinitionValidator.validate(mission.definition)}",
                MissionDefinitionValidator.validate(mission.definition).isEmpty()
            )
        }
    }

    @Test
    fun allMissionsBelongToTheSameApprovedChapterAndOperation() {
        assertTrue(FirstContactMissionCatalog.missions.all {
            it.chapterId == FirstContactMissionCatalog.CHAPTER_ID &&
                it.definition.operationId == FirstContactMissionCatalog.OPERATION_ID
        })
    }

    @Test
    fun learningConceptsFollowTheApprovedSequence() {
        assertEquals(listOf("variables", "print()"), FirstContactMissionCatalog.missions[0].learningConcepts)
        assertEquals(listOf("for", "if", "counting"), FirstContactMissionCatalog.missions[2].learningConcepts)
        assertEquals(listOf("enumerate()", "filtering", "source indices"), FirstContactMissionCatalog.missions[4].learningConcepts)
        assertEquals(listOf("int()", "indexing", "signed difference"), FirstContactMissionCatalog.missions[6].learningConcepts)
        assertTrue(FirstContactMissionCatalog.missions[7].learningConcepts.contains("responsible uncertainty"))
    }

    @Test
    fun finalMissionRequiresReportingThatCauseIsNotEstablished() {
        val finalMission = FirstContactMissionCatalog.missions.last()
        val uncertaintyObjective = finalMission.definition.objectives
            .firstOrNull { it.id == "report_unresolved_cause" }

        assertNotNull(uncertaintyObjective)
        assertEquals(setOf("cause_not_established"), uncertaintyObjective?.acceptedOutcomeIds)
        assertFalse(finalMission.definition.objectives.any {
            it.acceptedOutcomeIds.any { outcome ->
                outcome.contains("tampering") || outcome.contains("culprit")
            }
        })
    }

    @Test
    fun missionLookupUsesStableIdentifiers() {
        val first = FirstContactMissionCatalog.find("fc_m01_first_signal")
        assertEquals("Sinyal Pertama", first?.definition?.title)
        assertEquals(null, FirstContactMissionCatalog.find("unknown_mission"))
    }

    @Test
    fun identityCheckRequiresItsSourceRecordAndBothReportedFields() {
        val objective = FirstContactMissionCatalog.missions[1].definition.objectives.single()
        assertEquals(setOf("identity_record"), objective.requiredEvidenceIds)
        assertEquals(setOf("RECRUIT-014", "FIRSTCONTACT"), objective.requiredOutputContains)
        assertTrue(objective.requiresSuccessfulRun)
    }

    @Test
    fun systemTraceAuditRequiresLogEvidenceAndExactErrorCount() {
        val objective = FirstContactMissionCatalog.missions[2].definition.objectives.single()
        assertEquals(setOf("system_trace_log"), objective.requiredEvidenceIds)
        assertEquals(setOf("ERROR_COUNT=2"), objective.requiredOutputContains)
        assertTrue(objective.requiresSuccessfulRun)
    }

    @Test
    fun recordInventoryRequiresDatasetEvidenceAndExactCollectionSize() {
        val objective = FirstContactMissionCatalog.missions[3].definition.objectives.single()
        assertEquals(setOf("record_inventory"), objective.requiredEvidenceIds)
        assertEquals(setOf("RECORD_COUNT=5"), objective.requiredOutputContains)
        assertTrue(objective.requiresSuccessfulRun)
    }

    @Test
    fun referenceTraceRequiresCatalogEvidenceAndCorrectSourceIndex() {
        val objective = FirstContactMissionCatalog.missions[4].definition.objectives.single()
        assertEquals(setOf("reference_catalog"), objective.requiredEvidenceIds)
        assertEquals(setOf("SOURCE_INDEX=2|RX-14|RECEIPT"), objective.requiredOutputContains)
        assertTrue(objective.requiresSuccessfulRun)
    }

    @Test
    fun twoSourceComparisonRequiresBothRecordsAndExactSourceValues() {
        val objective = FirstContactMissionCatalog.missions[5].definition.objectives.single()
        assertEquals(setOf("aegis_receipts", "archive_receipts"), objective.requiredEvidenceIds)
        assertEquals(
            setOf("AEGIS_TIME=09:41:12", "ARCHIVE_TIME=09:41:16", "SAME_REFERENCE=True"),
            objective.requiredOutputContains
        )
    }

    @Test
    fun timeNormalizationRequiresSignedFourSecondDelta() {
        val objective = FirstContactMissionCatalog.missions[6].definition.objectives.single()
        assertEquals(setOf("aegis_receipts", "archive_receipts"), objective.requiredEvidenceIds)
        assertEquals(setOf("SIGNED_DELTA=4"), objective.requiredOutputContains)
    }

    @Test
    fun finalDeltaValidationRequiresAbsoluteDifferenceAndResponsibleUncertainty() {
        val mission = FirstContactMissionCatalog.missions[7].definition
        assertEquals(setOf("ABSOLUTE_DELTA=4"), mission.objectives.first().requiredOutputContains)
        assertTrue(mission.objectives.any { it.id == "report_unresolved_cause" })
    }
}
