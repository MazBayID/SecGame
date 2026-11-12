package com.mazbayid.aegisnet.domain.evidence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceGraphEditorTest {

    private fun evidence(
        id: String,
        discovered: Boolean = false,
        eventTime: Long? = null,
        type: EvidenceType = EvidenceType.FILE
    ) = EvidenceRecord(
        id = id,
        title = "Record $id",
        description = "Test evidence",
        type = type,
        sourceId = "source-$id",
        sourceLabel = "Test source",
        eventAtEpochMillis = eventTime,
        operationId = "op-001",
        isDiscovered = discovered
    )

    @Test
    fun addingSameEvidenceTwiceIsIdempotent() {
        val first = EvidenceGraphEditor.addEvidence(EvidenceGraph("op-001"), evidence("ev-1"))
            as EvidenceGraphMutation.Accepted
        val second = EvidenceGraphEditor.addEvidence(first.graph, evidence("ev-1"))
            as EvidenceGraphMutation.Accepted

        assertTrue(first.changed)
        assertFalse(second.changed)
        assertEquals(1, second.graph.evidence.size)
    }

    @Test
    fun conflictingEvidenceIdIsRejectedWithoutChangingOriginalGraph() {
        val original = EvidenceGraph("op-001", evidence = listOf(evidence("ev-1")))
        val result = EvidenceGraphEditor.addEvidence(
            original,
            evidence("ev-1", discovered = true)
        )

        assertTrue(result is EvidenceGraphMutation.Rejected)
        assertEquals(false, original.evidence.single().isDiscovered)
    }

    @Test
    fun annotationDoesNotModifySourceEvidence() {
        val original = EvidenceGraph("op-001", evidence = listOf(evidence("ev-1")))
        val result = EvidenceGraphEditor.addAnnotation(
            original,
            EvidenceAnnotation("note-1", "ev-1", "Check timestamp")
        ) as EvidenceGraphMutation.Accepted

        assertEquals(0, original.annotations.size)
        assertEquals(1, result.graph.annotations.size)
        assertEquals(original.evidence.single(), result.graph.evidence.single())
    }

    @Test
    fun relationshipRequiresBothEvidenceEndpoints() {
        val result = EvidenceGraphEditor.addRelationship(
            EvidenceGraph("op-001", evidence = listOf(evidence("ev-1"))),
            EvidenceRelationship(
                id = "rel-1",
                fromEvidenceId = "ev-1",
                toEvidenceId = "missing",
                type = EvidenceRelationType.SUPPORTS,
                status = EvidenceLinkStatus.PLAYER_HYPOTHESIS
            )
        )

        assertTrue(result is EvidenceGraphMutation.Rejected)
    }

    @Test
    fun hypothesisRequiresExplicitConfirmation() {
        val graph = EvidenceGraph(
            "op-001",
            evidence = listOf(evidence("ev-1"), evidence("ev-2")),
            relationships = listOf(
                EvidenceRelationship(
                    id = "rel-1",
                    fromEvidenceId = "ev-1",
                    toEvidenceId = "ev-2",
                    type = EvidenceRelationType.SUPPORTS,
                    status = EvidenceLinkStatus.PLAYER_HYPOTHESIS
                )
            )
        )

        val result = EvidenceGraphEditor.confirmHypothesis(graph, "rel-1")
            as EvidenceGraphMutation.Accepted

        assertEquals(EvidenceLinkStatus.PLAYER_HYPOTHESIS, graph.relationships.single().status)
        assertEquals(EvidenceLinkStatus.CONFIRMED, result.graph.relationships.single().status)
        assertTrue(result.changed)
    }

    @Test
    fun confirmingUnresolvedRelationshipIsRejected() {
        val graph = EvidenceGraph(
            "op-001",
            evidence = listOf(evidence("ev-1"), evidence("ev-2")),
            relationships = listOf(
                EvidenceRelationship(
                    id = "rel-1",
                    fromEvidenceId = "ev-1",
                    toEvidenceId = "ev-2",
                    type = EvidenceRelationType.SUPPORTS,
                    status = EvidenceLinkStatus.UNRESOLVED
                )
            )
        )

        val result = EvidenceGraphEditor.confirmHypothesis(graph, "rel-1")

        assertTrue(result is EvidenceGraphMutation.Rejected)
        assertEquals(EvidenceLinkStatus.UNRESOLVED, graph.relationships.single().status)
    }

    @Test
    fun removingHypothesisPreservesOriginalGraphAndConfirmedLinks() {
        val graph = EvidenceGraph(
            "op-001",
            evidence = listOf(evidence("ev-1"), evidence("ev-2")),
            relationships = listOf(
                EvidenceRelationship(
                    id = "rel-hypothesis",
                    fromEvidenceId = "ev-1",
                    toEvidenceId = "ev-2",
                    type = EvidenceRelationType.SUPPORTS,
                    status = EvidenceLinkStatus.PLAYER_HYPOTHESIS
                )
            )
        )

        val result = EvidenceGraphEditor.removeHypothesis(graph, "rel-hypothesis")
            as EvidenceGraphMutation.Accepted

        assertEquals(1, graph.relationships.size)
        assertTrue(result.graph.relationships.isEmpty())
        assertTrue(result.changed)
    }

    @Test
    fun confirmedRelationshipCannotBeRemovedAsHypothesis() {
        val graph = EvidenceGraph(
            "op-001",
            evidence = listOf(evidence("ev-1"), evidence("ev-2")),
            relationships = listOf(
                EvidenceRelationship(
                    id = "rel-1",
                    fromEvidenceId = "ev-1",
                    toEvidenceId = "ev-2",
                    type = EvidenceRelationType.SUPPORTS,
                    status = EvidenceLinkStatus.CONFIRMED
                )
            )
        )

        val result = EvidenceGraphEditor.removeHypothesis(graph, "rel-1")

        assertTrue(result is EvidenceGraphMutation.Rejected)
        assertEquals(1, graph.relationships.size)
    }

    @Test
    fun searchFiltersAndOrdersEvidenceDeterministically() {
        val graph = EvidenceGraph(
            operationId = "op-001",
            evidence = listOf(
                evidence("ev-late", discovered = true, eventTime = 300L),
                evidence("ev-hidden", discovered = false, eventTime = 100L),
                evidence("ev-early", discovered = true, eventTime = 200L, type = EvidenceType.MESSAGE)
            )
        )

        val results = EvidenceGraphQuery.search(
            graph,
            EvidenceQuery(discoveredOnly = true)
        )

        assertEquals(listOf("ev-early", "ev-late"), results.map { it.id })
    }
}
