package com.mazbayid.aegisnet.data.campaign

import android.content.Context
import com.mazbayid.aegisnet.domain.campaign.CampaignProgress
import com.mazbayid.aegisnet.domain.campaign.CampaignProgressRepository
import com.mazbayid.aegisnet.domain.campaign.CampaignProgression
import com.mazbayid.aegisnet.domain.campaign.CampaignStep

/**
 * Small offline campaign-save adapter. The domain remains Android-independent.
 * SharedPreferences is sufficient for this compact sequence; structured
 * evidence data will use its own versioned repository/database.
 */
class SharedPreferencesCampaignProgressRepository(
    context: Context
) : CampaignProgressRepository {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun load(): CampaignProgress {
        val names = preferences.getStringSet(KEY_COMPLETED_STEPS, emptySet()).orEmpty()
        val restored = names.mapNotNull { name ->
            runCatching { CampaignStep.valueOf(name) }.getOrNull()
        }.toSet()
        return CampaignProgression.restore(restored)
    }

    override fun save(progress: CampaignProgress): Boolean {
        val names = progress.completedSteps.map { it.name }.toSet()
        return preferences.edit()
            .putStringSet(KEY_COMPLETED_STEPS, names)
            .commit()
    }

    private companion object {
        const val PREFERENCES_NAME = "aegis_campaign_progress"
        const val KEY_COMPLETED_STEPS = "first_contact_completed_steps"
    }
}
