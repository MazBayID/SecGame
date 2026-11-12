package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.data.campaign.SharedPreferencesCampaignMissionProgressRepository
import com.mazbayid.aegisnet.data.evidence.EvidenceDatabaseProvider
import com.mazbayid.aegisnet.data.evidence.RoomEvidenceGraphRepository
import com.mazbayid.aegisnet.data.execution.SharedPreferencesRunReceiptRepository
import com.mazbayid.aegisnet.data.mission.FirstContactMissionSeed
import com.mazbayid.aegisnet.data.vfs.SharedPreferencesVfsRepository
import com.mazbayid.aegisnet.domain.campaign.CampaignMissionMutation
import com.mazbayid.aegisnet.domain.campaign.CampaignMissionProgression
import com.mazbayid.aegisnet.domain.editor.EditorDocumentService
import com.mazbayid.aegisnet.domain.evidence.EvidenceGraph
import com.mazbayid.aegisnet.domain.evidence.EvidenceGraphSaveResult
import com.mazbayid.aegisnet.domain.mission.FirstContactMissionCatalog
import com.mazbayid.aegisnet.domain.mission.MissionEvaluator
import com.mazbayid.aegisnet.domain.mission.MissionSubmission
import com.mazbayid.aegisnet.domain.vfs.VfsResult
import com.mazbayid.aegisnet.domain.vfs.VfsWorkspace
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem
import com.mazbayid.aegisnet.ui.components.AegisPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val REFERENCE_TRACE_MISSION_ID = "fc_m05_reference_trace"

