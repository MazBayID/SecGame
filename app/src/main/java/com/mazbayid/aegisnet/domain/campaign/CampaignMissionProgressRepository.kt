package com.mazbayid.aegisnet.domain.campaign

/**
 * Persistence boundary for ordered mission completion snapshots.
 * Implementations must confirm writes before callers advance the campaign UI.
 */
interface CampaignMissionProgressRepository {
    fun load(): CampaignMissionProgress
    fun save(progress: CampaignMissionProgress): Boolean
}
