package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.data.evidence.EvidenceDatabaseProvider
import com.mazbayid.aegisnet.data.evidence.RoomEvidenceGraphRepository
import com.mazbayid.aegisnet.data.mission.FirstContactMissionSeed
import com.mazbayid.aegisnet.domain.evidence.EvidenceAnnotation
import com.mazbayid.aegisnet.domain.evidence.ClaimStatus
import com.mazbayid.aegisnet.domain.evidence.EvidenceClaim
import com.mazbayid.aegisnet.domain.evidence.EvidenceConfidence
import com.mazbayid.aegisnet.domain.evidence.EvidenceLinkStatus
import com.mazbayid.aegisnet.domain.evidence.EvidenceRelationType
import com.mazbayid.aegisnet.domain.evidence.EvidenceRelationship
import com.mazbayid.aegisnet.domain.evidence.EvidenceGraph
import com.mazbayid.aegisnet.domain.evidence.EvidenceGraphEditor
import com.mazbayid.aegisnet.domain.evidence.EvidenceGraphMutation
import com.mazbayid.aegisnet.domain.evidence.EvidenceGraphSaveResult
import com.mazbayid.aegisnet.domain.mission.FirstContactMissionCatalog
import com.mazbayid.aegisnet.ui.components.AegisPanel
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EvidenceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) {
        RoomEvidenceGraphRepository(EvidenceDatabaseProvider.get(context))
    }
    val scope = rememberCoroutineScope()
    var graph by remember { mutableStateOf<EvidenceGraph?>(null) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var relationshipNote by remember { mutableStateOf("") }
    var claimStatement by remember { mutableStateOf("") }
    var targetEvidenceId by remember { mutableStateOf<String?>(null) }
    var relationType by remember { mutableStateOf(EvidenceRelationType.SUPPORTS) }
    var claimStatus by remember { mutableStateOf(ClaimStatus.PROPOSED) }
    var status by remember { mutableStateOf("Loading operation evidence...") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(repository) {
        try {
            graph = withContext(Dispatchers.IO) {
                var loaded = repository.load(FirstContactMissionCatalog.OPERATION_ID)
                if (loaded.evidence.isEmpty()) {
                    when (val seeded = repository.save(FirstContactMissionSeed.evidenceGraph())) {
                        EvidenceGraphSaveResult.Saved -> Unit
                        is EvidenceGraphSaveResult.Rejected ->
                            error(seeded.issues.joinToString { it.message })
                        is EvidenceGraphSaveResult.Failed -> error(seeded.reason)
                    }
                    loaded = repository.load(FirstContactMissionCatalog.OPERATION_ID)
                }
                loaded
            }
            selectedId = graph?.evidence?.firstOrNull { it.isDiscovered }?.id
            status = "Evidence repository ready · offline"
        } catch (failure: Exception) {
            status = failure.message ?: "Evidence could not be loaded."
        } finally {
            loading = false
        }
    }

    AegisPanel(title = "DATABASE / EVIDENCE ARCHIVE") {
        Text(
            "OPERATION: FIRST CONTACT  ·  SOURCE RECORDS ARE READ-ONLY",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
        Spacer(Modifier.height(8.dp))

        when {
            loading -> Text("Loading evidence records...", fontSize = 11.sp)
            graph == null -> Text(
                status,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp
            )
            else -> {
                val discovered = graph!!.evidence.filter { it.isDiscovered }
                Text(
                    "DISCOVERED RECORDS  /  ${discovered.size}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp
                )
                if (discovered.isEmpty()) {
                    Text(
                        "No evidence has been discovered in this operation. Return to Mission Control and inspect the available source material.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(discovered, key = { it.id }) { record ->
                            val selected = record.id == selectedId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedId = record.id },
                                color = if (selected) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Column(Modifier.padding(9.dp)) {
                                    Text(
                                        record.title,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        "${record.type}  ·  ${record.integrity}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                val selected = graph!!.evidence.firstOrNull {
                    it.id == selectedId && it.isDiscovered
                }
                if (selected != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        selected.title,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Text(selected.description, fontSize = 11.sp)
                    Spacer(Modifier.height(6.dp))
                    MetadataLine("SOURCE", selected.sourceLabel)
                    selected.sourceReference?.let { MetadataLine("REFERENCE", it) }
                    MetadataLine("TYPE / INTEGRITY", "${selected.type} / ${selected.integrity}")
                    MetadataLine("ACCESS / ORIGIN", "${selected.access} / ${selected.origin}")
                    selected.eventAtEpochMillis?.let {
                        MetadataLine("EVENT TIME", formatEvidenceTime(it))
                    }
                    selected.createdAtEpochMillis?.let {
                        MetadataLine("RECEIVED / CREATED", formatEvidenceTime(it))
                    }

                    val annotations = graph!!.annotations.filter { it.evidenceId == selected.id }
                    if (annotations.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("OPERATIVE NOTES", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                        annotations.forEach { annotation ->
                            Text("• ${annotation.text}", fontSize = 10.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        label = { Text("Add a private investigative note") }
                    )
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val current = graph ?: return@Button
                            val cleanNote = note.trim()
                            if (cleanNote.isNotEmpty()) {
                                scope.launch {
                                    val mutation = EvidenceGraphEditor.addAnnotation(
                                        current,
                                        EvidenceAnnotation(
                                            id = "note_${System.currentTimeMillis()}",
                                            evidenceId = selected.id,
                                            text = cleanNote
                                        )
                                    )
                                    when (mutation) {
                                        is EvidenceGraphMutation.Rejected -> {
                                            status = mutation.issue.message
                                        }
                                        is EvidenceGraphMutation.Accepted -> {
                                            if (!mutation.changed) {
                                                status = "Note already exists."
                                            } else {
                                                when (val saved = withContext(Dispatchers.IO) {
                                                    repository.save(mutation.graph)
                                                }) {
                                                    EvidenceGraphSaveResult.Saved -> {
                                                        graph = mutation.graph
                                                        note = ""
                                                        status = "Private note saved. Original evidence was not changed."
                                                    }
                                                    is EvidenceGraphSaveResult.Rejected ->
                                                        status = saved.issues.joinToString { it.message }
                                                    is EvidenceGraphSaveResult.Failed ->
                                                        status = saved.reason
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        enabled = note.isNotBlank()
                    ) { Text("SAVE PRIVATE NOTE") }
                }

                // Evidence links begin as player hypotheses. Confirmation is a separate,
                // explicit action and never rewrites the source records.
                Spacer(Modifier.height(16.dp))
                Text("EVIDENCE RELATIONSHIPS", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                Text(
                    "Select another discovered record, describe the relationship, then add it as an unconfirmed hypothesis.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp
                )
                val relationshipTargets = discovered.filter { it.id != selected.id }
                relationshipTargets.forEach { target ->
                    Button(
                        onClick = { targetEvidenceId = target.id },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text((if (targetEvidenceId == target.id) "TARGET SELECTED · " else "LINK TO · ") + target.title)
                    }
                }
                Button(
                    onClick = {
                        relationType = EvidenceRelationType.entries[
                            (EvidenceRelationType.entries.indexOf(relationType) + 1) % EvidenceRelationType.entries.size
                        ]
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("RELATION TYPE · $relationType / CHANGE") }
                OutlinedTextField(
                    value = relationshipNote,
                    onValueChange = { relationshipNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    maxLines = 3,
                    label = { Text("Why might these records be related?") }
                )
                Button(
                    enabled = targetEvidenceId != null && targetEvidenceId != selected.id,
                    onClick = {
                        val current = graph ?: return@Button
                        val target = targetEvidenceId ?: return@Button
                        val mutation = EvidenceGraphEditor.addRelationship(
                            current,
                            EvidenceRelationship(
                                id = "hyp_" + System.currentTimeMillis(),
                                fromEvidenceId = selected.id,
                                toEvidenceId = target,
                                type = relationType,
                                status = EvidenceLinkStatus.PLAYER_HYPOTHESIS,
                                confidence = EvidenceConfidence.MEDIUM,
                                note = relationshipNote.trim().ifBlank { null }
                            )
                        )
                        when (mutation) {
                            is EvidenceGraphMutation.Rejected -> status = mutation.issue.message
                            is EvidenceGraphMutation.Accepted -> scope.launch {
                                when (val saved = withContext(Dispatchers.IO) { repository.save(mutation.graph) }) {
                                    EvidenceGraphSaveResult.Saved -> {
                                        graph = mutation.graph
                                        targetEvidenceId = null
                                        relationshipNote = ""
                                        status = "Hypothesis saved. It remains unconfirmed until you confirm it."
                                    }
                                    is EvidenceGraphSaveResult.Rejected -> status = saved.issues.joinToString { it.message }
                                    is EvidenceGraphSaveResult.Failed -> status = saved.reason
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("ADD PLAYER HYPOTHESIS") }

                graph!!.relationships
                    .filter { it.fromEvidenceId == selected.id || it.toEvidenceId == selected.id }
                    .forEach { relationship ->
                        val otherId = if (relationship.fromEvidenceId == selected.id) relationship.toEvidenceId
                            else relationship.fromEvidenceId
                        val other = graph!!.evidence.firstOrNull { it.id == otherId }
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(
                                "${relationship.type} · ${relationship.status} · ${relationship.confidence}",
                                color = MaterialTheme.colorScheme.primary, fontSize = 9.sp
                            )
                            Text("LINKED RECORD: ${other?.title ?: otherId}", fontSize = 10.sp)
                            relationship.note?.let { Text(it, fontSize = 9.sp) }
                            if (relationship.status == EvidenceLinkStatus.PLAYER_HYPOTHESIS) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        val current = graph ?: return@Button
                                        when (val mutation = EvidenceGraphEditor.confirmHypothesis(current, relationship.id)) {
                                            is EvidenceGraphMutation.Rejected -> status = mutation.issue.message
                                            is EvidenceGraphMutation.Accepted -> scope.launch {
                                                when (val saved = withContext(Dispatchers.IO) { repository.save(mutation.graph) }) {
                                                    EvidenceGraphSaveResult.Saved -> {
                                                        graph = mutation.graph
                                                        status = "Relationship explicitly confirmed."
                                                    }
                                                    is EvidenceGraphSaveResult.Rejected -> status = saved.issues.joinToString { it.message }
                                                    is EvidenceGraphSaveResult.Failed -> status = saved.reason
                                                }
                                            }
                                        }
                                    }) { Text("CONFIRM LINK") }
                                    Button(onClick = {
                                        val current = graph ?: return@Button
                                        when (val mutation = EvidenceGraphEditor.removeHypothesis(current, relationship.id)) {
                                            is EvidenceGraphMutation.Rejected -> status = mutation.issue.message
                                            is EvidenceGraphMutation.Accepted -> scope.launch {
                                                when (val saved = withContext(Dispatchers.IO) { repository.save(mutation.graph) }) {
                                                    EvidenceGraphSaveResult.Saved -> {
                                                        graph = mutation.graph
                                                        status = "Unconfirmed hypothesis removed."
                                                    }
                                                    is EvidenceGraphSaveResult.Rejected -> status = saved.issues.joinToString { it.message }
                                                    is EvidenceGraphSaveResult.Failed -> status = saved.reason
                                                }
                                            }
                                        }
                                    }) { Text("REMOVE") }
                                }
                            }
                        }
                    }

                Spacer(Modifier.height(16.dp))
                Text("INVESTIGATIVE CLAIMS", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                Text(
                    "Claims are assessments, not facts. Attach the selected record as support or contradiction and retain an explicit confidence status.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp
                )
                OutlinedTextField(
                    value = claimStatement,
                    onValueChange = { claimStatement = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    label = { Text("Write a testable investigative claim") }
                )
                Button(
                    onClick = {
                        claimStatus = when (claimStatus) {
                            ClaimStatus.PROPOSED -> ClaimStatus.SUPPORTED
                            ClaimStatus.SUPPORTED -> ClaimStatus.DISPUTED
                            ClaimStatus.DISPUTED -> ClaimStatus.INSUFFICIENT
                            ClaimStatus.INSUFFICIENT -> ClaimStatus.PROPOSED
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("CLAIM STATUS · $claimStatus / CHANGE") }
                Button(
                    enabled = claimStatement.isNotBlank(),
                    onClick = {
                        val current = graph ?: return@Button
                        val claim = EvidenceClaim(
                            id = "claim_" + System.currentTimeMillis(),
                            statement = claimStatement.trim(),
                            supportingEvidenceIds = if (claimStatus == ClaimStatus.SUPPORTED) setOf(selected.id) else emptySet(),
                            contradictingEvidenceIds = if (claimStatus == ClaimStatus.DISPUTED) setOf(selected.id) else emptySet(),
                            confidence = when (claimStatus) {
                                ClaimStatus.SUPPORTED, ClaimStatus.DISPUTED -> EvidenceConfidence.MEDIUM
                                ClaimStatus.INSUFFICIENT -> EvidenceConfidence.LOW
                                ClaimStatus.PROPOSED -> EvidenceConfidence.UNASSESSED
                            },
                            status = claimStatus
                        )
                        when (val mutation = EvidenceGraphEditor.saveClaim(current, claim)) {
                            is EvidenceGraphMutation.Rejected -> status = mutation.issue.message
                            is EvidenceGraphMutation.Accepted -> scope.launch {
                                when (val saved = withContext(Dispatchers.IO) { repository.save(mutation.graph) }) {
                                    EvidenceGraphSaveResult.Saved -> {
                                        graph = mutation.graph
                                        claimStatement = ""
                                        status = "Investigative claim saved with selected evidence reference."
                                    }
                                    is EvidenceGraphSaveResult.Rejected -> status = saved.issues.joinToString { it.message }
                                    is EvidenceGraphSaveResult.Failed -> status = saved.reason
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("SAVE INVESTIGATIVE CLAIM") }

                graph!!.claims.forEach { claim ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("${claim.status} · ${claim.confidence}", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
                        Text(claim.statement, fontSize = 10.sp)
                        Text(
                            "SUPPORTS: ${claim.supportingEvidenceIds.joinToString().ifBlank { "NONE" }} · " +
                                "CONTRADICTS: ${claim.contradictingEvidenceIds.joinToString().ifBlank { "NONE" }}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            status,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
        Spacer(Modifier.height(10.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("RETURN TO APP HUB")
        }
    }
}

@Composable
private fun MetadataLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            "$label  ",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
        Text(value, fontSize = 9.sp)
    }
}

private fun formatEvidenceTime(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
        .format(Date(epochMillis))
