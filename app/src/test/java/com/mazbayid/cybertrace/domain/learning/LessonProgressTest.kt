package com.mazbayid.cybertrace.domain.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonProgressTest {

    @Test
    fun newLessonStartsWithoutViewedStepsOrAttempts() {
        val progress = LessonProgress("00.01")

        assertEquals(-1, progress.furthestViewedStepIndex)
        assertEquals(0, progress.practiceAttempts)
        assertFalse(progress.practicePassed)
        assertFalse(progress.isCompleted(stepCount = 4))
    }

    @Test
    fun viewingLaterStepRecordsFurthestProgress() {
        val progress = LessonProgress("00.01")
            .recordStepViewed(stepIndex = 2, stepCount = 4)

        assertEquals(2, progress.furthestViewedStepIndex)
    }

    @Test
    fun movingBackDoesNotEraseFurthestProgress() {
        val progress = LessonProgress("00.01")
            .recordStepViewed(stepIndex = 3, stepCount = 4)
            .recordStepViewed(stepIndex = 1, stepCount = 4)

        assertEquals(3, progress.furthestViewedStepIndex)
    }

    @Test
    fun failedPracticeAttemptDoesNotCompleteLesson() {
        val progress = LessonProgress("00.01")
            .recordStepViewed(stepIndex = 3, stepCount = 4)
            .recordPracticeAttempt(PracticeEvaluation.OUTPUT_MISMATCH)

        assertEquals(1, progress.practiceAttempts)
        assertFalse(progress.practicePassed)
        assertFalse(progress.isCompleted(stepCount = 4))
    }

    @Test
    fun lessonCompletesOnlyAfterAllStepsAndPassedPractice() {
        val viewed = LessonProgress("00.01")
            .recordStepViewed(stepIndex = 3, stepCount = 4)

        assertFalse(viewed.isCompleted(stepCount = 4))

        val passed = viewed.recordPracticeAttempt(PracticeEvaluation.PASSED)
        assertTrue(passed.isCompleted(stepCount = 4))
    }

    @Test
    fun passingPracticeIsNotLostAfterLaterFailedAttempt() {
        val progress = LessonProgress("00.01")
            .recordPracticeAttempt(PracticeEvaluation.PASSED)
            .recordPracticeAttempt(PracticeEvaluation.EXECUTION_FAILED)

        assertEquals(2, progress.practiceAttempts)
        assertTrue(progress.practicePassed)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsStepOutsideLesson() {
        LessonProgress("00.01").recordStepViewed(stepIndex = 4, stepCount = 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPassedStateWithoutAttempt() {
        LessonProgress("00.01", practicePassed = true)
    }
}
