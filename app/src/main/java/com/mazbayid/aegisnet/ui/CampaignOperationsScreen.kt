package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.mazbayid.aegisnet.data.campaign.SharedPreferencesCampaignMissionProgressRepository
import com.mazbayid.aegisnet.domain.campaign.CampaignMissionProgress
import com.mazbayid.aegisnet.domain.campaign.CampaignMissionProgression
import com.mazbayid.aegisnet.domain.campaign.CampaignRankProgression
import com.mazbayid.aegisnet.domain.mission.FirstContactMissionCatalog
import com.mazbayid.aegisnet.ui.components.AegisPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CampaignOperationsScreen(
    onOpenMission: (Int) -> Unit,
    onOpenChapterReport: () -> Unit,
    onOpenCampaign: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) {
        SharedPreferencesCampaignMissionProgressRepository(context)
    }
    val missions = remember {
        FirstContactMissionCatalog.missions.sortedBy { it.sequenceNumber }
    }
    val orderedIds = remember(missions) { missions.map { it.definition.id } }
    var progress by remember { mutableStateOf(CampaignMissionProgress()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(repository) {
        try {
            progress = withContext(Dispatchers.IO) { repository.load() }
        } catch (failure: Exception) {
            error = failure.message ?: "Campaign progress could not be loaded."
        } finally {
            loading = false
        }
    }

    val nextMissionId = CampaignMissionProgression.nextMissionId(progress, orderedIds)
    val completeCount = progress.completedMissionIds.size
    val chapterComplete = completeCount == missions.size

    AegisPanel(title = "OPERATIONS / MISSION CONTROL") {
        Text("CAMPAIGN 01  /  CHAPTER 001: FIRST CONTACT", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
        Spacer(Modifier.height(5.dp))
        Text("MISSION READINESS  ·  $completeCount / ${missions.size} COMPLETE", fontSize = 10.sp)
        Text(
            "PERSONNEL RANK  ·  ${CampaignRankProgression.rank(progress).displayLabel}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
        if (loading) {
            Spacer(Modifier.height(8.dp))
            Text("Loading campaign authorization...", fontSize = 10.sp)
        } else if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error ?: "Progress error", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
        } else {
            Spacer(Modifier.height(12.dp))
            missions.forEach { mission ->
                val id = mission.definition.id
                val completed = id in progress.completedMissionIds
                val available = completed || id == nextMissionId
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "MISSION ${mission.sequenceNumber.toString().padStart(2, '0')}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 8.sp
                        )
                        Text(
                            when {
                                completed -> "COMPLETE"
                                available -> "AVAILABLE"
                                else -> "LOCKED"
                            },
                            color = if (available) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 8.sp
                        )
                    }
                    Text(mission.definition.title, fontSize = 12.sp)
                    Text(
                        mission.learningConcepts.joinToString(" · "),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 8.sp
                    )
                    if (available) {
                        OutlinedButton(
                            onClick = { onOpenMission(mission.sequenceNumber) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (completed) "REVIEW OPERATION" else "OPEN AVAILABLE OPERATION")
                        }
                    } else {
                        Text(
                            "Complete the preceding operation to unlock this assignment.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 8.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Button(
                onClick = onOpenChapterReport,
                enabled = chapterComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (chapterComplete) "OPEN FINAL INTELLIGENCE REPORT" else "FINAL REPORT SEALED")
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onOpenCampaign, modifier = Modifier.fillMaxWidth()) {
            Text("REVIEW CAMPAIGN BRIEFING")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("RETURN TO APP HUB")
        }
    }
}
