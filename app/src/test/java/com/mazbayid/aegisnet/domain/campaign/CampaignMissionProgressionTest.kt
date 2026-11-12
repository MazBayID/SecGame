package com.mazbayid.aegisnet.domain.campaign

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CampaignMissionProgressionTest {

    private val order = listOf("mission_01", "mission_02", "mission_03", "mission_04")

    @Test
    fun onlyTheFirstMissionIsUnlockedForANewCampaign() {
        val progress = CampaignMissionProgress()
        assertEquals("mission_01", CampaignMissionProgression.nextMissionId(progress, order))
        assertTrue(CampaignMissionProgression.isUnlocked("mission_01", progress, order))
        assertFalse(CampaignMissionProgression.isUnlocked("mission_02", progress, order))
    }

    @Test
    fun completionUnlocksExactlyTheNextMission() {
        val first = CampaignMissionProgression.complete(
            CampaignMissionProgress(), "mission_01", order
        ) as CampaignMissionMutation.Accepted

        assertTrue(first.changed)
        assertEquals(listOf("mission_01"), first.progress.completedMissionIds)
        assertEquals("mission_02", CampaignMissionProgression.nextMissionId(first.progress, order))
        assertTrue(CampaignMissionProgression.isUnlocked("mission_02", first.progress, order))
        assertFalse(CampaignMissionProgression.isUnlocked("mission_03", first.progress, order))
    }

    @Test
    fun completionCannotSkipAnEarlierMission() {
        val result = CampaignMissionProgression.complete(
            CampaignMissionProgress(), "mission_03", order
        )
        assertTrue(result is CampaignMissionMutation.Rejected)
        assertEquals(
            "mission_locked",
            (result as CampaignMissionMutation.Rejected).issue.code
        )
    }

    @Test
    fun repeatedCompletionIsIdempotent() {
        val progress = CampaignMissionProgress(listOf("mission_01"))
        val result = CampaignMissionProgression.complete(progress, "mission_01", order)
            as CampaignMissionMutation.Accepted

        assertFalse(result.changed)
        assertEquals(progress, result.progress)
    }

    @Test
    fun restoreDropsAnyCompletionAfterTheFirstGap() {
        val restored = CampaignMissionProgression.restore(
            listOf("mission_01", "mission_03", "mission_04"),
            order
        )
        assertEquals(listOf("mission_01"), restored.completedMissionIds)
        assertEquals("mission_02", CampaignMissionProgression.nextMissionId(restored, order))
    }

    @Test
    fun restoreIgnoresUnknownPersistedMissionIds() {
        val restored = CampaignMissionProgression.restore(
            listOf("mission_01", "removed_mission", "mission_02"),
            order
        )
        assertEquals(listOf("mission_01", "mission_02"), restored.completedMissionIds)
    }

    @Test
    fun unknownMissionCannotBeCompleted() {
        val result = CampaignMissionProgression.complete(
            CampaignMissionProgress(), "mission_99", order
        )
        assertTrue(result is CampaignMissionMutation.Rejected)
        assertEquals("unknown_mission", (result as CampaignMissionMutation.Rejected).issue.code)
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateMissionIdsInAuthoredOrderAreRejected() {
        CampaignMissionProgression.nextMissionId(
            CampaignMissionProgress(),
            listOf("mission_01", "mission_01")
        )
    }
}
