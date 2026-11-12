package com.mazbayid.aegisnet.domain.campaign

/**
 * Persisted milestones for Campaign 01 / First Contact.
 *
 * A milestone is recorded only after the corresponding player action. The
 * sequence is deliberately linear so a restored save cannot skip story beats.
 */
enum class CampaignStep {
    OFFICIAL_MESSAGE_OPENED,
    MISSION_SEQUENCE_OPENED,
    FIRST_MISSION_ACTIVATED,
    FREYA_MESSAGE_OPENED,
    NEXUS_LINK_OPENED,
    NEXUS_CHANNEL_OPENED
}

data class CampaignProgress(
    val completedSteps: Set<CampaignStep> = emptySet()
)

data class CampaignProgressIssue(
    val code: String,
    val message: String
)

sealed interface CampaignProgressMutation {
    data class Accepted(
        val progress: CampaignProgress,
        val changed: Boolean
    ) : CampaignProgressMutation

    data class Rejected(
        val issue: CampaignProgressIssue
    ) : CampaignProgressMutation
}

object CampaignProgression {

    fun nextStep(progress: CampaignProgress): CampaignStep? =
        CampaignStep.entries.firstOrNull { it !in progress.completedSteps }

    /**
     * Records one explicit player action. Repeated actions are idempotent;
     * future beats cannot be recorded before their prerequisites.
     */
    fun advance(
        progress: CampaignProgress,
        step: CampaignStep
    ): CampaignProgressMutation {
        if (step in progress.completedSteps) {
            return CampaignProgressMutation.Accepted(progress, changed = false)
        }

        val expected = nextStep(progress)
        if (step != expected) {
            return CampaignProgressMutation.Rejected(
                CampaignProgressIssue(
                    code = "campaign_step_out_of_order",
                    message = "Complete the current story beat before advancing."
                )
            )
        }

        return CampaignProgressMutation.Accepted(
            progress = progress.copy(completedSteps = progress.completedSteps + step),
            changed = true
        )
    }

    /**
     * Sanitizes locally restored data. Only the contiguous completed prefix is
     * trusted; corrupt or partially written saves cannot skip campaign beats.
     */
    fun restore(persistedSteps: Set<CampaignStep>): CampaignProgress {
        val validPrefix = linkedSetOf<CampaignStep>()
        for (step in CampaignStep.entries) {
            if (step !in persistedSteps) break
            validPrefix += step
        }
        return CampaignProgress(validPrefix)
    }
}
