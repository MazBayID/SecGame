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
import com.mazbayid.aegisnet.domain.evidence.EvidenceLinkStatus
import com.mazbayid.aegisnet.domain.evidence.EvidenceGraphSaveResult
import com.mazbayid.aegisnet.domain.mission.FirstContactMissionCatalog
import com.mazbayid.aegisnet.ui.components.AegisPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DossierScreen(
    onOpenAnalysis: () -> Unit,
    onOpenDatabase: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) {
        RoomEvidenceGraphRepository(EvidenceDatabaseProvider.get(context))
    }
    var graph by remember { mutableStateOf<EvidenceGraph?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(repository) {
        try {
            graph = withContext(Dispatchers.IO) {
                var current = repository.load(FirstContactMissionCatalog.OPERATION_ID)
                if (current.evidence.isEmpty()) {
                    when (val result = repository.save(FirstContactMissionSeed.evidenceGraph())) {
                        EvidenceGraphSaveResult.Saved -> Unit
                        is EvidenceGraphSaveResult.Rejected ->
                            throw IllegalStateException(result.issues.joinToString { it.message })
                        is EvidenceGraphSaveResult.Failed ->
                            throw IllegalStateException(result.reason)
                    }
                    current = repository.load(FirstContactMissionCatalog.OPERATION_ID)
                }
                current
            }
        } catch (failure: Exception) {
            error = failure.message ?: "Dossier records could not be loaded."
        } finally {
            loading = false
        }
    }

    val active = graph
    val discovered = active?.evidence?.filter { it.isDiscovered }.orEmpty()
    val annotations = active?.annotations.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AegisPanel(title = "PERSONNEL DOSSIER / RECRUIT-014") {
            Text("CAMPAIGN 01 · FIRST CONTACT", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
            Spacer(Modifier.height(6.dp))
            Text("SUBJECT STATUS: RECRUITMENT EVALUATION", fontSize = 11.sp)
            Text("Clearance is provisional. This dossier distinguishes preserved records from analyst interpretation.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
        AegisPanel(title = "EVIDENCE SUMMARY") {
            when {
                loading -> Text("Retrieving operation dossier...", fontSize = 10.sp)
                error != null -> Text(error ?: "Load failure", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                active == null -> Text("No dossier graph is available.", fontSize = 10.sp)
                else -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryMetric("DISCOVERED", discovered.size.toString())
                        SummaryMetric("ANNOTATIONS", annotations.size.toString())
                        SummaryMetric("CLAIMS", active.claims.size.toString())
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("SOURCE REGISTER", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
                    if (discovered.isEmpty()) {
                        Text("No records discovered yet. Inspect evidence through Database.", fontSize = 10.sp)
                    } else discovered.forEach { record ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                            Text(record.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${record.sourceLabel} · ${record.type} · ${record.integrity}",
                                fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("REFERENCE: ${record.sourceReference ?: "NOT RECORDED"} · ACCESS: ${record.access}",
                                fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            annotations.filter { it.evidenceId == record.id }.forEach { note ->
                                Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text("ANALYST NOTE: ${note.text}", Modifier.padding(7.dp), fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        AegisPanel(title = "ASSESSMENT REGISTER") {
            val claims = active?.claims.orEmpty()
            if (claims.isEmpty()) {
                Text("No analyst claims have been entered. Claims can be authored in Evidence Archive.", fontSize = 10.sp)
            } else claims.forEach { claim ->
                Column(Modifier.padding(vertical = 5.dp)) {
                    Text("${claim.status} · ${claim.confidence}", color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
                    Text(claim.statement, fontSize = 10.sp)
                    Text("SUPPORT: ${claim.supportingEvidenceIds.size} · CONTRADICTION: ${claim.contradictingEvidenceIds.size}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            val links = active?.relationships.orEmpty()
            val confirmed = links.count { it.status == EvidenceLinkStatus.CONFIRMED }
            val hypotheses = links.count { it.status == EvidenceLinkStatus.PLAYER_HYPOTHESIS }
            Text("LINKS: ${links.size} · CONFIRMED: $confirmed · HYPOTHESES: $hypotheses",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
            Text("A hypothesis is not treated as a confirmed finding.", fontSize = 9.sp)
        }
        Button(onClick = onOpenDatabase, modifier = Modifier.fillMaxWidth()) { Text("OPEN EVIDENCE ARCHIVE") }
        Button(onClick = onOpenAnalysis, modifier = Modifier.fillMaxWidth()) { Text("OPEN ANALYSIS WORKSTATION") }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("RETURN TO APP HUB") }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 7.sp)
    }
}
