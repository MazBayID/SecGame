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
}
