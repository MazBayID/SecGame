package com.mazbayid.aegisnet.data.campaign

import android.content.Context

/**
 * Offline read receipts for authored Messenger messages.
 *
 * Message IDs are stable campaign identifiers in the form
 * "<thread-id>:<zero-based-message-index>". Read state is deliberately
 * separate from campaign unlock progress.
 */
class SharedPreferencesMessengerReadStateRepository(
    context: Context
) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun loadReadMessageIds(): Set<String> =
        preferences.getStringSet(KEY_READ_MESSAGES, emptySet())
            .orEmpty()
            .filter { it.isNotBlank() && ':' in it }
            .toSet()

    /**
     * Mark a collection of authored messages as read.
     * Returns false if the local save could not be committed.
     */
    fun markRead(messageIds: Collection<String>): Boolean {
        if (messageIds.isEmpty()) return true
        val validIds = messageIds.filter { it.isNotBlank() && ':' in it }
        if (validIds.isEmpty()) return true
        val updated = loadReadMessageIds() + validIds
        return preferences.edit()
            .putStringSet(KEY_READ_MESSAGES, updated)
            .commit()
    }

    private companion object {
        const val PREFERENCES_NAME = "aegis_messenger_state"
        const val KEY_READ_MESSAGES = "read_message_ids"
    }
}
