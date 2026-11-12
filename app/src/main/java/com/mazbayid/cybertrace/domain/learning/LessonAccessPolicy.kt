package com.mazbayid.cybertrace.domain.learning

/**
 * Determines whether a lesson may be opened from the player's completed lessons.
 *
 * This policy is deliberately independent from Android and the UI so it can be
 * tested without launching an emulator.
 */
data class LessonRequirement(
    val id: String,
    val prerequisiteIds: Set<String> = emptySet()
)

object LessonAccessPolicy {
    /**
     * A lesson is available when every prerequisite has been completed.
     * Lessons without prerequisites are available to a new player.
     */
    fun isUnlocked(
        lesson: LessonRequirement,
        completedLessonIds: Set<String>
    ): Boolean {
        return lesson.prerequisiteIds.all(completedLessonIds::contains)
    }
}
