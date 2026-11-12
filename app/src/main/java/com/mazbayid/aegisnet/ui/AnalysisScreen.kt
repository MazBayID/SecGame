package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.data.evidence.EvidenceDatabaseProvider
import com.mazbayid.aegisnet.data.evidence.RoomEvidenceGraphRepository
import com.mazbayid.aegisnet.data.mission.FirstContactMissionSeed
import com.mazbayid.aegisnet.domain.evidence.EvidenceGraph
import com.mazbayid.aegisnet.domain.evidence.EvidenceGraphQuery
import com.mazbayid.aegisnet.domain.evidence.EvidenceQuery
import com.mazbayid.aegisnet.domain.mission.FirstContactMissionCatalog
import com.mazbayid.aegisnet.ui.components.AegisPanel
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AnalysisScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) {
        RoomEvidenceGraphRepository(EvidenceDatabaseProvider.get(context))
    }
    var graph by remember { mutableStateOf<EvidenceGraph?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var leftEvidenceId by remember { mutableStateOf<String?>(null) }
    var rightEvidenceId by remember { mutableStateOf<String?>(null) }
    var selectionSlot by remember { mutableStateOf("LEFT") }

    LaunchedEffect(repository) {
        try {
            val loaded = withContext(Dispatchers.IO) {
                var current = repository.load(FirstContactMissionCatalog.OPERATION_ID)
                if (current.evidence.isEmpty()) {
                    when (val seeded = repository.save(FirstContactMissionSeed.evidenceGraph())) {
                        com.mazbayid.aegisnet.domain.evidence.EvidenceGraphSaveResult.Saved -> Unit
                        is com.mazbayid.aegisnet.domain.evidence.EvidenceGraphSaveResult.Rejected ->
                            error(seeded.issues.joinToString { it.message })
                        is com.mazbayid.aegisnet.domain.evidence.EvidenceGraphSaveResult.Failed ->
                            error(seeded.reason)
                    }
                    current = repository.load(FirstContactMissionCatalog.OPERATION_ID)
                }
                current
            }
            graph = loaded
            val discoveredRecords = loaded.evidence.filter { it.isDiscovered }
            leftEvidenceId = discoveredRecords.getOrNull(0)?.id
            rightEvidenceId = discoveredRecords.getOrNull(1)?.id
        } catch (failure: Exception) {
            error = failure.message ?: "Analysis data could not be loaded."
        } finally {
            loading = false
        }
    }

    val discovered = graph?.let {
        EvidenceGraphQuery.search(it, EvidenceQuery(discoveredOnly = true))
    }.orEmpty()
    val left = discovered.firstOrNull { it.id == leftEvidenceId }
    val right = discovered.firstOrNull { it.id == rightEvidenceId }

    AegisPanel(title = "ANALYSIS / INTELLIGENCE REVIEW") {
        Text("FIRST CONTACT / EVIDENCE-LED ANALYSIS", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
        Spacer(Modifier.height(8.dp))
        when {
            loading -> Text("Loading operation records...", fontSize = 11.sp)
            error != null -> Text(error ?: "Analysis error", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
            graph == null -> Text("No operation graph is available.", fontSize = 10.sp)
            else -> {
                val activeGraph = graph!!
                val confirmed = activeGraph.relationships.count {
                    it.status == com.mazbayid.aegisnet.domain.evidence.EvidenceLinkStatus.CONFIRMED
                }
                val hypotheses = activeGraph.relationships.count {
                    it.status == com.mazbayid.aegisnet.domain.evidence.EvidenceLinkStatus.PLAYER_HYPOTHESIS
                }
                val unresolved = activeGraph.relationships.count {
                    it.status == com.mazbayid.aegisnet.domain.evidence.EvidenceLinkStatus.UNRESOLVED
                }

                Text("DISCOVERED RECORDS  ${discovered.size}", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                Text(
                    "CLAIMS  ${activeGraph.claims.size}   /   CONFIRMED LINKS  $confirmed   /   HYPOTHESES  $hypotheses   /   UNRESOLVED  $unresolved",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 8.sp
                )

                Spacer(Modifier.height(14.dp))
                Text("SOURCE COMPARISON", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                Text(
                    "Choose two discovered records. The comparison displays stored metadata only; it does not infer a cause.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { selectionSlot = "LEFT" }, modifier = Modifier.weight(1f)) {
                        Text(if (selectionSlot == "LEFT") "SELECTING A" else "SELECT A")
                    }
                    Button(onClick = { selectionSlot = "RIGHT" }, modifier = Modifier.weight(1f)) {
                        Text(if (selectionSlot == "RIGHT") "SELECTING B" else "SELECT B")
                    }
                }
                discovered.forEach { record ->
                    Button(
                        onClick = {
                            if (selectionSlot == "LEFT") leftEvidenceId = record.id
                            else rightEvidenceId = record.id
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val marker = when (record.id) {
                            leftEvidenceId -> "A · "
                            rightEvidenceId -> "B · "
                            else -> ""
                        }
                        Text(marker + record.title)
                    }
                }
                if (left != null && right != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ComparisonColumn("SOURCE A", left.title, left.sourceLabel, left.sourceReference, left.type.name, left.integrity.name, left.eventAtEpochMillis)
                        ComparisonColumn("SOURCE B", right.title, right.sourceLabel, right.sourceReference, right.type.name, right.integrity.name, right.eventAtEpochMillis)
                    }
                    if (left.id == right.id) {
                        Text("Both slots reference the same record. Select a different record to compare two sources.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    }
                } else {
                    Text("Select two discovered records to begin comparison.", fontSize = 9.sp)
                }

                Spacer(Modifier.height(16.dp))
                Text("OPERATION TIMELINE", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                if (discovered.isEmpty()) {
                    Text("No discovered events are available yet. Inspect source records from Mission Control first.", fontSize = 10.sp)
                } else {
                    discovered.sortedBy { it.eventAtEpochMillis ?: it.createdAtEpochMillis ?: Long.MAX_VALUE }
                        .forEach { record ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                Text(
                                    record.eventAtEpochMillis?.let(::formatAnalysisTime) ?: "TIME NOT ESTABLISHED",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 8.sp
                                )
                                Text(record.title, fontSize = 10.sp)
                                Text(
                                    "${record.sourceLabel}  ·  ${record.type}  ·  ${record.integrity}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 8.sp
                                )
                            }
                        }
                }

                Spacer(Modifier.height(16.dp))
                Text("CLAIM REGISTER", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                if (activeGraph.claims.isEmpty()) {
                    Text("No player-authored claims are recorded. Create a claim in Database / Evidence Archive.", fontSize = 9.sp)
                } else {
                    activeGraph.claims.forEach { claim ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(Modifier.padding(9.dp)) {
                                Text(
                                    "${claim.status}  ·  ${claim.confidence}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 8.sp
                                )
                                Text(claim.statement, fontSize = 10.sp)
                                Text(
                                    "SUPPORTS ${claim.supportingEvidenceIds.size}  /  CONTRADICTS ${claim.contradictingEvidenceIds.size}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("RELATIONSHIP REGISTER", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                if (activeGraph.relationships.isEmpty()) {
                    Text("No links have been authored. Add a hypothesis in Database / Evidence Archive.", fontSize = 9.sp)
                } else {
                    activeGraph.relationships.forEach { link ->
                        Text(
                            "${link.fromEvidenceId}  — ${link.type} / ${link.status} —  ${link.toEvidenceId}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("RETURN TO APP HUB") }
    }
}

@Composable
private fun ComparisonColumn(
    label: String,
    title: String,
    source: String,
    reference: String?,
    type: String,
    integrity: String,
    eventAt: Long?
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(label, color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
        Text(title, fontSize = 10.sp)
        Text("SOURCE: $source", fontSize = 8.sp)
        Text("REF: ${reference ?: "NOT RECORDED"}", fontSize = 8.sp)
        Text("TYPE: $type", fontSize = 8.sp)
        Text("INTEGRITY: $integrity", fontSize = 8.sp)
        Text("EVENT: ${eventAt?.let(::formatAnalysisTime) ?: "UNKNOWN"}", fontSize = 8.sp)
    }
}

private fun formatAnalysisTime(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date(epochMillis))
