package com.mazbayid.cybertrace.domain.learning

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonAccessPolicyTest {

    @Test
    fun lessonWithoutPrerequisitesIsUnlockedForNewPlayer() {
        val lesson = LessonRequirement(id = "python-00")

        assertTrue(LessonAccessPolicy.isUnlocked(lesson, emptySet()))
    }

    @Test
    fun lessonRemainsLockedUntilEveryPrerequisiteIsCompleted() {
        val lesson = LessonRequirement(
            id = "python-02",
            prerequisiteIds = setOf("python-00", "python-01")
        )

        assertFalse(
            LessonAccessPolicy.isUnlocked(
                lesson,
                completedLessonIds = setOf("python-00")
            )
        )
    }

    @Test
    fun lessonUnlocksWhenEveryPrerequisiteIsCompleted() {
        val lesson = LessonRequirement(
            id = "python-02",
            prerequisiteIds = setOf("python-00", "python-01")
        )

        assertTrue(
            LessonAccessPolicy.isUnlocked(
                lesson,
                completedLessonIds = setOf("python-00", "python-01")
            )
        )
    }
}
