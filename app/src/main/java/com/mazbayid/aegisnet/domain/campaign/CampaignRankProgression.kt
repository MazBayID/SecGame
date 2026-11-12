package com.mazbayid.aegisnet.domain.campaign

/**
 * First Contact recruitment rank is awarded once, after both initial
 * verification missions are complete. Later missions never re-award it.
 */
enum class CampaignRank(val displayLabel: String) {
    APPLICANT("APPLICANT"),
    RECRUIT("RECRUIT")
}

object CampaignRankProgression {
    private val recruitmentMilestoneMissionIds = setOf(
        "fc_m01_first_signal",
        "fc_m02_identity_check"
    )

    fun rank(progress: CampaignMissionProgress): CampaignRank =
        if (recruitmentMilestoneMissionIds.all { it in progress.completedMissionIds }) {
            CampaignRank.RECRUIT
        } else {
            CampaignRank.APPLICANT
        }
}
