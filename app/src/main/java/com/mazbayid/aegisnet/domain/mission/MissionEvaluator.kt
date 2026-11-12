package com.mazbayid.aegisnet.domain.mission

data class MissionRunReceipt(
    val operationId: String,
    val sourceHash: String,
    val successful: Boolean
)

data class MissionSubmission(
    val missionId: String,
    val objectiveApproachIds: Map<String, String>,
    val objectiveOutcomeIds: Map<String, String>,
    val selectedEvidenceIds: Set<String>,
    val runReceipt: MissionRunReceipt? = null,
    val submittedSourceHash: String? = null
)

data class MissionCriterionResult(
    val objectiveId: String?,
    val code: String,
    val passed: Boolean,
    val message: String
)

data class MissionEvaluation(
    val missionId: String,
    val accepted: Boolean,
    val criteria: List<MissionCriterionResult>
)

/**
 * Deterministic SUBMIT evaluator. It validates semantic objectives and evidence
 * provenance; it never executes code and never treats RUN as mission completion.
 */
object MissionEvaluator {

    fun evaluate(
        definition: MissionDefinition,
        submission: MissionSubmission,
        availableEvidenceIds: Set<String>
    ): MissionEvaluation {
        val results = mutableListOf<MissionCriterionResult>()

        val definitionIssues = MissionDefinitionValidator.validate(definition)
        definitionIssues.forEach { issue ->
            results += criterion(
                objectiveId = null,
                code = "invalid_definition_${issue.code}",
                passed = false,
                message = issue.message
            )
        }

        if (definitionIssues.isNotEmpty()) {
            return MissionEvaluation(definition.id, accepted = false, criteria = results)
        }

        results += criterion(
            objectiveId = null,
            code = "mission_id_matches",
            passed = submission.missionId == definition.id,
            message = if (submission.missionId == definition.id) {
                "Submission belongs to this mission."
            } else {
                "Submission belongs to another mission."
            }
        )

        val objectiveIds = definition.objectives.map { it.id }.toSet()
        val submittedObjectiveIds =
            submission.objectiveApproachIds.keys + submission.objectiveOutcomeIds.keys
        val unknownObjectives = submittedObjectiveIds - objectiveIds
        results += criterion(
            objectiveId = null,
            code = "no_unknown_objectives",
            passed = unknownObjectives.isEmpty(),
            message = if (unknownObjectives.isEmpty()) {
                "Submission contains no unknown objective identifiers."
            } else {
                "Submission contains unknown objective identifiers: ${unknownObjectives.sorted().joinToString()}."
            }
        )

        val unknownEvidence = submission.selectedEvidenceIds - availableEvidenceIds
        results += criterion(
            objectiveId = null,
            code = "evidence_exists",
            passed = unknownEvidence.isEmpty(),
            message = if (unknownEvidence.isEmpty()) {
                "Every submitted evidence reference exists in the operation."
            } else {
                "Submission references evidence that is not available: ${unknownEvidence.sorted().joinToString()}."
            }
        )

        definition.objectives.forEach { objective ->
            val approachId = submission.objectiveApproachIds[objective.id]
            val approachAccepted = approachId != null && approachId in objective.acceptedApproachIds
            results += criterion(
                objectiveId = objective.id,
                code = "accepted_approach",
                passed = approachAccepted,
                message = if (approachAccepted) {
                    "The selected approach is accepted."
                } else {
                    "Choose one of the authored approaches for this objective."
                }
            )

            val outcomeId = submission.objectiveOutcomeIds[objective.id]
            val outcomeAccepted = outcomeId != null && outcomeId in objective.acceptedOutcomeIds
            results += criterion(
                objectiveId = objective.id,
                code = "accepted_outcome",
                passed = outcomeAccepted,
                message = if (outcomeAccepted) {
                    "The submitted outcome satisfies an accepted result."
                } else {
                    "The submitted outcome does not satisfy an accepted result."
                }
            )

            val missingEvidence = objective.requiredEvidenceIds - submission.selectedEvidenceIds
            val unavailableRequiredEvidence = objective.requiredEvidenceIds - availableEvidenceIds
            val evidencePassed = missingEvidence.isEmpty() && unavailableRequiredEvidence.isEmpty()
            results += criterion(
                objectiveId = objective.id,
                code = "required_evidence",
                passed = evidencePassed,
                message = when {
                    unavailableRequiredEvidence.isNotEmpty() ->
                        "Required evidence is not available in this operation: ${unavailableRequiredEvidence.sorted().joinToString()}."
                    missingEvidence.isNotEmpty() ->
                        "Required evidence was not included: ${missingEvidence.sorted().joinToString()}."
                    else -> "All required evidence is attached to the submission."
                }
            )

            if (objective.requiresSuccessfulRun) {
                val receipt = submission.runReceipt
                results += criterion(
                    objectiveId = objective.id,
                    code = "successful_run_exists",
                    passed = receipt != null && receipt.successful,
                    message = when {
                        receipt == null -> "A successful RUN record is required before submission."
                        !receipt.successful -> "The latest RUN record was not successful."
                        else -> "A successful RUN record is present."
                    }
                )

                results += criterion(
                    objectiveId = objective.id,
                    code = "run_operation_matches",
                    passed = receipt != null && receipt.operationId == definition.operationId,
                    message = if (receipt?.operationId == definition.operationId) {
                        "RUN record belongs to this operation."
                    } else {
                        "RUN record belongs to another operation or is missing."
                    }
                )

                val sourceMatches = receipt != null &&
                    receipt.sourceHash.isNotBlank() &&
                    submission.submittedSourceHash == receipt.sourceHash
                results += criterion(
                    objectiveId = objective.id,
                    code = "submitted_source_matches_run",
                    passed = sourceMatches,
                    message = if (sourceMatches) {
                        "Submitted source matches the exact successful RUN snapshot."
                    } else {
                        "Submitted source differs from the successful RUN snapshot or has no valid hash."
                    }
                )
            }
        }

        return MissionEvaluation(
            missionId = definition.id,
            accepted = results.all { it.passed },
            criteria = results
        )
    }

    private fun criterion(
        objectiveId: String?,
        code: String,
        passed: Boolean,
        message: String
    ) = MissionCriterionResult(objectiveId, code, passed, message)
}
