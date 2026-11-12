package com.mazbayid.cybertrace.domain.learning

import com.mazbayid.cybertrace.execution.api.PythonExecutionResult
import com.mazbayid.cybertrace.execution.api.PythonExecutionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PythonPracticeEvaluatorTest {

    @Test
    fun acceptsExactOutputWithPrintNewline() {
        val result = PythonExecutionResult(
            status = PythonExecutionStatus.COMPLETED,
            stdout = "NEXUS connection ready\n",
            durationMillis = 12
        )

        assertEquals(
            PracticeEvaluation.PASSED,
            PythonPracticeEvaluator.evaluate(result, "NEXUS connection ready")
        )
    }

    @Test
    fun normalizesWindowsLineEnding() {
        val result = PythonExecutionResult(
            status = PythonExecutionStatus.COMPLETED,
            stdout = "Signal acquired\r\n",
            durationMillis = 8
        )

        assertEquals(
            PracticeEvaluation.PASSED,
            PythonPracticeEvaluator.evaluate(result, "Signal acquired")
        )
    }

    @Test
    fun rejectsDifferentOutput() {
        val result = PythonExecutionResult(
            status = PythonExecutionStatus.COMPLETED,
            stdout = "Wrong signal\n",
            durationMillis = 5
        )

        assertEquals(
            PracticeEvaluation.OUTPUT_MISMATCH,
            PythonPracticeEvaluator.evaluate(result, "Signal acquired")
        )
    }

    @Test
    fun doesNotPassWhenExecutionFailed() {
        val result = PythonExecutionResult(
            status = PythonExecutionStatus.RUNTIME_ERROR,
            stderr = "NameError",
            durationMillis = 4
        )

        assertEquals(
            PracticeEvaluation.EXECUTION_FAILED,
            PythonPracticeEvaluator.evaluate(result, "Signal acquired")
        )
    }

    @Test
    fun truncatedOutputCannotPass() {
        val result = PythonExecutionResult(
            status = PythonExecutionStatus.COMPLETED,
            stdout = "Signal acquired",
            durationMillis = 4,
            outputTruncated = true
        )

        assertEquals(
            PracticeEvaluation.OUTPUT_TRUNCATED,
            PythonPracticeEvaluator.evaluate(result, "Signal acquired")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEmptyExpectedOutput() {
        val result = PythonExecutionResult(
            status = PythonExecutionStatus.COMPLETED,
            stdout = "",
            durationMillis = 1
        )

        PythonPracticeEvaluator.evaluate(result, "")
    }
}