@Composable
fun ReferenceTraceMissionScreen(
    onOpenEditor: () -> Unit,
    onOpenMissionSix: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vfsRepository = remember(context) { SharedPreferencesVfsRepository(context) }
    val evidenceRepository = remember(context) {
        RoomEvidenceGraphRepository(EvidenceDatabaseProvider.get(context))
    }
    val receiptRepository = remember(context) { SharedPreferencesRunReceiptRepository(context) }
    val progressRepository = remember(context) {
        SharedPreferencesCampaignMissionProgressRepository(context)
    }
    val scope = rememberCoroutineScope()
    val mission = remember { FirstContactMissionCatalog.find(REFERENCE_TRACE_MISSION_ID)!! }

    var graph by remember { mutableStateOf<EvidenceGraph?>(null) }
    var recordText by remember { mutableStateOf<String?>(null) }
    var approachConfirmed by remember { mutableStateOf(false) }
    var outcomeConfirmed by remember { mutableStateOf(false) }
    var evidenceAttached by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var resultText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            graph = withContext(Dispatchers.IO) {
                var loaded = evidenceRepository.load(FirstContactMissionCatalog.OPERATION_ID)
                val mergedGraph = FirstContactMissionSeed.mergeEvidenceGraph(loaded)
                if (mergedGraph != loaded) {
                    check(evidenceRepository.save(mergedGraph) is EvidenceGraphSaveResult.Saved) {
                        "Evidence references could not be initialized."
                    }
                    loaded = evidenceRepository.load(FirstContactMissionCatalog.OPERATION_ID)
                }

                val savedWorkspace = vfsRepository.loadMission(FirstContactMissionCatalog.OPERATION_ID)
                val mergedWorkspace = FirstContactMissionSeed.mergeMissionWorkspace(savedWorkspace)
                if (mergedWorkspace != savedWorkspace) {
                    check(vfsRepository.saveMission(FirstContactMissionCatalog.OPERATION_ID, mergedWorkspace)) {
                        "Mission workspace could not be initialized."
                    }
                }
                loaded
            }
            completed = withContext(Dispatchers.IO) {
                progressRepository.load().completedMissionIds.contains(mission.definition.id)
            }
        } catch (failure: Exception) {
            error = failure.message ?: "Mission data could not be loaded."
        } finally {
            loading = false
        }
    }

    AegisPanel(title = "MISSION CONTROL / REFERENCE TRACE") {
        when {
            loading -> Text("Loading reference catalog...", fontSize = 11.sp)
            error != null -> Text(error ?: "Operation error", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
            else -> {
                Text("MISSION 05 / " + mission.definition.title, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Text(mission.briefing, fontSize = 11.sp)
                Text(
                    "LEARNING: " + mission.learningConcepts.joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
                Spacer(Modifier.height(10.dp))

                Button(
                    enabled = !completed,
                    onClick = {
                        scope.launch {
                            val file = withContext(Dispatchers.IO) {
                                val fs = VirtualFileSystem.fromSnapshots(
                                    FirstContactMissionCatalog.OPERATION_ID,
                                    vfsRepository.loadPersonal(),
                                    vfsRepository.loadMission(FirstContactMissionCatalog.OPERATION_ID)
                                )
                                fs.switchWorkspace(VfsWorkspace.MISSION)
                                fs.cat("briefing/reference_catalog.txt")
                            }
                            when (file) {
                                is VfsResult.Success -> {
                                    recordText = file.value
                                    val currentGraph = graph ?: return@launch
                                    val updated = currentGraph.copy(
                                        evidence = currentGraph.evidence.map {
                                            if (it.id == "reference_catalog") it.copy(isDiscovered = true) else it
                                        }
                                    )
                                    val saved = withContext(Dispatchers.IO) {
                                        evidenceRepository.save(updated)
                                    }
                                    if (saved is EvidenceGraphSaveResult.Saved) {
                                        graph = updated
                                    } else {
                                        error = "Reference catalog was read, but its discovery state could not be saved."
                                    }
                                }
                                is VfsResult.Failure -> error = file.message
                            }
                        }
                    }
                ) { Text("OPEN REFERENCE TRACE") }

                recordText?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    enabled = !completed,
                    onClick = onOpenEditor,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("OPEN REFERENCE TRACE SCRIPT") }
                Text("SCRIPT: scripts/reference_trace.py", fontSize = 9.sp)
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = approachConfirmed,
                        onCheckedChange = { approachConfirmed = it },
                        enabled = !completed
                    )
                    Text("I used enumerate() and filtering to count the references.", fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = outcomeConfirmed,
                        onCheckedChange = { outcomeConfirmed = it },
                        enabled = !completed
                    )
                    Text("The program reports SOURCE_INDEX=2 for RX-14.", fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = evidenceAttached,
                        onCheckedChange = { evidenceAttached = it },
                        enabled = !completed
                    )
                    Text("Attach reference_catalog as supporting evidence.", fontSize = 10.sp)
                }

                Button(
                    enabled = !completed,
                    onClick = {
                        scope.launch {
                            val currentGraph = graph ?: return@launch
                            val evaluation = withContext(Dispatchers.IO) {
                                val fs = VirtualFileSystem.fromSnapshots(
                                    FirstContactMissionCatalog.OPERATION_ID,
                                    vfsRepository.loadPersonal(),
                                    vfsRepository.loadMission(FirstContactMissionCatalog.OPERATION_ID)
                                )
                                fs.switchWorkspace(VfsWorkspace.MISSION)
                                val source = (fs.cat("scripts/reference_trace.py") as? VfsResult.Success)?.value
                                MissionEvaluator.evaluate(
                                    mission.definition,
                                    MissionSubmission(
                                        missionId = mission.definition.id,
                                        objectiveApproachIds = if (approachConfirmed) mapOf(
                                            "trace_reference" to "python_enumerate_and_filter"
                                        ) else emptyMap(),
                                        objectiveOutcomeIds = if (outcomeConfirmed) mapOf(
                                            "trace_reference" to "reference_source_index_verified"
                                        ) else emptyMap(),
                                        selectedEvidenceIds = if (evidenceAttached) setOf("reference_catalog") else emptySet(),
                                        runReceipt = receiptRepository.load(FirstContactMissionCatalog.OPERATION_ID),
                                        submittedSourceHash = source?.let { EditorDocumentService.sha256(it) }
                                    ),
                                    availableEvidenceIds = currentGraph.evidence
                                        .filter { it.isDiscovered }
                                        .map { it.id }
                                        .toSet()
                                )
                            }
                            val criteria = evaluation.criteria.joinToString("\n") {
                                (if (it.passed) "PASS" else "FAIL") + " · " + it.message
                            }
                            if (evaluation.accepted) {
                                val orderedIds = FirstContactMissionCatalog.missions
                                    .sortedBy { it.sequenceNumber }
                                    .map { it.definition.id }
                                val saved = withContext(Dispatchers.IO) {
                                    when (
                                        val mutation = CampaignMissionProgression.complete(
                                            progressRepository.load(),
                                            mission.definition.id,
                                            orderedIds
                                        )
                                    ) {
                                        is CampaignMissionMutation.Accepted ->
                                            progressRepository.save(mutation.progress)
                                        is CampaignMissionMutation.Rejected -> false
                                    }
                                }
                                completed = saved
                                resultText = if (saved) {
                                    "MISSION ACCEPTED\n$criteria"
                                } else {
                                    "MISSION PASSED, BUT CAMPAIGN SAVE FAILED.\n$criteria"
                                }
                            } else {
                                resultText = "SUBMISSION NEEDS REVISION\n$criteria"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("SUBMIT REFERENCE TRACE") }

                if (resultText.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        resultText,
                        fontSize = 9.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                if (completed) {
                    Text("MISSION 05 COMPLETE", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onOpenMissionSix, modifier = Modifier.fillMaxWidth()) {
                        Text("CONTINUE TO MISSION 06 / TWO-SOURCE COMPARISON")
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("BACK TO OPERATIONS") }
    }
}
