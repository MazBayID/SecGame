package com.mazbayid.aegisnet.data.vfs

import android.content.Context
import com.mazbayid.aegisnet.domain.vfs.VfsRepository
import com.mazbayid.aegisnet.domain.vfs.VfsSnapshotCodec
import com.mazbayid.aegisnet.domain.vfs.VfsWorkspaceSnapshot

/**
 * Offline SharedPreferences adapter for small game virtual workspaces.
 * This writes only serialized game data; it never creates host filesystem paths.
 */
class SharedPreferencesVfsRepository(
    context: Context
) : VfsRepository {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun loadPersonal(): VfsWorkspaceSnapshot =
        load(KEY_PERSONAL)

    override fun savePersonal(snapshot: VfsWorkspaceSnapshot): Boolean =
        save(KEY_PERSONAL, snapshot)

    override fun loadMission(operationId: String): VfsWorkspaceSnapshot =
        load(missionKey(operationId))

    override fun saveMission(operationId: String, snapshot: VfsWorkspaceSnapshot): Boolean =
        save(missionKey(operationId), snapshot)

    private fun load(key: String): VfsWorkspaceSnapshot {
        val serialized = preferences.getString(key, null) ?: return VfsWorkspaceSnapshot()
        return try {
            VfsSnapshotCodec.decode(serialized)
        } catch (error: Exception) {
            // Never present corrupted data as an empty workspace or overwrite it silently.
            throw IllegalStateException(
                "Saved virtual workspace is corrupted; the original save was preserved.",
                error
            )
        }
    }

    private fun save(key: String, snapshot: VfsWorkspaceSnapshot): Boolean {
        val serialized = runCatching { VfsSnapshotCodec.encode(snapshot) }.getOrNull()
            ?: return false
        return preferences.edit().putString(key, serialized).commit()
    }

    private fun missionKey(operationId: String): String {
        require(operationId.matches(Regex("[A-Za-z0-9_-]+"))) {
            "Operation ID must contain only letters, digits, underscores, and hyphens."
        }
        return "$KEY_MISSION_PREFIX$operationId"
    }

    private companion object {
        const val PREFERENCES_NAME = "aegis_virtual_workspaces"
        const val KEY_PERSONAL = "personal_workspace_v1"
        const val KEY_MISSION_PREFIX = "mission_workspace_v1_"
    }
}
