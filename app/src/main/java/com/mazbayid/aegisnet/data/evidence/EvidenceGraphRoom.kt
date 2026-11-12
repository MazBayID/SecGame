package com.mazbayid.aegisnet.data.evidence

import android.content.Context
import androidx.room.*
import androidx.room.withTransaction
import com.mazbayid.aegisnet.domain.evidence.*

@Entity(
    tableName = "evidence_records",
    primaryKeys = ["operationId", "id"],
    indices = [Index(value = ["operationId", "sourceId"]), Index(value = ["operationId", "eventAtEpochMillis"])]
)
data class EvidenceRecordEntity(
    val operationId: String, val id: String, val title: String, val description: String,
    val type: String, val sourceId: String, val sourceLabel: String, val sourceReference: String?,
    val createdAtEpochMillis: Long?, val eventAtEpochMillis: Long?, val integrity: String,
    val access: String, val origin: String
)

@Entity(
    tableName = "evidence_discovery",
    primaryKeys = ["operationId", "evidenceId"],
    foreignKeys = [ForeignKey(
        entity = EvidenceRecordEntity::class, parentColumns = ["operationId", "id"],
        childColumns = ["operationId", "evidenceId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["operationId", "evidenceId"])]
)
data class EvidenceDiscoveryEntity(
    val operationId: String,
    val evidenceId: String,
    val isDiscovered: Boolean
)

@Entity(
    tableName = "evidence_annotations",
    primaryKeys = ["operationId", "id"],
    foreignKeys = [ForeignKey(
        entity = EvidenceRecordEntity::class, parentColumns = ["operationId", "id"],
        childColumns = ["operationId", "evidenceId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["operationId"]), Index(value = ["operationId", "evidenceId"])]
)
data class EvidenceAnnotationEntity(
    val operationId: String, val id: String, val evidenceId: String,
    val text: String, val createdAtEpochMillis: Long?
)

@Entity(
    tableName = "evidence_relationships",
    primaryKeys = ["operationId", "id"],
    foreignKeys = [
        ForeignKey(
            entity = EvidenceRecordEntity::class, parentColumns = ["operationId", "id"],
            childColumns = ["operationId", "fromEvidenceId"], onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EvidenceRecordEntity::class, parentColumns = ["operationId", "id"],
            childColumns = ["operationId", "toEvidenceId"], onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["operationId"]), Index(value = ["operationId", "fromEvidenceId"]),
        Index(value = ["operationId", "toEvidenceId"])
    ]
)
data class EvidenceRelationshipEntity(
    val operationId: String, val id: String, val fromEvidenceId: String, val toEvidenceId: String,
    val type: String, val status: String, val confidence: String, val note: String?
)

@Entity(
    tableName = "evidence_claims",
    primaryKeys = ["operationId", "id"],
    indices = [Index(value = ["operationId"])]
)
data class EvidenceClaimEntity(
    val operationId: String, val id: String, val statement: String,
    val confidence: String, val status: String
)

@Entity(
    tableName = "evidence_claim_links",
    primaryKeys = ["operationId", "claimId", "evidenceId", "role"],
    foreignKeys = [
        ForeignKey(
            entity = EvidenceClaimEntity::class, parentColumns = ["operationId", "id"],
            childColumns = ["operationId", "claimId"], onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EvidenceRecordEntity::class, parentColumns = ["operationId", "id"],
            childColumns = ["operationId", "evidenceId"], onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["operationId"]), Index(value = ["operationId", "claimId"]),
        Index(value = ["operationId", "evidenceId"])
    ]
)
data class EvidenceClaimLinkEntity(
    val operationId: String, val claimId: String, val evidenceId: String, val role: String
)

object EvidenceGraphEntityMapper {
    fun EvidenceRecord.toEntity() = EvidenceRecordEntity(
        operationId, id, title, description, type.name, sourceId, sourceLabel, sourceReference,
        createdAtEpochMillis, eventAtEpochMillis, integrity.name, access.name, origin.name
    )

    fun EvidenceRecord.toDiscoveryEntity() =
        EvidenceDiscoveryEntity(operationId, id, isDiscovered)

    fun EvidenceRecordEntity.toDomain(isDiscovered: Boolean = false) = EvidenceRecord(
        id, title, description, EvidenceType.valueOf(type), sourceId, sourceLabel, sourceReference,
        createdAtEpochMillis, eventAtEpochMillis, EvidenceIntegrity.valueOf(integrity),
        EvidenceAccess.valueOf(access), EvidenceOrigin.valueOf(origin), operationId, isDiscovered
    )

    fun EvidenceAnnotation.toEntity(operationId: String) =
        EvidenceAnnotationEntity(operationId, id, evidenceId, text, createdAtEpochMillis)

    fun EvidenceAnnotationEntity.toDomain() =
        EvidenceAnnotation(id, evidenceId, text, createdAtEpochMillis)

    fun EvidenceRelationship.toEntity(operationId: String) =
        EvidenceRelationshipEntity(
            operationId, id, fromEvidenceId, toEvidenceId, type.name, status.name, confidence.name, note
        )

    fun EvidenceRelationshipEntity.toDomain() = EvidenceRelationship(
        id, fromEvidenceId, toEvidenceId, EvidenceRelationType.valueOf(type),
        EvidenceLinkStatus.valueOf(status), EvidenceConfidence.valueOf(confidence), note
    )

    fun EvidenceClaim.toEntity(operationId: String) =
        EvidenceClaimEntity(operationId, id, statement, confidence.name, status.name)

    fun EvidenceClaimEntity.toDomain(links: List<EvidenceClaimLinkEntity>) = EvidenceClaim(
        id = id,
        statement = statement,
        supportingEvidenceIds = links.filter { it.role == "SUPPORTS" }.map { it.evidenceId }.toSet(),
        contradictingEvidenceIds = links.filter { it.role == "CONTRADICTS" }.map { it.evidenceId }.toSet(),
        confidence = EvidenceConfidence.valueOf(confidence),
        status = ClaimStatus.valueOf(status)
    )

    fun EvidenceClaim.toLinkEntities(operationId: String) =
        (supportingEvidenceIds.map { it to "SUPPORTS" } +
            contradictingEvidenceIds.map { it to "CONTRADICTS" })
            .map { (evidenceId, role) -> EvidenceClaimLinkEntity(operationId, id, evidenceId, role) }
}

@Dao
interface EvidenceGraphDao {
    @Query("SELECT * FROM evidence_records WHERE operationId = :operationId ORDER BY eventAtEpochMillis, id")
    suspend fun getEvidence(operationId: String): List<EvidenceRecordEntity>
    @Query("SELECT * FROM evidence_discovery WHERE operationId = :operationId")
    suspend fun getDiscovery(operationId: String): List<EvidenceDiscoveryEntity>
    @Query("SELECT * FROM evidence_annotations WHERE operationId = :operationId ORDER BY createdAtEpochMillis, id")
    suspend fun getAnnotations(operationId: String): List<EvidenceAnnotationEntity>
    @Query("SELECT * FROM evidence_relationships WHERE operationId = :operationId ORDER BY id")
    suspend fun getRelationships(operationId: String): List<EvidenceRelationshipEntity>
    @Query("SELECT * FROM evidence_claims WHERE operationId = :operationId ORDER BY id")
    suspend fun getClaims(operationId: String): List<EvidenceClaimEntity>
    @Query("SELECT * FROM evidence_claim_links WHERE operationId = :operationId ORDER BY claimId, role, evidenceId")
    suspend fun getClaimLinks(operationId: String): List<EvidenceClaimLinkEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvidence(records: List<EvidenceRecordEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscovery(records: List<EvidenceDiscoveryEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotations(records: List<EvidenceAnnotationEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationships(records: List<EvidenceRelationshipEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaims(records: List<EvidenceClaimEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaimLinks(records: List<EvidenceClaimLinkEntity>)

    @Query("DELETE FROM evidence_discovery WHERE operationId = :operationId")
    suspend fun deleteDiscovery(operationId: String)
    @Query("DELETE FROM evidence_annotations WHERE operationId = :operationId")
    suspend fun deleteAnnotations(operationId: String)
    @Query("DELETE FROM evidence_relationships WHERE operationId = :operationId")
    suspend fun deleteRelationships(operationId: String)
    @Query("DELETE FROM evidence_claims WHERE operationId = :operationId")
    suspend fun deleteClaims(operationId: String)
}

@Database(
    entities = [
        EvidenceRecordEntity::class, EvidenceDiscoveryEntity::class, EvidenceAnnotationEntity::class,
        EvidenceRelationshipEntity::class, EvidenceClaimEntity::class,
        EvidenceClaimLinkEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class EvidenceDatabase : RoomDatabase() {
    abstract fun evidenceGraphDao(): EvidenceGraphDao
}

object EvidenceDatabaseProvider {
    @Volatile private var instance: EvidenceDatabase? = null
    fun get(context: Context): EvidenceDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
            context.applicationContext, EvidenceDatabase::class.java, "aegis_evidence.db"
        ).build().also { instance = it }
    }
}

class RoomEvidenceGraphRepository(private val database: EvidenceDatabase) : EvidenceGraphRepository {
    private val dao = database.evidenceGraphDao()

    override suspend fun load(operationId: String): EvidenceGraph {
        require(operationId.isNotBlank())
        val discovery = dao.getDiscovery(operationId).associate { it.evidenceId to it.isDiscovered }
        val evidence = dao.getEvidence(operationId).map { record ->
            EvidenceGraphEntityMapper.run { record.toDomain(discovery[record.id] ?: false) }
        }
        val annotations = dao.getAnnotations(operationId).map { EvidenceGraphEntityMapper.run { it.toDomain() } }
        val relationships = dao.getRelationships(operationId).map { EvidenceGraphEntityMapper.run { it.toDomain() } }
        val links = dao.getClaimLinks(operationId)
        val claims = dao.getClaims(operationId).map { claim ->
            EvidenceGraphEntityMapper.run { claim.toDomain(links.filter { it.claimId == claim.id }) }
        }
        return EvidenceGraph(operationId, evidence, annotations, relationships, claims)
    }

    override suspend fun save(graph: EvidenceGraph): EvidenceGraphSaveResult {
        val issues = EvidenceGraphValidator.validate(graph)
        if (issues.isNotEmpty()) return EvidenceGraphSaveResult.Rejected(issues)
        return try {
            database.withTransaction {
                val stored = dao.getEvidence(graph.operationId).associateBy { it.id }
                val incomingIds = graph.evidence.map { it.id }.toSet()
                val omittedIds = stored.keys - incomingIds
                if (omittedIds.isNotEmpty()) {
                    return@withTransaction EvidenceGraphSaveResult.Failed(
                        "Graph snapshot omitted existing source evidence: ${omittedIds.sorted().joinToString()}."
                    )
                }
                val conflicts = graph.evidence.filter { record ->
                    stored[record.id]?.let {
                        EvidenceGraphEntityMapper.run { it.toDomain(isDiscovered = false) }
                    }?.let { existing -> record.copy(isDiscovered = false) != existing } ?: false
                }.map { it.id }
                if (conflicts.isNotEmpty()) {
                    return@withTransaction EvidenceGraphSaveResult.Failed(
                        "Source evidence metadata is immutable; conflicting IDs: ${conflicts.sorted().joinToString()}."
                    )
                }

                dao.insertEvidence(graph.evidence.map { EvidenceGraphEntityMapper.run { it.toEntity() } })
                dao.deleteDiscovery(graph.operationId)
                dao.insertDiscovery(graph.evidence.map { EvidenceGraphEntityMapper.run { it.toDiscoveryEntity() } })
                dao.deleteAnnotations(graph.operationId)
                dao.deleteRelationships(graph.operationId)
                dao.deleteClaims(graph.operationId)
                dao.insertAnnotations(graph.annotations.map {
                    EvidenceGraphEntityMapper.run { it.toEntity(graph.operationId) }
                })
                dao.insertRelationships(graph.relationships.map {
                    EvidenceGraphEntityMapper.run { it.toEntity(graph.operationId) }
                })
                dao.insertClaims(graph.claims.map {
                    EvidenceGraphEntityMapper.run { it.toEntity(graph.operationId) }
                })
                dao.insertClaimLinks(graph.claims.flatMap {
                    EvidenceGraphEntityMapper.run { it.toLinkEntities(graph.operationId) }
                })
                EvidenceGraphSaveResult.Saved
            }
        } catch (error: Exception) {
            EvidenceGraphSaveResult.Failed(error.message ?: "Evidence graph transaction failed.")
        }
    }
}
