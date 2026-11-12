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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.ui.components.AegisPanel

/** Offline-first internal network; entries are authored, not live telemetry. */
@Composable
fun IntranetScreen(
    onOpenOperations: () -> Unit,
    onOpenDatabase: () -> Unit,
    onOpenMessenger: () -> Unit,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AegisPanel(title = "INTRANET / INTERNAL BULLETIN") {
            Text("LOCAL ARCHIVE · OFFLINE ACCESS", color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("This workstation contains locally authored internal notices. No network connection is required, and no live server status is implied.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            Bulletin("HR / 001", "Recruitment intake acknowledged",
                "Candidate RECRUIT-014 has entered the First Contact evaluation track. Recruitment records remain authoritative; conclusions require evidence.")
            Bulletin("OPS / 014", "RX-14 receipt discrepancy",
                "Two preserved receipt sources report different timestamps for the same reference. The discrepancy is confirmed; its cause remains unresolved.")
            Bulletin("SEC / 003", "Evidence handling reminder",
                "Preserve source records. Add analyst notes separately. Do not overwrite original evidence or present an unverified hypothesis as fact.")
        }
        AegisPanel(title = "DIRECTORY / AUTHORIZED DEPARTMENTS") {
            Department("OPERATIONS", "Active assignments, objectives, submissions and debriefs.",
                "OPEN MISSION CONTROL", onOpenOperations)
            Department("INTELLIGENCE DATABASE", "Discovered records, provenance, integrity and analyst annotations.",
                "OPEN EVIDENCE ARCHIVE", onOpenDatabase)
            Department("SECURE COMMUNICATIONS", "Recruitment correspondence and campaign contact history.",
                "OPEN MESSENGER", onOpenMessenger)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("BACK") }
            Button(onClick = onOpenOperations, modifier = Modifier.weight(1f)) { Text("RESUME OPERATIONS") }
        }
    }
}

@Composable
private fun Bulletin(code: String, title: String, body: String) {
    Column(Modifier.padding(vertical = 7.dp)) {
        Text(code, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
        Spacer(Modifier.height(3.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(3.dp))
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
}

@Composable
private fun Department(name: String, detail: String, actionLabel: String, onClick: () -> Unit) {
    Column(Modifier.padding(vertical = 7.dp)) {
        Text(name, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        Spacer(Modifier.height(5.dp))
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(actionLabel, fontSize = 9.sp) }
    }
}
