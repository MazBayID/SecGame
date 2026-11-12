package com.mazbayid.aegisnet.data.evidence

import com.mazbayid.aegisnet.domain.evidence.*
import org.junit.Assert.assertEquals
import org.junit.Test

class EvidenceGraphEntityMapperTest {
    @Test
    fun mapsEvidenceAndAllMetadataWithoutLoss() {
        val record = EvidenceRecord(
            id = "rx14_receipt_a", title = "A.E.G.I.S. receipt",
            description = "Receipt time recorded by A.E.G.I.S.",
            type = EvidenceType.DATABASE_RECORD, sourceId = "aegis_archive",
            sourceLabel = "A.E.G.I.S. Archive", sourceReference = "RX-14",
            createdAtEpochMillis = 1000L, eventAtEpochMillis = 2000L,
            integrity = EvidenceIntegrity.VERIFIED, access = EvidenceAccess.INTERNAL,
            origin = EvidenceOrigin.ORIGINAL, operationId = "first_contact_01", isDiscovered = true
        )
        assertEquals(
            record.copy(isDiscovered = false),
            EvidenceGraphEntityMapper.run { record.toEntity().toDomain(isDiscovered = false) }
        )
        assertEquals(
            record.isDiscovered,
            EvidenceGraphEntityMapper.run { record.toDiscoveryEntity().isDiscovered }
        )
    }

    @Test
    fun mapsClaimsAndTheirSupportingAndContradictingLinks() {
        val claim = EvidenceClaim(
            id = "claim_delta", statement = "Two sources record different receipt times.",
            supportingEvidenceIds = setOf("aegis_receipt", "archive_receipt"),
            contradictingEvidenceIds = setOf("other_record"),
            confidence = EvidenceConfidence.MEDIUM, status = ClaimStatus.PROPOSED
        )
        val links = EvidenceGraphEntityMapper.run { claim.toLinkEntities("first_contact_01") }
        val entity = EvidenceGraphEntityMapper.run { claim.toEntity("first_contact_01") }
        assertEquals(claim, EvidenceGraphEntityMapper.run { entity.toDomain(links) })
        assertEquals(3, links.size)
    }

    @Test
    fun preservesNullTimestampsAndOptionalSourceReference() {
        val record = EvidenceRecord(
            id = "unknown_time", title = "Unresolved record", description = "",
            type = EvidenceType.OBSERVATION, sourceId = "unknown_source",
            sourceLabel = "Unknown source", operationId = "first_contact_01"
        )
        val restored = EvidenceGraphEntityMapper.run { record.toEntity().toDomain() }
        assertEquals(null, restored.createdAtEpochMillis)
        assertEquals(null, restored.eventAtEpochMillis)
        assertEquals(null, restored.sourceReference)
        assertEquals(EvidenceIntegrity.UNKNOWN, restored.integrity)
    }
}
