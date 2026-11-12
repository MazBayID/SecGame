package com.mazbayid.aegisnet.domain.evidence

/**
 * Domain-only evidence model. It intentionally has no Android or Compose dependencies.
 *
 * Source evidence is immutable from the player's perspective. Player annotations and
 * relationships are stored separately so an investigation cannot silently rewrite a source.
 */
data class EvidenceRecord(
    val id: String,
    val title: String,
    val description: String,
    val type: EvidenceType,
    val sourceId: String,
    val sourceLabel: String,
    val sourceReference: String? = null,
    val createdAtEpochMillis: Long? = null,
    val eventAtEpochMillis: Long? = null,
    val integrity: EvidenceIntegrity = EvidenceIntegrity.UNKNOWN,
    val access: EvidenceAccess = EvidenceAccess.UNCLASSIFIED,
    val origin: EvidenceOrigin = EvidenceOrigin.ORIGINAL,
    val operationId: String,
    val annotations: List<String> = emptyList(),
    val isDiscovered: Boolean = false
)

enum class EvidenceType {
    MESSAGE,
    FILE,
    SYSTEM_LOG,
    DATABASE_RECORD,
    TESTIMONY,
    OBSERVATION,
    DATASET,
    REPORT
}

enum class EvidenceIntegrity {
    VERIFIED,
    QUESTIONABLE,
    COMPROMISED,
    UNKNOWN
}

enum class EvidenceAccess {
    PUBLIC,
    INTERNAL,
    RESTRICTED,
    CLASSIFIED,
    UNCLASSIFIED
}

enum class EvidenceOrigin {
    ORIGINAL,
    DERIVED,
    COPY,
    PLAYER_AUTHORED
}

enum class EvidenceRelationType {
    SUPPORTS,
    CONTRADICTS,
    SAME_ENTITY,
    PRECEDES,
    FOLLOWS,
    DERIVED_FROM,
    COPIED_FROM,
    CONFLICTS_WITH,
    PATTERN_WITH
}

enum class EvidenceLinkStatus {
    CONFIRMED,
    PLAYER_HYPOTHESIS,
    UNRESOLVED
}

enum class EvidenceConfidence {
    LOW,
    MEDIUM,
    HIGH,
    UNASSESSED
}

data class EvidenceRelationship(
    val id: String,
    val fromEvidenceId: String,
    val toEvidenceId: String,
    val type: EvidenceRelationType,
    val status: EvidenceLinkStatus,
    val confidence: EvidenceConfidence = EvidenceConfidence.UNASSESSED,
    val note: String? = null
)

enum class ClaimStatus {
    PROPOSED,
    SUPPORTED,
    DISPUTED,
    INSUFFICIENT
}

data class EvidenceClaim(
    val id: String,
    val statement: String,
    val supportingEvidenceIds: Set<String> = emptySet(),
    val contradictingEvidenceIds: Set<String> = emptySet(),
    val confidence: EvidenceConfidence = EvidenceConfidence.UNASSESSED,
    val status: ClaimStatus = ClaimStatus.PROPOSED
)

data class EvidenceGraph(
    val operationId: String,
    val evidence: List<EvidenceRecord> = emptyList(),
    val relationships: List<EvidenceRelationship> = emptyList(),
    val claims: List<EvidenceClaim> = emptyList()
)

data class EvidenceGraphIssue(
    val code: String,
    val message: String
)

/**
 * Deterministic structural validation. This does not decide whether a relationship
 * is factually true; that decision belongs to authored mission rules or the player.
 */
object EvidenceGraphValidator {

    fun validate(graph: EvidenceGraph): List<EvidenceGraphIssue> {
        val issues = mutableListOf<EvidenceGraphIssue>()
        val evidenceIds = graph.evidence.map { it.id }
        val knownEvidenceIds = evidenceIds.toSet()
        val relationshipIds = graph.relationships.map { it.id }
        val claimIds = graph.claims.map { it.id }

        if (graph.operationId.isBlank()) {
            issues += issue("blank_operation_id", "Operation ID must not be blank.")
        }

        addDuplicateIssues(evidenceIds, "duplicate_evidence_id", issues)
        addDuplicateIssues(relationshipIds, "duplicate_relationship_id", issues)
        addDuplicateIssues(claimIds, "duplicate_claim_id", issues)

        graph.evidence.forEach { record ->
            if (record.id.isBlank()) issues += issue("blank_evidence_id", "Evidence ID must not be blank.")
            if (record.title.isBlank()) issues += issue("blank_evidence_title", "Evidence title must not be blank: ${record.id}.")
            if (record.sourceId.isBlank()) issues += issue("blank_source_id", "Source ID must not be blank: ${record.id}.")
            if (record.operationId != graph.operationId) {
                issues += issue("operation_mismatch", "Evidence ${record.id} belongs to another operation.")
            }
        }

        val seenRelationshipKeys = mutableSetOf<String>()
        graph.relationships.forEach { link ->
            if (link.id.isBlank()) issues += issue("blank_relationship_id", "Relationship ID must not be blank.")
            if (link.fromEvidenceId !in knownEvidenceIds) {
                issues += issue("missing_relationship_source", "Relationship ${link.id} references missing source evidence.")
            }
            if (link.toEvidenceId !in knownEvidenceIds) {
                issues += issue("missing_relationship_target", "Relationship ${link.id} references missing target evidence.")
            }
            if (link.fromEvidenceId == link.toEvidenceId) {
                issues += issue("self_relationship", "Relationship ${link.id} cannot connect evidence to itself.")
            }

            val key = "${link.fromEvidenceId}|${link.toEvidenceId}|${link.type}|${link.status}"
            if (!seenRelationshipKeys.add(key)) {
                issues += issue("duplicate_relationship", "Duplicate relationship: ${link.id}.")
            }
        }

        graph.claims.forEach { claim ->
            if (claim.id.isBlank()) issues += issue("blank_claim_id", "Claim ID must not be blank.")
            if (claim.statement.isBlank()) issues += issue("blank_claim_statement", "Claim statement must not be blank: ${claim.id}.")

            val overlap = claim.supportingEvidenceIds intersect claim.contradictingEvidenceIds
            if (overlap.isNotEmpty()) {
                issues += issue(
                    "claim_evidence_overlap",
                    "Claim ${claim.id} cannot classify the same evidence as both supporting and contradicting."
                )
            }

            (claim.supportingEvidenceIds + claim.contradictingEvidenceIds)
                .filterNot { it in knownEvidenceIds }
                .forEach { missingId ->
                    issues += issue("missing_claim_evidence", "Claim ${claim.id} references missing evidence: $missingId.")
                }

            if (claim.status == ClaimStatus.SUPPORTED && claim.supportingEvidenceIds.isEmpty()) {
                issues += issue("supported_claim_without_evidence", "Supported claim ${claim.id} needs supporting evidence.")
            }
        }

        return issues
    }

    private fun addDuplicateIssues(
        ids: List<String>,
        code: String,
        issues: MutableList<EvidenceGraphIssue>
    ) {
        ids.groupingBy { it }
            .eachCount()
            .filter { (id, count) -> id.isNotBlank() && count > 1 }
            .keys
            .forEach { id -> issues += issue(code, "Duplicate ID: $id.") }
    }

    private fun issue(code: String, message: String) = EvidenceGraphIssue(code, message)
}
