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
