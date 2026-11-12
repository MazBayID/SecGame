package com.mazbayid.aegisnet.domain.campaign

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CampaignProgressionTest {

    @Test
    fun newCampaignStartsAtOfficialMessage() {
        assertEquals(
            CampaignStep.OFFICIAL_MESSAGE_OPENED,
            CampaignProgression.nextStep(CampaignProgress())
        )
    }

    @Test
    fun futureStoryBeatCannotBeSkipped() {
        val result = CampaignProgression.advance(
            CampaignProgress(),
            CampaignStep.FREYA_MESSAGE_OPENED
        )

        assertTrue(result is CampaignProgressMutation.Rejected)
        assertEquals(0, CampaignProgress().completedSteps.size)
    }

    @Test
    fun everyOpeningBeatMustBeExplicitlyAdvanced() {
        var progress = CampaignProgress()
        CampaignStep.entries.forEach { step ->
            val result = CampaignProgression.advance(progress, step)
            assertTrue("Expected acceptance for $step", result is CampaignProgressMutation.Accepted)
            progress = (result as CampaignProgressMutation.Accepted).progress
        }

        assertEquals(CampaignStep.entries.toSet(), progress.completedSteps)
        assertEquals(null, CampaignProgression.nextStep(progress))
    }

    @Test
    fun repeatedPlayerActionIsIdempotent() {
        val first = CampaignProgression.advance(
            CampaignProgress(),
            CampaignStep.OFFICIAL_MESSAGE_OPENED
        ) as CampaignProgressMutation.Accepted

        val second = CampaignProgression.advance(
            first.progress,
            CampaignStep.OFFICIAL_MESSAGE_OPENED
        ) as CampaignProgressMutation.Accepted

        assertFalse(second.changed)
        assertEquals(first.progress, second.progress)
    }

    @Test
    fun restoreRejectsGapsAndUnknownProgression() {
        val restored = CampaignProgression.restore(
            setOf(
                CampaignStep.OFFICIAL_MESSAGE_OPENED,
                CampaignStep.FIRST_MISSION_ACTIVATED,
                CampaignStep.NEXUS_LINK_OPENED
            )
        )

        assertEquals(
            setOf(CampaignStep.OFFICIAL_MESSAGE_OPENED),
            restored.completedSteps
        )
        assertEquals(
            CampaignStep.MISSION_SEQUENCE_OPENED,
            CampaignProgression.nextStep(restored)
        )
    }

    @Test
    fun restoreKeepsCompletePrefix() {
        val prefix = CampaignStep.entries.take(4).toSet()
        assertEquals(prefix, CampaignProgression.restore(prefix).completedSteps)
    }
}
