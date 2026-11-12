package com.mazbayid.aegisnet.domain.campaign

import org.junit.Assert.assertEquals
import org.junit.Test

class CampaignRankProgressionTest {

    @Test
    fun newCandidateStartsAsApplicant() {
        assertEquals(
            CampaignRank.APPLICANT,
            CampaignRankProgression.rank(CampaignMissionProgress())
        )
    }

    @Test
    fun firstMissionAloneDoesNotAwardRecruitRank() {
        assertEquals(
            CampaignRank.APPLICANT,
            CampaignRankProgression.rank(
                CampaignMissionProgress(listOf("fc_m01_first_signal"))
            )
        )
    }

    @Test
    fun recruitRankIsAwardedAfterBothRecruitmentVerificationMissions() {
        assertEquals(
            CampaignRank.RECRUIT,
            CampaignRankProgression.rank(
                CampaignMissionProgress(
                    listOf("fc_m01_first_signal", "fc_m02_identity_check")
                )
            )
        )
    }

    @Test
    fun laterMissionCompletionsDoNotChangeOrReawardRecruitRank() {
        assertEquals(
            CampaignRank.RECRUIT,
            CampaignRankProgression.rank(
                CampaignMissionProgress(
                    listOf(
                        "fc_m01_first_signal",
                        "fc_m02_identity_check",
                        "fc_m03_system_trace_audit",
                        "fc_m04_record_inventory"
                    )
                )
            )
        )
    }
}
