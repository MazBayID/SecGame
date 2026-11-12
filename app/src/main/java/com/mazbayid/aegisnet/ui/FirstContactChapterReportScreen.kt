package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.data.campaign.SharedPreferencesCampaignMissionProgressRepository
import com.mazbayid.aegisnet.data.campaign.SharedPreferencesChapterReportRepository
import com.mazbayid.aegisnet.domain.mission.FirstContactMissionCatalog
import com.mazbayid.aegisnet.ui.components.AegisPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FirstContactChapterReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val missionProgressRepository = remember(context) {
        SharedPreferencesCampaignMissionProgressRepository(context)
    }
    val reportRepository = remember(context) {
        SharedPreferencesChapterReportRepository(context)
    }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var chapterComplete by remember { mutableStateOf(false) }
    var acknowledged by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val state = withContext(Dispatchers.IO) {
            val progress = missionProgressRepository.load()
            val finalMissionId = FirstContactMissionCatalog.missions
                .maxBy { it.sequenceNumber }.definition.id
            progress.completedMissionIds.contains(finalMissionId) to reportRepository.isAcknowledged()
        }
        chapterComplete = state.first
        acknowledged = state.second
        loading = false
    }

    AegisPanel(title = "CAMPAIGN 01 / FINAL INTELLIGENCE REPORT") {
        when {
            loading -> Text("Verifying chapter completion...", fontSize = 11.sp)
            !chapterComplete -> {
                Text("REPORT SEALED", color = MaterialTheme.colorScheme.error)
                Text(
                    "The final report remains unavailable until all eight First Contact operations are complete.",
                    fontSize = 11.sp
                )
            }
            else -> {
                Text("SUBJECT: RX-14 / TRANSMISSION RECEIPT", color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("VERIFIED OBSERVATIONS", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                Text("• A.E.G.I.S. records RX-14 at 09:41:12.")
                Text("• ARCHIVE records the same reference at 09:41:16.")
                Text("• The two records differ by +4 seconds; absolute difference: 4 seconds.")
                Spacer(Modifier.height(10.dp))
                Text("ASSESSMENT", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    "A timestamp discrepancy is confirmed. The available records do not establish why it occurred, who caused it, or whether either source was manipulated.",
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(8.dp))
                Text("RECOMMENDATION", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Preserve both original records and request independent corroboration before attributing responsibility.",
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(12.dp))
                Text("FREYA KNOX / MENTOR DEBRIEF", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    "You proved the discrepancy. You did not invent a culprit. That restraint is part of the job. A credible report tells us what the evidence supports—and where certainty ends.",
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(12.dp))
                if (saveError) {
                    Text(
                        "ACKNOWLEDGEMENT COULD NOT BE SAVED. TRY AGAIN.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 9.sp
                    )
                    Spacer(Modifier.height(6.dp))
                }
                if (acknowledged) {
                    Text("CHAPTER 01 / FIRST CONTACT CLOSED", color = MaterialTheme.colorScheme.primary)
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                val saved = withContext(Dispatchers.IO) {
                                    reportRepository.acknowledge()
                                }
                                acknowledged = saved
                                saveError = !saved
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ACKNOWLEDGE FINAL REPORT") }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("RETURN TO OPERATIONS")
        }
    }
}
