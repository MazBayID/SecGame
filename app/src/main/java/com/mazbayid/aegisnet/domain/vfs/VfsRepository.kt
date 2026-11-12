package com.mazbayid.aegisnet.domain.vfs

/**
 * Storage boundary for game-controlled virtual workspaces.
 * Personal data is global to the player; mission data is scoped by operation ID.
 */
interface VfsRepository {
    fun loadPersonal(): VfsWorkspaceSnapshot
    fun savePersonal(snapshot: VfsWorkspaceSnapshot): Boolean

    fun loadMission(operationId: String): VfsWorkspaceSnapshot
    fun saveMission(operationId: String, snapshot: VfsWorkspaceSnapshot): Boolean
}
