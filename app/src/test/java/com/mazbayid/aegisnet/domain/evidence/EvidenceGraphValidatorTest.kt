package com.mazbayid.aegisnet.domain.evidence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceGraphValidatorTest {

    private fun evidence(id: String) = EvidenceRecord(
        id = id,
        title = "Record $id",
        description = "Test evidence",
        type = EvidenceType.FILE,
        sourceId = "source-$id",
        sourceLabel = "Test source",
        operationId = "op-001"
    )

    @Test
    fun validGraphHasNoIssues() {
        val graph = EvidenceGraph(
            operationId = "op-001",
            evidence = listOf(evidence("ev-1"), evidence("ev-2")),
            relationships = listOf(
                EvidenceRelationship(
                    id = "rel-1",
                    fromEvidenceId = "ev-1",
                    toEvidenceId = "ev-2",
                    type = EvidenceRelationType.SUPPORTS,
                    status = EvidenceLinkStatus.PLAYER_HYPOTHESIS
                )
            ),
            claims = listOf(
                EvidenceClaim(
                    id = "claim-1",
                    statement = "The two records may refer to the same event.",
                    supportingEvidenceIds = setOf("ev-1"),
                    confidence = EvidenceConfidence.LOW
                )
            )
        )

        assertTrue(EvidenceGraphValidator.validate(graph).isEmpty())
    }

    @Test
    fun detectsDuplicateEvidenceIds() {
        val graph = EvidenceGraph(
            operationId = "op-001",
            evidence = listOf(evidence("ev-1"), evidence("ev-1"))
        )

        val codes = EvidenceGraphValidator.validate(graph).map { it.code }

        assertTrue(codes.contains("duplicate_evidence_id"))
    }

    @Test
    fun detectsMissingRelationshipEndpointAndSelfLink() {
        val graph = EvidenceGraph(
            operationId = "op-001",
            evidence = listOf(evidence("ev-1")),
            relationships = listOf(
                EvidenceRelationship(
                    id = "rel-1",
                    fromEvidenceId = "ev-1",
                    toEvidenceId = "missing",
                    type = EvidenceRelationType.SUPPORTS,
                    status = EvidenceLinkStatus.CONFIRMED
                ),
                EvidenceRelationship(
                    id = "rel-2",
                    fromEvidenceId = "ev-1",
                    toEvidenceId = "ev-1",
                    type = EvidenceRelationType.SAME_ENTITY,
                    status = EvidenceLinkStatus.PLAYER_HYPOTHESIS
                )
            )
        )

        val codes = EvidenceGraphValidator.validate(graph).map { it.code }

        assertTrue(codes.contains("missing_relationship_target"))
        assertTrue(codes.contains("self_relationship"))
    }

    @Test
    fun claimCannotUseSameEvidenceAsSupportAndContradiction() {
        val graph = EvidenceGraph(
            operationId = "op-001",
            evidence = listOf(evidence("ev-1")),
            claims = listOf(
                EvidenceClaim(
                    id = "claim-1",
                    statement = "A test claim.",
                    supportingEvidenceIds = setOf("ev-1"),
                    contradictingEvidenceIds = setOf("ev-1")
                )
            )
        )

        val issues = EvidenceGraphValidator.validate(graph)

        assertTrue(issues.any { it.code == "claim_evidence_overlap" })
    }

    @Test
    fun annotationIsPlayerDataAndMustReferenceExistingEvidence() {
        val graph = EvidenceGraph(
            operationId = "op-001",
            annotations = listOf(
                EvidenceAnnotation(
                    id = "note-1",
                    evidenceId = "missing",
                    text = "Player observation"
                )
            )
        )

        val issues = EvidenceGraphValidator.validate(graph)

        assertTrue(issues.any { it.code == "missing_annotation_evidence" })
    }

    @Test
    fun supportedClaimRequiresAtLeastOneSupportingSource() {
        val graph = EvidenceGraph(
            operationId = "op-001",
            claims = listOf(
                EvidenceClaim(
                    id = "claim-1",
                    statement = "An unsupported claim.",
                    status = ClaimStatus.SUPPORTED
                )
            )
        )

        val issues = EvidenceGraphValidator.validate(graph)

        assertTrue(issues.any { it.code == "supported_claim_without_evidence" })
        assertEquals(1, issues.count { it.code == "supported_claim_without_evidence" })
    }
}
