package com.mazbayid.aegisnet.domain.evidence

sealed interface EvidenceGraphMutation {
    data class Accepted(
        val graph: EvidenceGraph,
        val changed: Boolean
    ) : EvidenceGraphMutation

    data class Rejected(
        val issue: EvidenceGraphIssue
    ) : EvidenceGraphMutation
}

/**
 * Pure domain operations for editing an operation's evidence graph.
 *
 * Persistence is intentionally outside this class. Callers can store the returned graph
 * only after receiving Accepted; a rejected mutation never changes the supplied graph.
 */
object EvidenceGraphEditor {

    fun addEvidence(
        graph: EvidenceGraph,
        record: EvidenceRecord
    ): EvidenceGraphMutation {
        if (record.operationId != graph.operationId) {
            return reject("operation_mismatch", "Evidence belongs to another operation.")
        }

        val existing = graph.evidence.firstOrNull { it.id == record.id }
        if (existing != null) {
            return if (existing == record) accepted(graph, changed = false)
            else reject("evidence_id_conflict", "Evidence ID already exists with different content.")
        }

        return validateCandidate(graph.copy(evidence = graph.evidence + record))
    }

    fun addAnnotation(
        graph: EvidenceGraph,
        annotation: EvidenceAnnotation
    ): EvidenceGraphMutation {
        if (graph.evidence.none { it.id == annotation.evidenceId }) {
            return reject("missing_annotation_evidence", "Cannot annotate evidence that is not in this operation.")
        }

        val existing = graph.annotations.firstOrNull { it.id == annotation.id }
        if (existing != null) {
            return if (existing == annotation) accepted(graph, changed = false)
            else reject("annotation_id_conflict", "Annotation ID already exists with different content.")
        }

        return validateCandidate(graph.copy(annotations = graph.annotations + annotation))
    }

    fun addRelationship(
        graph: EvidenceGraph,
        relationship: EvidenceRelationship
    ): EvidenceGraphMutation {
        if (graph.evidence.none { it.id == relationship.fromEvidenceId } ||
            graph.evidence.none { it.id == relationship.toEvidenceId }
        ) {
            return reject("missing_relationship_evidence", "Both relationship endpoints must exist in this operation.")
        }

        val existing = graph.relationships.firstOrNull { it.id == relationship.id }
        if (existing != null) {
            return if (existing == relationship) accepted(graph, changed = false)
            else reject("relationship_id_conflict", "Relationship ID already exists with different content.")
        }

        return validateCandidate(graph.copy(relationships = graph.relationships + relationship))
    }

    /**
     * Explicitly confirms a player hypothesis. Merely adding a relationship never
     * promotes it; the caller must invoke this operation after a player-confirmed
     * action or an authored mission rule.
     */
    fun confirmHypothesis(
        graph: EvidenceGraph,
        relationshipId: String
    ): EvidenceGraphMutation {
        val index = graph.relationships.indexOfFirst { it.id == relationshipId }
        if (index < 0) {
            return reject("missing_relationship", "Relationship does not exist in this operation.")
        }

        val relationship = graph.relationships[index]
        if (relationship.status == EvidenceLinkStatus.CONFIRMED) {
            return accepted(graph, changed = false)
        }
        if (relationship.status != EvidenceLinkStatus.PLAYER_HYPOTHESIS) {
            return reject(
                "relationship_not_hypothesis",
                "Only a player hypothesis can be explicitly confirmed."
            )
        }

        val updated = relationship.copy(status = EvidenceLinkStatus.CONFIRMED)
        val relationships = graph.relationships.toMutableList().also { it[index] = updated }
        return validateCandidate(graph.copy(relationships = relationships))
    }

    /** Removes only player-owned, unconfirmed hypotheses; confirmed links are preserved. */
    fun removeHypothesis(
        graph: EvidenceGraph,
        relationshipId: String
    ): EvidenceGraphMutation {
        val relationship = graph.relationships.firstOrNull { it.id == relationshipId }
            ?: return reject("missing_relationship", "Relationship does not exist in this operation.")

        if (relationship.status == EvidenceLinkStatus.CONFIRMED) {
            return reject(
                "cannot_remove_confirmed_relationship",
                "Confirmed relationships cannot be removed as player hypotheses."
            )
        }

        return validateCandidate(
            graph.copy(relationships = graph.relationships.filterNot { it.id == relationshipId })
        )
    }

    fun saveClaim(
        graph: EvidenceGraph,
        claim: EvidenceClaim
    ): EvidenceGraphMutation {
        val existingIndex = graph.claims.indexOfFirst { it.id == claim.id }
        val updatedClaims = if (existingIndex >= 0) {
            if (graph.claims[existingIndex] == claim) return accepted(graph, changed = false)
            graph.claims.toMutableList().also { it[existingIndex] = claim }
        } else {
            graph.claims + claim
        }

        return validateCandidate(graph.copy(claims = updatedClaims))
    }

    private fun validateCandidate(candidate: EvidenceGraph): EvidenceGraphMutation {
        val issue = EvidenceGraphValidator.validate(candidate).firstOrNull()
        return if (issue == null) accepted(candidate, changed = true)
        else EvidenceGraphMutation.Rejected(issue)
    }

    private fun accepted(
        graph: EvidenceGraph,
        changed: Boolean
    ) = EvidenceGraphMutation.Accepted(graph, changed)

    private fun reject(
        code: String,
        message: String
    ) = EvidenceGraphMutation.Rejected(EvidenceGraphIssue(code, message))
}

data class EvidenceQuery(
    val types: Set<EvidenceType> = emptySet(),
    val sourceId: String? = null,
    val integrity: EvidenceIntegrity? = null,
    val discoveredOnly: Boolean = false,
    val eventTimeFromEpochMillis: Long? = null,
    val eventTimeToEpochMillis: Long? = null
)

/** Deterministic, in-memory filtering; storage and paging belong to the repository layer. */
object EvidenceGraphQuery {

    fun search(
        graph: EvidenceGraph,
        query: EvidenceQuery = EvidenceQuery()
    ): List<EvidenceRecord> {
        return graph.evidence
            .asSequence()
            .filter { query.types.isEmpty() || it.type in query.types }
            .filter { query.sourceId == null || it.sourceId == query.sourceId }
            .filter { query.integrity == null || it.integrity == query.integrity }
            .filter { !query.discoveredOnly || it.isDiscovered }
            .filter {
                query.eventTimeFromEpochMillis == null ||
                    (it.eventAtEpochMillis != null && it.eventAtEpochMillis >= query.eventTimeFromEpochMillis)
            }
            .filter {
                query.eventTimeToEpochMillis == null ||
                    (it.eventAtEpochMillis != null && it.eventAtEpochMillis <= query.eventTimeToEpochMillis)
            }
            .sortedWith(
                compareBy<EvidenceRecord> { it.eventAtEpochMillis ?: Long.MAX_VALUE }
                    .thenBy { it.id }
            )
            .toList()
    }
}
