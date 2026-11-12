package com.mazbayid.aegisnet.domain.mission

/**
 * Authored requirements for one operation. Accepted approaches and outcomes
 * are semantic identifiers, not exact command strings or source-code snippets.
 */
data class MissionObjective(
    val id: String,
    val description: String,
    val acceptedApproachIds: Set<String>,
    val acceptedOutcomeIds: Set<String>,
    val requiredEvidenceIds: Set<String> = emptySet(),
    val requiresSuccessfulRun: Boolean = false
)

data class MissionDefinition(
    val id: String,
    val operationId: String,
    val title: String,
    val objectives: List<MissionObjective>
)

data class MissionDefinitionIssue(
    val code: String,
    val message: String
)

object MissionDefinitionValidator {

    fun validate(definition: MissionDefinition): List<MissionDefinitionIssue> {
        val issues = mutableListOf<MissionDefinitionIssue>()

        if (definition.id.isBlank()) {
            issues += issue("blank_mission_id", "Mission ID must not be blank.")
        }
        if (definition.operationId.isBlank()) {
            issues += issue("blank_operation_id", "Operation ID must not be blank.")
        }
        if (definition.title.isBlank()) {
            issues += issue("blank_mission_title", "Mission title must not be blank.")
        }
        if (definition.objectives.isEmpty()) {
            issues += issue("missing_objectives", "A mission must define at least one objective.")
        }

        val duplicateIds = definition.objectives
            .groupingBy { it.id }
            .eachCount()
            .filter { (id, count) -> id.isNotBlank() && count > 1 }
            .keys
        duplicateIds.forEach { id ->
            issues += issue("duplicate_objective_id", "Objective ID is duplicated: $id.")
        }

        definition.objectives.forEach { objective ->
            if (objective.id.isBlank()) {
                issues += issue("blank_objective_id", "Objective ID must not be blank.")
            }
            if (objective.description.isBlank()) {
                issues += issue("blank_objective_description", "Objective description must not be blank: ${objective.id}.")
            }
            if (objective.acceptedApproachIds.isEmpty() ||
                objective.acceptedApproachIds.any { it.isBlank() }
            ) {
                issues += issue("invalid_accepted_approaches", "Objective ${objective.id} needs non-blank accepted approaches.")
            }
            if (objective.acceptedOutcomeIds.isEmpty() ||
                objective.acceptedOutcomeIds.any { it.isBlank() }
            ) {
                issues += issue("invalid_accepted_outcomes", "Objective ${objective.id} needs non-blank accepted outcomes.")
            }
            if (objective.requiredEvidenceIds.any { it.isBlank() }) {
                issues += issue("blank_required_evidence_id", "Objective ${objective.id} contains a blank evidence ID.")
            }
        }

        return issues
    }

    private fun issue(code: String, message: String) = MissionDefinitionIssue(code, message)
}
