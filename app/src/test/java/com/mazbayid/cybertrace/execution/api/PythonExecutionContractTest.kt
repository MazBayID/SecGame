package com.mazbayid.cybertrace.execution.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PythonExecutionContractTest {

    @Test
    fun defaultLimitsStayWithinDocumentedBounds() {
        val limits = PythonExecutionLimits()

        assertTrue(limits.timeoutMillis in 100L..10_000L)
        assertTrue(limits.memoryLimitMiB in 16..256)
        assertTrue(limits.maxOutputBytes in 1_024..65_536)
        assertTrue(limits.maxSourceBytes in 1..32_768)
    }

    @Test
    fun rejectsTimeoutOutsideAllowedRange() {
        assertThrows(IllegalArgumentException::class.java) {
            PythonExecutionLimits(timeoutMillis = 50)
        }
    }

    @Test
    fun rejectsBlankWorkspaceId() {
        assertThrows(IllegalArgumentException::class.java) {
            PythonExecutionRequest(source = "print('hello')", workspaceId = " ")
        }
    }

    @Test
    fun sourceLimitUsesUtf8ByteCount() {
        val request = PythonExecutionRequest(
            source = "é",
            workspaceId = "mission-01",
            limits = PythonExecutionLimits(maxSourceBytes = 2)
        )

        assertEquals("é", request.source)

        assertThrows(IllegalArgumentException::class.java) {
            PythonExecutionRequest(
                source = "éé",
                workspaceId = "mission-01",
                limits = PythonExecutionLimits(maxSourceBytes = 2)
            )
        }
    }

    @Test
    fun diagnosticRequiresPositiveSourceLocation() {
        assertThrows(IllegalArgumentException::class.java) {
            PythonDiagnostic(
                kind = PythonDiagnosticKind.SYNTAX,
                message = "Invalid syntax",
                line = 0
            )
        }
    }

    @Test
    fun resultCannotHaveNegativeDuration() {
        assertThrows(IllegalArgumentException::class.java) {
            PythonExecutionResult(
                status = PythonExecutionStatus.RUNTIME_ERROR,
                durationMillis = -1
            )
        }
    }
}
