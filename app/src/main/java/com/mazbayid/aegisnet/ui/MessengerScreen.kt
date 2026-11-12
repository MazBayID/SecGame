package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.mazbayid.aegisnet.data.campaign.SharedPreferencesCampaignProgressRepository
import com.mazbayid.aegisnet.domain.campaign.CampaignProgress
import com.mazbayid.aegisnet.domain.campaign.CampaignStep
import com.mazbayid.aegisnet.ui.components.AegisPanel

private data class SecureConversation(
    val id: String,
    val sender: String,
    val channel: String,
    val preview: String,
    val unlockStep: CampaignStep,
    val messages: List<Pair<String, String>>
)

@Composable
fun MessengerScreen(
    onOpenCampaign: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val campaignRepository = remember(context) {
        SharedPreferencesCampaignProgressRepository(context)
    }
    val progress = remember(context) { campaignRepository.load() }
    var selectedConversationId by remember { mutableStateOf<String?>(null) }

    val conversations = remember {
        listOf(
            SecureConversation(
                id = "recruitment",
                sender = "RECRUITMENT OFFICE",
                channel = "OFFICIAL / SECURE",
                preview = "An instruction has been issued to recruit you.",
                unlockStep = CampaignStep.OFFICIAL_MESSAGE_OPENED,
                messages = listOf(
                    "RECRUITMENT OFFICE" to
                        "Seseorang telah memberikan perintah kepada A.E.G.I.S. untuk merekrutmu.",
                    "RECRUITMENT OFFICE" to
                        "Identitas pemberi perintah dibatasi. Rangkaian misi Campaign 01 tersedia melalui kanal Campaign."
                )
            ),
            SecureConversation(
                id = "freya",
                sender = "FREYA KNOX",
                channel = "MENTOR / SECURE CHAT",
                preview = "Namaku Freya Knox. Aku ditugaskan sebagai mentormu.",
                unlockStep = CampaignStep.FREYA_MESSAGE_OPENED,
                messages = listOf(
                    "FREYA KNOX" to
                        "Namaku Freya Knox. Aku ditugaskan sebagai mentor yang akan mendampingimu selama penugasan.",
                    "FREYA KNOX" to
                        "Kita akan mulai dari dasar. Jika ada yang belum kamu pahami, tanyakan saja.",
                    "FREYA KNOX" to
                        "Aku juga akan memperkenalkan kanal dukungan sistem. NEXUS bisa membantu memberi informasi teknis, tetapi aku tetap menjadi mentormu."
                )
            ),
            SecureConversation(
                id = "nexus",
                sender = "NEXUS",
                channel = "SYSTEM AI / SECURE",
                preview = "Secondary technical support channel available.",
                unlockStep = CampaignStep.NEXUS_CHANNEL_OPENED,
                messages = listOf(
                    "NEXUS" to
                        "Kanal sistem aktif. Saya adalah NEXUS, sistem AI komunikasi-intelijen A.E.G.I.S.",
                    "NEXUS" to
                        "Saya menyediakan peringatan, informasi sistem, referensi, dan dukungan teknis sekunder. Saya tidak mengambil alih keputusan operasional atau peran mentor manusia."
                )
            )
        )
    }
    val available = conversations.filter { it.unlockStep in progress.completedSteps }
    val selected = available.firstOrNull { it.id == selectedConversationId }

    AegisPanel(
        title = if (selected == null) "MESSENGER / SECURE INBOX" else "MESSENGER / SECURE THREAD",
        trailing = {
            Text(
                "LOCAL ARCHIVE",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 8.sp
            )
        }
    ) {
        if (selected == null) {
            Text(
                "AUTHORIZED CONVERSATIONS",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 9.sp
            )
            Text(
                "Contacts appear only after their introduction has been opened in the campaign.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
            Spacer(Modifier.height(10.dp))
            if (available.isEmpty()) {
                Text(
                    "NO CONTACTS UNLOCKED. Continue the Campaign 01 opening sequence to receive secure messages.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            } else {
                available.forEach { conversation ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedConversationId = conversation.id },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    conversation.sender,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp
                                )
                                Text("AVAILABLE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                            }
                            Text(conversation.channel, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(conversation.preview, fontSize = 10.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onOpenCampaign, modifier = Modifier.fillMaxWidth()) {
                Text("OPEN CAMPAIGN SEQUENCE")
            }
        } else {
            Text(selected.sender, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            Text(selected.channel, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
            Spacer(Modifier.height(10.dp))
            selected.messages.forEachIndexed { index, message ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        message.first + "  /  MESSAGE ${index + 1}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 8.sp
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            message.second,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (selected.id == "recruitment" || selected.id == "freya") {
                Button(onClick = onOpenCampaign, modifier = Modifier.fillMaxWidth()) {
                    Text("RETURN TO CAMPAIGN SEQUENCE")
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { selectedConversationId = null },
                modifier = Modifier.fillMaxWidth()
            ) { Text("BACK TO INBOX") }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Messages are authored local campaign records. No network connection is required.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 8.sp
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("RETURN TO APP HUB")
        }
    }
}
