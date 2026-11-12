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
import com.mazbayid.aegisnet.domain.mission.MissionEvaluation
import com.mazbayid.aegisnet.domain.mission.MissionSubmission
import com.mazbayid.aegisnet.domain.vfs.VfsResult
import com.mazbayid.aegisnet.domain.vfs.VfsWorkspace
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem
import com.mazbayid.aegisnet.ui.components.AegisPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TWO_SOURCE_MISSION_ID = "fc_m06_two_source_comparison"

@Composable
fun TwoSourceComparisonMissionScreen(
    onOpenEditor: () -> Unit,
    onOpenMissionSeven: () -> Unit,
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
    val mission = remember { FirstContactMissionCatalog.find(TWO_SOURCE_MISSION_ID)!! }

    var graph by remember { mutableStateOf<EvidenceGraph?>(null) }
    var sourceText by remember { mutableStateOf<String?>(null) }
    var approachConfirmed by remember { mutableStateOf(false) }
    var outcomeConfirmed by remember { mutableStateOf(false) }
    var evidenceAttached by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var resultText by remember { mutableStateOf("") }
    var evaluationResult by remember { mutableStateOf<MissionEvaluation?>(null) }

    LaunchedEffect(Unit) {
        try {
            graph = withContext(Dispatchers.IO) {
                var loaded = evidenceRepository.load(FirstContactMissionCatalog.OPERATION_ID)
                val mergedGraph = FirstContactMissionSeed.mergeEvidenceGraph(loaded)
                if (mergedGraph != loaded) {
                    check(evidenceRepository.save(mergedGraph) is EvidenceGraphSaveResult.Saved) {
                        "Evidence records could not be initialized."
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

    AegisPanel(title = "MISSION CONTROL / TWO-SOURCE COMPARISON") {
        when {
            loading -> Text("Loading paired receipt records...", fontSize = 11.sp)
            error != null -> Text(error ?: "Operation error", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
            else -> {
                Text("MISSION 06 / " + mission.definition.title, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Text(mission.briefing, fontSize = 11.sp)
                Text("LEARNING: " + mission.learningConcepts.joinToString(" · "), fontSize = 9.sp)
                Spacer(Modifier.height(10.dp))

                Button(
                    enabled = !completed,
                    onClick = {
                        scope.launch {
                            val records = withContext(Dispatchers.IO) {
                                val fs = VirtualFileSystem.fromSnapshots(
                                    FirstContactMissionCatalog.OPERATION_ID,
                                    vfsRepository.loadPersonal(),
                                    vfsRepository.loadMission(FirstContactMissionCatalog.OPERATION_ID)
                                )
                                fs.switchWorkspace(VfsWorkspace.MISSION)
                                val aegis = fs.cat("briefing/aegis_receipt.txt")
                                val archive = fs.cat("briefing/archive_receipt.txt")
                                if (aegis is VfsResult.Success && archive is VfsResult.Success) {
                                    aegis.value to archive.value
                                } else {
                                    null
                                }
                            }
                            if (records == null) {
                                error = "One or both source receipt records could not be read."
                                return@launch
                            }
                            sourceText = "A.E.G.I.S. SOURCE\n" + records.first +
                                "\n\nARCHIVE SOURCE\n" + records.second
                            val currentGraph = graph ?: return@launch
                            val updated = currentGraph.copy(
                                evidence = currentGraph.evidence.map {
                                    if (it.id == "aegis_receipts" || it.id == "archive_receipts") {
                                        it.copy(isDiscovered = true)
                                    } else it
                                }
                            )
                            val saved = withContext(Dispatchers.IO) { evidenceRepository.save(updated) }
                            if (saved is EvidenceGraphSaveResult.Saved) graph = updated
                            else error = "Source records were read, but discovery could not be saved."
                        }
                    }
                ) { Text("OPEN BOTH SOURCE RECEIPTS") }

                sourceText?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Spacer(Modifier.height(8.dp))
                Button(enabled = !completed, onClick = onOpenEditor, modifier = Modifier.fillMaxWidth()) {
                    Text("OPEN SOURCE COMPARISON SCRIPT")
                }
                Text("SCRIPT: scripts/source_compare.py", fontSize = 9.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = approachConfirmed, onCheckedChange = { approachConfirmed = it }, enabled = !completed)
                    Text("I used list indexing to inspect both source records.", fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = outcomeConfirmed, onCheckedChange = { outcomeConfirmed = it }, enabled = !completed)
                    Text("The program reports matching RX-14 references and both recorded times.", fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = evidenceAttached, onCheckedChange = { evidenceAttached = it }, enabled = !completed)
                    Text("Attach both source receipt records.", fontSize = 10.sp)
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
                                val source = (fs.cat("scripts/source_compare.py") as? VfsResult.Success)?.value
                                MissionEvaluator.evaluate(
                                    mission.definition,
                                    MissionSubmission(
                                        missionId = mission.definition.id,
                                        objectiveApproachIds = if (approachConfirmed) mapOf(
                                            "compare_receipt_records" to "python_list_indexing"
                                        ) else emptyMap(),
                                        objectiveOutcomeIds = if (outcomeConfirmed) mapOf(
                                            "compare_receipt_records" to "two_source_records_compared"
                                        ) else emptyMap(),
                                        selectedEvidenceIds = if (evidenceAttached) {
                                            setOf("aegis_receipts", "archive_receipts")
                                        } else emptySet(),
                                        runReceipt = receiptRepository.load(FirstContactMissionCatalog.OPERATION_ID),
                                        submittedSourceHash = source?.let { EditorDocumentService.sha256(it) }
                                    ),
                                    availableEvidenceIds = currentGraph.evidence
                                        .filter { it.isDiscovered }
                                        .map { it.id }
                                        .toSet()
                                )
                            }
                            evaluationResult = evaluation
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
                                        is CampaignMissionMutation.Accepted -> progressRepository.save(mutation.progress)
                                        is CampaignMissionMutation.Rejected -> false
                                    }
                                }
                                completed = saved
                                resultText = if (saved) "MISSION ACCEPTED\n$criteria"
                                else "MISSION PASSED, BUT CAMPAIGN SAVE FAILED.\n$criteria"
                            } else {
                                resultText = "SUBMISSION NEEDS REVISION\n$criteria"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("SUBMIT SOURCE COMPARISON") }

                if (resultText.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    evaluationResult?.let { MissionDebriefPanel(it) }
                Text(resultText.substringBefore('\n'), fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
                if (completed) {
                    Text("MISSION 06 COMPLETE", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onOpenMissionSeven, modifier = Modifier.fillMaxWidth()) {
                        Text("CONTINUE TO MISSION 07 / TIME NORMALIZATION")
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("BACK TO OPERATIONS") }
    }
}
