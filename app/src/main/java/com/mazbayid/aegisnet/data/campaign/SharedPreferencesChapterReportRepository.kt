package com.mazbayid.aegisnet.data.campaign

import android.content.Context

/**
 * Persists the player's explicit acknowledgement of the First Contact chapter report.
 */
class SharedPreferencesChapterReportRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun isAcknowledged(): Boolean =
        preferences.getBoolean(KEY_ACKNOWLEDGED, false)

    fun acknowledge(): Boolean =
        preferences.edit().putBoolean(KEY_ACKNOWLEDGED, true).commit()

    private companion object {
        const val PREFERENCES_NAME = "aegis_chapter_report"
        const val KEY_ACKNOWLEDGED = "first_contact_report_acknowledged"
    }
}
