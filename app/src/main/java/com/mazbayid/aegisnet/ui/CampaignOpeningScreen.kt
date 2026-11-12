package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.data.campaign.SharedPreferencesCampaignProgressRepository
import com.mazbayid.aegisnet.domain.campaign.CampaignProgress
import com.mazbayid.aegisnet.domain.campaign.CampaignProgressMutation
import com.mazbayid.aegisnet.domain.campaign.CampaignProgression
import com.mazbayid.aegisnet.domain.campaign.CampaignStep
import com.mazbayid.aegisnet.ui.components.AegisPanel

@Composable
fun CampaignOpeningScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) {
        SharedPreferencesCampaignProgressRepository(context)
    }
    var progress by remember { mutableStateOf(repository.load()) }
    var saveError by remember { mutableStateOf(false) }
    val nextStep = CampaignProgression.nextStep(progress)

    AegisPanel(
        title = "CAMPAIGN 01 / FIRST CONTACT",
        trailing = {
            Text(
                "LOCAL SAVE",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    ) {
        Text(
            "THE RECRUITMENT",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "OPENING SEQUENCE  /  ${progress.completedSteps.size} OF ${CampaignStep.entries.size}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
        Spacer(Modifier.height(14.dp))

        when (nextStep) {
            CampaignStep.OFFICIAL_MESSAGE_OPENED -> StoryBeat(
                eyebrow = "INCOMING SECURE MESSAGE",
                title = "PESAN AMAN",
                body = "A.E.G.I.S.  /  RECRUITMENT OFFICE",
                detail = "Sebuah pesan aman menunggu untuk dibuka. Ketuk untuk membaca percakapan resmi."
            )
            CampaignStep.MISSION_SEQUENCE_OPENED -> StoryBeat(
                eyebrow = "SECURE CONVERSATION",
                title = "REKRUTMEN A.E.G.I.S.",
                body = "Seseorang telah memberikan perintah kepada A.E.G.I.S. untuk merekrutmu.",
                detail = "Identitas pemberi perintah belum diungkap. Rangkaian misi hanya dimulai jika kamu membuka tautannya."
            )
            CampaignStep.FIRST_MISSION_ACTIVATED -> StoryBeat(
                eyebrow = "MISSION SEQUENCE",
                title = "FIRST CONTACT",
                body = "Rangkaian misi Campaign 01 telah dibuka.",
                detail = "Aktifkan misi pertama untuk melanjutkan proses perekrutan. Konten operasional misi akan hadir pada slice berikutnya."
            )
            CampaignStep.FREYA_MESSAGE_OPENED -> StoryBeat(
                eyebrow = "NEW MESSAGE NOTIFICATION",
                title = "FREYA KNOX",
                body = "Sebuah pesan baru muncul di atas layar misi.",
                detail = "Pesan ini tidak dibuka otomatis. Ketuk untuk menemui mentor yang ditugaskan kepadamu."
            )
            CampaignStep.NEXUS_LINK_OPENED -> StoryBeat(
                eyebrow = "FREYA / SECURE CHAT",
                title = "MENTOR ASSIGNED",
                body = "Namaku Freya Knox. Aku akan menjadi mentor teknismu. Dan sebelum kamu bertanya—ya, aku memang sehebat itu.",
                detail = "Freya memperkenalkan NEXUS sebagai kanal dukungan sistem. Kontak NEXUS belum tersedia sampai tautan di bawah dibuka."
            )
            CampaignStep.NEXUS_CHANNEL_OPENED -> StoryBeat(
                eyebrow = "CONTACT UNLOCKED",
                title = "NEXUS / KANAL SISTEM",
                body = "Kontak NEXUS sekarang tersedia di inbox.",
                detail = "NEXUS adalah sistem pendukung teknis berbasis teks, bukan mentor utama. Pembukaan kanal ini tersimpan di perangkat."
            )
            null -> StoryBeat(
                eyebrow = "OPENING SEQUENCE COMPLETE",
                title = "NEXUS ONLINE",
                body = "Seluruh interaksi pembukaan First Contact yang tersedia pada build ini telah diselesaikan.",
                detail = "Orientasi teknis, Intranet, dan operasi pertama belum dibuka pada build ini. Progresmu aman tersimpan."
            )
        }

        if (saveError) {
            Spacer(Modifier.height(8.dp))
            Text(
                "SAVE ERROR — progress could not be confirmed on device storage. Try again.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 9.sp
            )
        }

        if (nextStep != null) {
            Spacer(Modifier.height(14.dp))
            CampaignActionButton(
                label = nextStep.actionLabel(),
                onClick = {
                    val result = CampaignProgression.advance(progress, nextStep)
                    if (result is CampaignProgressMutation.Accepted) {
                        if (repository.save(result.progress)) {
                            progress = result.progress
                            saveError = false
                        } else {
                            saveError = true
                        }
                    }
                }
            )
        }

        Spacer(Modifier.height(10.dp))
        CampaignActionButton("RETURN TO MAIN MENU", onBack, secondary = true)
    }
}

@Composable
private fun StoryBeat(
    eyebrow: String,
    title: String,
    body: String,
    detail: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            eyebrow,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 8.sp,
            letterSpacing = 1.sp
        )
        Text(
            title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                body,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            detail,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun CampaignActionButton(
    label: String,
    onClick: () -> Unit,
    secondary: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (secondary) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.small,
        onClick = onClick
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            color = if (secondary) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp
        )
    }
}

private fun CampaignStep.actionLabel(): String = when (this) {
    CampaignStep.OFFICIAL_MESSAGE_OPENED -> "BUKA PESAN AMAN"
    CampaignStep.MISSION_SEQUENCE_OPENED -> "BUKA RANGKAIAN MISI"
    CampaignStep.FIRST_MISSION_ACTIVATED -> "AKTIFKAN MISI PERTAMA"
    CampaignStep.FREYA_MESSAGE_OPENED -> "BUKA PESAN FREYA"
    CampaignStep.NEXUS_LINK_OPENED -> "NEXUS / KANAL SISTEM"
    CampaignStep.NEXUS_CHANNEL_OPENED -> "BUKA KANAL NEXUS"
}
