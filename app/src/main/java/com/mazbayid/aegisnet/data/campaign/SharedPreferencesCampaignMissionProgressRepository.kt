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

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val orderedMissionIds: List<String>
        get() = FirstContactMissionCatalog.missions
            .sortedBy { it.sequenceNumber }
            .map { it.definition.id }

    override fun load(): CampaignMissionProgress {
        val persisted = preferences.getStringSet(KEY_COMPLETED_MISSIONS, emptySet())
            .orEmpty()
            .toList()
        return CampaignMissionProgression.restore(persisted, orderedMissionIds)
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
        const val KEY_COMPLETED_MISSIONS = "first_contact_completed_missions"
    }
}
