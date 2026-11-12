package com.mazbayid.aegisnet.domain.mission

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionEvaluatorTest {

    private val definition = MissionDefinition(
        id = "mission_first_signal",
        operationId = "operation_first_contact_01",
        title = "Sinyal Pertama",
        objectives = listOf(
            MissionObjective(
                id = "identify_signal",
                description = "Identify the signal using an authorized approach.",
                acceptedApproachIds = setOf("inspect_message", "process_authorized_log"),
                acceptedOutcomeIds = setOf("signal_identified"),
                requiredEvidenceIds = setOf("evidence_signal_01"),
                requiresSuccessfulRun = true
            )
        )
    )

    private fun submission(
        approach: String = "inspect_message",
        outcome: String = "signal_identified",
        evidence: Set<String> = setOf("evidence_signal_01"),
        receipt: MissionRunReceipt? = MissionRunReceipt(
            operationId = definition.operationId,
            sourceHash = "sha256:source-v1",
            successful = true
        ),
        submittedHash: String? = "sha256:source-v1"
    ) = MissionSubmission(
        missionId = definition.id,
        objectiveApproachIds = mapOf("identify_signal" to approach),
        objectiveOutcomeIds = mapOf("identify_signal" to outcome),
        selectedEvidenceIds = evidence,
        runReceipt = receipt,
        submittedSourceHash = submittedHash
    )

    @Test
    fun acceptsAnyAuthoredApproachWithRequiredProof() {
        val result = MissionEvaluator.evaluate(
            definition,
            submission(approach = "process_authorized_log"),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertTrue(result.accepted)
        assertTrue(result.criteria.all { it.passed })
    }

    @Test
    fun rejectsUnacceptedApproachAndOutcome() {
        val result = MissionEvaluator.evaluate(
            definition,
            submission(approach = "invented_shortcut", outcome = "unsupported_claim"),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertFalse(result.accepted)
        assertTrue(result.criteria.any { it.code == "accepted_approach" && !it.passed })
        assertTrue(result.criteria.any { it.code == "accepted_outcome" && !it.passed })
    }

    @Test
    fun rejectsMissingRequiredEvidence() {
        val result = MissionEvaluator.evaluate(
            definition,
            submission(evidence = emptySet()),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertFalse(result.accepted)
        assertTrue(result.criteria.any { it.code == "required_evidence" && !it.passed })
    }

    @Test
    fun rejectsEvidenceThatDoesNotExistInOperation() {
        val result = MissionEvaluator.evaluate(
            definition,
            submission(evidence = setOf("fabricated_evidence")),
            availableEvidenceIds = emptySet()
        )

        assertFalse(result.accepted)
        assertTrue(result.criteria.any { it.code == "evidence_exists" && !it.passed })
        assertTrue(result.criteria.any { it.code == "required_evidence" && !it.passed })
    }

    @Test
    fun rejectsSubmissionForAnotherMission() {
        val result = MissionEvaluator.evaluate(
            definition,
            submission().copy(missionId = "another_mission"),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertFalse(result.accepted)
        assertTrue(result.criteria.any { it.code == "mission_id_matches" && !it.passed })
    }

    @Test
    fun successfulRunAloneDoesNotCompleteMission() {
        val result = MissionEvaluator.evaluate(
            definition,
            submission(outcome = "unsupported_claim"),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertFalse(result.accepted)
        assertTrue(result.criteria.any { it.code == "successful_run_exists" && it.passed })
        assertTrue(result.criteria.any { it.code == "accepted_outcome" && !it.passed })
    }

    @Test
    fun rejectsMissingOrFailedRun() {
        val missing = MissionEvaluator.evaluate(
            definition,
            submission(receipt = null, submittedHash = null),
            availableEvidenceIds = setOf("evidence_signal_01")
        )
        val failed = MissionEvaluator.evaluate(
            definition,
            submission(
                receipt = MissionRunReceipt(definition.operationId, "sha256:source-v1", false)
            ),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertFalse(missing.accepted)
        assertFalse(failed.accepted)
    }

    @Test
    fun rejectsRunFromAnotherOperation() {
        val result = MissionEvaluator.evaluate(
            definition,
            submission(
                receipt = MissionRunReceipt("another_operation", "sha256:source-v1", true)
            ),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertFalse(result.accepted)
        assertTrue(result.criteria.any { it.code == "run_operation_matches" && !it.passed })
    }

    @Test
    fun rejectsSubmittedSourceDifferentFromSuccessfulRun() {
        val result = MissionEvaluator.evaluate(
            definition,
            submission(submittedHash = "sha256:source-v2"),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertFalse(result.accepted)
        assertTrue(result.criteria.any { it.code == "submitted_source_matches_run" && !it.passed })
    }

    @Test
    fun missionWithoutTechnicalRunRequirementDoesNotDemandRunReceipt() {
        val nonTechnical = definition.copy(
            objectives = definition.objectives.map { it.copy(requiresSuccessfulRun = false) }
        )
        val result = MissionEvaluator.evaluate(
            nonTechnical,
            submission(receipt = null, submittedHash = null),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertTrue(result.accepted)
        assertFalse(result.criteria.any { it.code == "successful_run_exists" })
    }

    @Test
    fun invalidMissionDefinitionCannotBeEvaluatedAsAccepted() {
        val invalid = definition.copy(objectives = emptyList())
        val result = MissionEvaluator.evaluate(
            invalid,
            submission(),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertFalse(result.accepted)
        assertTrue(result.criteria.any { it.code == "invalid_definition_missing_objectives" })
    }
    @Test
    fun requiresAuthoredOutputTokensFromSuccessfulRun() {
        val outputDefinition = definition.copy(
            objectives = definition.objectives.map {
                it.copy(requiredOutputContains = setOf("ACCESS GRANTED"))
            }
        )
        val missing = MissionEvaluator.evaluate(
            outputDefinition,
            submission(
                receipt = MissionRunReceipt(
                    definition.operationId,
                    "sha256:source-v1",
                    true,
                    "different output"
                )
            ),
            availableEvidenceIds = setOf("evidence_signal_01")
        )
        val present = MissionEvaluator.evaluate(
            outputDefinition,
            submission(
                receipt = MissionRunReceipt(
                    definition.operationId,
                    "sha256:source-v1",
                    true,
                    "ACCESS GRANTED"
                )
            ),
            availableEvidenceIds = setOf("evidence_signal_01")
        )

        assertFalse(missing.accepted)
        assertTrue(missing.criteria.any { it.code == "required_run_output" && !it.passed })
        assertTrue(present.accepted)
    }

}

