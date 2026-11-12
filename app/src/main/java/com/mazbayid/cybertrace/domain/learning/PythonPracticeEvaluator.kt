package com.mazbayid.cybertrace.domain.learning

import com.mazbayid.cybertrace.execution.api.PythonExecutionResult
import com.mazbayid.cybertrace.execution.api.PythonExecutionStatus

enum class PracticeEvaluation {
    PASSED,
    OUTPUT_MISMATCH,
    EXECUTION_FAILED,
    OUTPUT_TRUNCATED
}

/**
 * Evaluates a beginner output exercise using the real interpreter result.
 *
 * This policy never executes or parses player code. It only compares the bounded
 * output returned by the Python execution engine with the lesson's expected output.
 */
object PythonPracticeEvaluator {
    fun evaluate(
        result: PythonExecutionResult,
        expectedOutput: String
    ): PracticeEvaluation {
        require(expectedOutput.isNotEmpty()) { "Expected output must not be empty." }

        if (result.outputTruncated || result.status == PythonExecutionStatus.OUTPUT_LIMIT) {
            return PracticeEvaluation.OUTPUT_TRUNCATED
        }

        if (result.status != PythonExecutionStatus.COMPLETED) {
            return PracticeEvaluation.EXECUTION_FAILED
        }

        return if (normalizeOutput(result.stdout) == normalizeOutput(expectedOutput)) {
            PracticeEvaluation.PASSED
        } else {
            PracticeEvaluation.OUTPUT_MISMATCH
        }
    }

    private fun normalizeOutput(value: String): String {
        val unixLineEndings = value.replace("\r\n", "\n").replace("\r", "\n")
        return if (unixLineEndings.endsWith("\n")) {
            unixLineEndings.dropLast(1)
        } else {
            unixLineEndings
        }
    }
}
