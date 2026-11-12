package com.mazbayid.cybertrace.domain.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PythonCurriculumTest {

    @Test
    fun curriculumContainsAllFifteenChaptersInStableOrder() {
        assertEquals((0..14).map { it.toString().padStart(2, '0') },
            PythonCurriculum.chapters.map(CurriculumChapter::id))
    }

    @Test
    fun everyChapterHasKnownEarlierPrerequisites() {
        assertTrue(PythonCurriculum.hasValidPrerequisiteGraph())
    }

    @Test
    fun firstChapterIsAvailableToANewPlayer() {
        assertTrue(PythonCurriculum.isChapterUnlocked("00", emptySet()))
    }

    @Test
    fun laterChapterRequiresItsPreviousChapter() {
        assertFalse(PythonCurriculum.isChapterUnlocked("01", emptySet()))
        assertTrue(PythonCurriculum.isChapterUnlocked("01", setOf("00")))
    }

    @Test
    fun unknownChapterIsNeverUnlocked() {
        assertFalse(PythonCurriculum.isChapterUnlocked("99", emptySet()))
    }

    @Test
    fun firstLessonBelongsToTheIntroductoryChapter() {
        val lesson = PythonCurriculum.firstLesson

        assertEquals("00", lesson.chapterId)
        assertNotNull(lesson.steps.firstOrNull { it.exampleCode == "print(\"Signal acquired\")" })
        assertEquals("NEXUS connection ready", lesson.expectedPracticeOutput)
    }
}
