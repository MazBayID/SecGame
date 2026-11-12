package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.layout.Arrangement
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
import com.mazbayid.aegisnet.data.evidence.EvidenceDatabaseProvider
import com.mazbayid.aegisnet.data.campaign.SharedPreferencesCampaignMissionProgressRepository
import com.mazbayid.aegisnet.data.evidence.RoomEvidenceGraphRepository
import com.mazbayid.aegisnet.data.mission.FirstContactMissionSeed
import com.mazbayid.aegisnet.data.execution.SharedPreferencesRunReceiptRepository
import com.mazbayid.aegisnet.data.vfs.SharedPreferencesVfsRepository
import com.mazbayid.aegisnet.domain.editor.EditorDocumentService
import com.mazbayid.aegisnet.domain.campaign.CampaignMissionMutation
import com.mazbayid.aegisnet.domain.campaign.CampaignMissionProgression
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

@Composable
fun FirstContactMissionScreen(
    onOpenEditor: () -> Unit,
    onOpenMissionTwo: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vfsRepo = remember(context) { SharedPreferencesVfsRepository(context) }
    val evidenceRepo = remember(context) {
        RoomEvidenceGraphRepository(EvidenceDatabaseProvider.get(context))
    }
    val receipts = remember(context) { SharedPreferencesRunReceiptRepository(context) }
    val missionProgressRepository = remember(context) {
        SharedPreferencesCampaignMissionProgressRepository(context)
    }
    val scope = rememberCoroutineScope()
    var graph by remember { mutableStateOf<EvidenceGraph?>(null) }
    var fragment by remember { mutableStateOf<String?>(null) }
    var approach by remember { mutableStateOf(false) }
    var outcome by remember { mutableStateOf(false) }
    var attachEvidence by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            graph = withContext(Dispatchers.IO) {
                var loaded = evidenceRepo.load(FirstContactMissionCatalog.OPERATION_ID)
                val mergedGraph = FirstContactMissionSeed.mergeEvidenceGraph(loaded)
                if (mergedGraph != loaded) {
                    check(evidenceRepo.save(mergedGraph) is EvidenceGraphSaveResult.Saved) {
                        "Mission evidence could not be initialized."
                    }
                    loaded = evidenceRepo.load(FirstContactMissionCatalog.OPERATION_ID)
                }
                val savedWorkspace = vfsRepo.loadMission(FirstContactMissionCatalog.OPERATION_ID)
                val mergedWorkspace = FirstContactMissionSeed.mergeMissionWorkspace(savedWorkspace)
                if (mergedWorkspace != savedWorkspace) {
                    check(vfsRepo.saveMission(FirstContactMissionCatalog.OPERATION_ID, mergedWorkspace)) {
                        "Mission workspace could not be initialized."
                    }
                }
                loaded
            }
            completed = withContext(Dispatchers.IO) {
                missionProgressRepository.load().completedMissionIds
                    .contains(FirstContactMissionCatalog.missions.first().definition.id)
            }
        } catch (failure: Exception) {
            error = failure.message ?: "Mission data could not be loaded."
        } finally {
            loading = false
        }
    }

    AegisPanel(title = "MISSION CONTROL / FIRST CONTACT") {
        if (loading) {
            Text("Loading operation data...", fontSize = 11.sp)
        } else if (error != null) {
            Text(error ?: "Operation error", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
        } else {
            val mission = FirstContactMissionCatalog.missions.first()
            Text("MISSION 01 / " + mission.definition.title, color = MaterialTheme.colorScheme.primary)
            Text(mission.briefing, fontSize = 11.sp)
            Text("LEARNING: " + mission.learningConcepts.joinToString(" · "), fontSize = 9.sp)
            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    scope.launch {
                        val source = withContext(Dispatchers.IO) {
                            val fs = VirtualFileSystem.fromSnapshots(
                                FirstContactMissionCatalog.OPERATION_ID,
                                vfsRepo.loadPersonal(),
                                vfsRepo.loadMission(FirstContactMissionCatalog.OPERATION_ID)
                            )
                            fs.switchWorkspace(VfsWorkspace.MISSION)
                            fs.cat("briefing/message_fragment.txt")
                        }
                        when (source) {
                            is VfsResult.Success -> {
                                fragment = source.value
                                val updated = graph!!.copy(
                                    evidence = graph!!.evidence.map {
                                        if (it.id == "message_fragment") it.copy(isDiscovered = true) else it
                                    }
                                )
                                val saved = withContext(Dispatchers.IO) { evidenceRepo.save(updated) }
                                if (saved is EvidenceGraphSaveResult.Saved) graph = updated
                                else error = "Evidence discovery could not be saved."
                            }
                            is VfsResult.Failure -> error = source.message
                        }
                    }
                },
                enabled = !completed
            ) { Text("DISCOVER MESSAGE FRAGMENT") }

            fragment?.let { Text("SOURCE CONTENT: " + it, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp) }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onOpenEditor, enabled = !completed) { Text("OPEN MISSION EDITOR") }
            Text("SCRIPT: scripts/analysis.py", fontSize = 9.sp)
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = approach, onCheckedChange = { approach = it }, enabled = !completed)
                Text("I used a variable and print() to inspect the fragment.", fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = outcome, onCheckedChange = { outcome = it }, enabled = !completed)
                Text("My RUN output matches the source text.", fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = attachEvidence, onCheckedChange = { attachEvidence = it }, enabled = !completed)
                Text("Attach the discovered message_fragment evidence.", fontSize = 10.sp)
            }
            Button(
                onClick = {
                    scope.launch {
                        val currentGraph = graph ?: return@launch
                        val evaluation = withContext(Dispatchers.IO) {
                            val fs = VirtualFileSystem.fromSnapshots(
                                FirstContactMissionCatalog.OPERATION_ID,
                                vfsRepo.loadPersonal(),
                                vfsRepo.loadMission(FirstContactMissionCatalog.OPERATION_ID)
                            )
                            fs.switchWorkspace(VfsWorkspace.MISSION)
                            val source = (fs.cat("scripts/analysis.py") as? VfsResult.Success)?.value
                            MissionEvaluator.evaluate(
                                mission.definition,
                                MissionSubmission(
                                    missionId = mission.definition.id,
                                    objectiveApproachIds = if (approach) mapOf(
                                        "report_message_fragment" to "python_variable_and_print"
                                    ) else emptyMap(),
                                    objectiveOutcomeIds = if (outcome) mapOf(
                                        "report_message_fragment" to "message_fragment_reported"
                                    ) else emptyMap(),
                                    selectedEvidenceIds = if (attachEvidence) setOf("message_fragment") else emptySet(),
                                    runReceipt = receipts.load(FirstContactMissionCatalog.OPERATION_ID),
                                    submittedSourceHash = source?.let { EditorDocumentService.sha256(it) }
                                ),
                                availableEvidenceIds = currentGraph.evidence.filter { it.isDiscovered }.map { it.id }.toSet()
                            )
                        }
                        resultText = evaluation.criteria.joinToString("\n") {
                            (if (it.passed) "PASS" else "FAIL") + " · " + it.message
                        }
                        if (evaluation.accepted) {
                            val orderedMissionIds = FirstContactMissionCatalog.missions
                                .sortedBy { it.sequenceNumber }
                                .map { it.definition.id }
                            val completion = withContext(Dispatchers.IO) {
                                when (
                                    val mutation = CampaignMissionProgression.complete(
                                        missionProgressRepository.load(),
                                        mission.definition.id,
                                        orderedMissionIds
                                    )
                                ) {
                                    is CampaignMissionMutation.Accepted ->
                                        missionProgressRepository.save(mutation.progress)
                                    is CampaignMissionMutation.Rejected -> false
                                }
                            }
                            completed = completion
                            resultText = if (completion) {
                                "MISSION ACCEPTED\\n" + resultText
                            } else {
                                "MISSION PASSED, BUT CAMPAIGN SAVE FAILED. CHECK STORAGE AND RETRY.\\n" + resultText
                            }
                        } else {
                            resultText = "SUBMISSION NEEDS REVISION\\n" + resultText
                        }
                    }
                },
                enabled = !completed
            ) { Text("SUBMIT MISSION") }

            if (resultText.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(resultText, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
            if (completed) {
                Text("MISSION COMPLETE", color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onOpenMissionTwo, modifier = Modifier.fillMaxWidth()) {
                    Text("CONTINUE TO MISSION 02 / IDENTITY CHECK")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("BACK") }
    }
}
