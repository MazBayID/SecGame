package com.mazbayid.cybertrace.domain.learning

/**
 * In-memory progress for one lesson.
 *
 * Persistence is deliberately handled by a later data-layer milestone. This model
 * keeps progression rules independent from Compose and from the Python runtime.
 */
data class LessonProgress(
    val lessonId: String,
    val furthestViewedStepIndex: Int = -1,
    val practiceAttempts: Int = 0,
    val practicePassed: Boolean = false
) {
    init {
        require(lessonId.isNotBlank()) { "Lesson ID must not be blank." }
        require(furthestViewedStepIndex >= -1) {
            "Furthest viewed step index cannot be below -1."
        }
        require(practiceAttempts >= 0) { "Practice attempts cannot be negative." }
        require(!practicePassed || practiceAttempts > 0) {
            "Practice cannot be passed before an attempt."
        }
    }

    /**
     * Records that a player opened a valid lesson step.
     * Moving backwards never erases previously viewed progress.
     */
    fun recordStepViewed(stepIndex: Int, stepCount: Int): LessonProgress {
        require(stepCount > 0) { "A lesson must contain at least one step." }
        require(stepIndex in 0 until stepCount) { "Step index is outside this lesson." }

        return copy(
            furthestViewedStepIndex = maxOf(furthestViewedStepIndex, stepIndex)
        )
    }

    /** A failed attempt remains visible in the attempt count; a pass is permanent. */
    fun recordPracticeAttempt(evaluation: PracticeEvaluation): LessonProgress {
        return copy(
            practiceAttempts = practiceAttempts + 1,
            practicePassed = practicePassed || evaluation == PracticeEvaluation.PASSED
        )
    }

    fun isCompleted(stepCount: Int): Boolean {
        require(stepCount > 0) { "A lesson must contain at least one step." }
        return furthestViewedStepIndex == stepCount - 1 && practicePassed
    }
}
