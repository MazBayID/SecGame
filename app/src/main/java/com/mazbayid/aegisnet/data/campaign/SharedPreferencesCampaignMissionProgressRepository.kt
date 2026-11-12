package com.mazbayid.aegisnet.data.campaign

import android.content.Context
import com.mazbayid.aegisnet.domain.campaign.CampaignMissionProgress
import com.mazbayid.aegisnet.domain.campaign.CampaignMissionProgressRepository
import com.mazbayid.aegisnet.domain.campaign.CampaignMissionProgression
import com.mazbayid.aegisnet.domain.mission.FirstContactMissionCatalog

/**
 * Offline persistence adapter for the ordered First Contact mission sequence.
 * Stored IDs are sanitized against the current authored catalog on every load
 * and save, so removed, unknown, or out-of-order IDs cannot unlock operations.
 */
class SharedPreferencesCampaignMissionProgressRepository(
    context: Context
) : CampaignMissionProgressRepository {

    private val applicationContext = context.applicationContext
    private val preferences = applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )
    private val legacyPreferences = applicationContext.getSharedPreferences(
        LEGACY_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val orderedMissionIds: List<String>
        get() = FirstContactMissionCatalog.missions
            .sortedBy { it.sequenceNumber }
            .map { it.definition.id }

    override fun load(): CampaignMissionProgress {
        val hasCurrentSave = preferences.contains(KEY_COMPLETED_MISSIONS)
        val persisted = if (hasCurrentSave) {
            preferences.getStringSet(KEY_COMPLETED_MISSIONS, emptySet()).orEmpty().toList()
        } else {
            migrateLegacyMissionOneCompletion()
        }
        return CampaignMissionProgression.restore(persisted, orderedMissionIds)
    }

    /**
     * Preserve Mission 01 completion from the earlier boolean-only save format.
     * The migration writes the new snapshot before returning it; if the write
     * fails, the in-memory result still reflects the legacy completion.
     */
    private fun migrateLegacyMissionOneCompletion(): List<String> {
        val legacyCompleted = legacyPreferences.getBoolean(LEGACY_MISSION_ONE_KEY, false)
        if (!legacyCompleted) return emptyList()

        val firstMissionId = orderedMissionIds.firstOrNull() ?: return emptyList()
        legacyPreferences.edit()
            .putStringSet(KEY_COMPLETED_MISSIONS, setOf(firstMissionId))
            .commit()
        return listOf(firstMissionId)
    }

    override fun save(progress: CampaignMissionProgress): Boolean {
        val sanitized = CampaignMissionProgression.restore(
            progress.completedMissionIds,
            orderedMissionIds
        )
        return preferences.edit()
            .putStringSet(KEY_COMPLETED_MISSIONS, sanitized.completedMissionIds.toSet())
            .commit()
    }

    private companion object {
        const val PREFERENCES_NAME = "aegis_campaign_mission_progress"
        const val LEGACY_PREFERENCES_NAME = "aegis_mission_progress"
        const val KEY_COMPLETED_MISSIONS = "first_contact_completed_missions"
        const val LEGACY_MISSION_ONE_KEY = "completed_fc_m01_first_signal"
    }
}
