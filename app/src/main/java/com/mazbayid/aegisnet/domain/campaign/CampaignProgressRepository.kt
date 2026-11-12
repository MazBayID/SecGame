package com.mazbayid.aegisnet.domain.campaign

/**
 * Persistence boundary for campaign progress. Implementations must store a
 * complete snapshot and return false when the write is not confirmed.
 */
interface CampaignProgressRepository {
    fun load(): CampaignProgress
    fun save(progress: CampaignProgress): Boolean
}
