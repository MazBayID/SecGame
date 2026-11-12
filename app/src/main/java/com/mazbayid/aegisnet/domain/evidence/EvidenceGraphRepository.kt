package com.mazbayid.aegisnet.domain.evidence

sealed interface EvidenceGraphSaveResult {
    data object Saved : EvidenceGraphSaveResult
    data class Rejected(val issues: List<EvidenceGraphIssue>) : EvidenceGraphSaveResult
    data class Failed(val reason: String) : EvidenceGraphSaveResult
}

interface EvidenceGraphRepository {
    suspend fun load(operationId: String): EvidenceGraph
    suspend fun save(graph: EvidenceGraph): EvidenceGraphSaveResult
}
