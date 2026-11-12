package com.mazbayid.aegisnet.domain.campaign

/**
 * Ordered completion record for one campaign chapter.
 * Only a contiguous prefix of the authored mission sequence is trusted.
 */
data class CampaignMissionProgress(
    val completedMissionIds: List<String> = emptyList()
)

data class CampaignMissionProgressIssue(
    val code: String,
    val message: String
)

sealed interface CampaignMissionMutation {
    data class Accepted(
        val progress: CampaignMissionProgress,
        val changed: Boolean
    ) : CampaignMissionMutation

    data class Rejected(
        val issue: CampaignMissionProgressIssue
    ) : CampaignMissionMutation
}

object CampaignMissionProgression {

    fun restore(
        persistedMissionIds: List<String>,
        orderedMissionIds: List<String>
    ): CampaignMissionProgress {
        validateSequence(orderedMissionIds)
        val trusted = mutableListOf<String>()
        for (missionId in orderedMissionIds) {
            if (missionId !in persistedMissionIds) break
            trusted += missionId
        }
        return CampaignMissionProgress(trusted)
    }

    fun nextMissionId(
        progress: CampaignMissionProgress,
        orderedMissionIds: List<String>
    ): String? {
        validateSequence(orderedMissionIds)
        val restored = restore(progress.completedMissionIds, orderedMissionIds)
        return orderedMissionIds.getOrNull(restored.completedMissionIds.size)
    }

    fun isUnlocked(
        missionId: String,
        progress: CampaignMissionProgress,
        orderedMissionIds: List<String>
    ): Boolean {
        validateSequence(orderedMissionIds)
        require(missionId in orderedMissionIds) { "Unknown campaign mission: $missionId." }
        val restored = restore(progress.completedMissionIds, orderedMissionIds)
        return missionId in restored.completedMissionIds ||
            missionId == orderedMissionIds.getOrNull(restored.completedMissionIds.size)
    }

    fun complete(
        progress: CampaignMissionProgress,
        missionId: String,
        orderedMissionIds: List<String>
    ): CampaignMissionMutation {
        validateSequence(orderedMissionIds)
        if (missionId !in orderedMissionIds) {
            return CampaignMissionMutation.Rejected(
                CampaignMissionProgressIssue(
                    "unknown_mission",
                    "The mission does not belong to this campaign chapter."
                )
            )
        }

        val restored = restore(progress.completedMissionIds, orderedMissionIds)
        if (missionId in restored.completedMissionIds) {
            return CampaignMissionMutation.Accepted(restored, changed = false)
        }

        val expected = orderedMissionIds.getOrNull(restored.completedMissionIds.size)
        if (missionId != expected) {
            return CampaignMissionMutation.Rejected(
                CampaignMissionProgressIssue(
                    "mission_locked",
                    "Complete the current mission before opening the next operation."
                )
            )
        }

        return CampaignMissionMutation.Accepted(
            CampaignMissionProgress(restored.completedMissionIds + missionId),
            changed = true
        )
    }

    private fun validateSequence(orderedMissionIds: List<String>) {
        require(orderedMissionIds.isNotEmpty()) { "A campaign chapter needs at least one mission." }
        require(orderedMissionIds.all { it.matches(MISSION_ID_PATTERN) }) {
            "Mission IDs must be non-blank stable identifiers."
        }
        require(orderedMissionIds.distinct().size == orderedMissionIds.size) {
            "Campaign mission order cannot contain duplicate IDs."
        }
    }

    private val MISSION_ID_PATTERN = Regex("[A-Za-z0-9_-]+")
}
