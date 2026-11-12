package com.mazbayid.aegisnet.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.ui.components.AegisPanel

private data class MenuAction(val label: String, val detail: String)

private val appTiles = listOf(
    MenuAction("MESSENGER", "SECURE COMMUNICATION"),
    MenuAction("INTRANET", "INTERNAL NETWORK"),
    MenuAction("TERMINAL", "COMMAND INTERFACE"),
    MenuAction("DATABASE", "INTELLIGENCE RECORDS"),
    MenuAction("DOSSIER", "PERSONNEL & EVIDENCE"),
    MenuAction("ANALYSIS", "INVESTIGATION TOOLS"),
    MenuAction("OPERATIONS", "MISSION CONTROL")
)

@Composable
fun AegisNetApp() {
    var selected by remember { mutableStateOf("MAIN MENU") }
    var hubOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TacticalGridBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            HeaderBar(
                onHubClick = { hubOpen = !hubOpen },
                hubOpen = hubOpen
            )

            if (hubOpen) {
                AppHub(
                    onSelect = {
                        selected = it
                        hubOpen = false
                    }
                )
            } else {
                AegisPanel(
                    title = "Secure connection",
                    trailing = {
                        Text(
                            "ENCRYPTED",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                ) {
                    Text(
                        "A.E.G.I.S. INTERNAL NETWORK",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "KNOWLEDGE IS POWER. INTELLIGENCE IS RESPONSIBILITY.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        letterSpacing = 0.7.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusDot()
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "LOCAL SYSTEM READY",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                AegisPanel(title = "Current assignment") {
                    Text(
                        "CAMPAIGN 01  /  THE RECRUITMENT",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "FIRST CONTACT",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Your first steps into the A.E.G.I.S. intelligence network.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    ActionButton(
                        text = "ENTER CAMPAIGN",
                        onClick = { selected = "CAMPAIGN OVERVIEW" },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AegisPanel(title = "System telemetry") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryValue("NETWORK", "OFFLINE")
                        TelemetryValue("CLEARANCE", "RECRUIT")
                        TelemetryValue("STATUS", "STANDBY")
                    }
                }

                AnimatedVisibility(
                    visible = selected != "MAIN MENU",
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    AegisPanel(title = selected) {
                        Text(
                            "This interface module is reserved for the next implementation slice.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        ActionButton("RETURN TO MAIN MENU", { selected = "MAIN MENU" })
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "AEGIS-NET  //  BUILD 0.1.0  //  OFFLINE-FIRST",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun HeaderBar(onHubClick: () -> Unit, hubOpen: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "A.E.G.I.S.",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp
            )
            Text(
                "ADVANCED ESPIONAGE & GLOBAL INTELLIGENCE SERVICE",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 7.sp,
                letterSpacing = 0.65.sp
            )
        }
        Surface(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                .clickable(onClick = onHubClick),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                if (hubOpen) "CLOSE  ×" else "APP HUB  +",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AppHub(onSelect: (String) -> Unit) {
    AegisPanel(title = "Application hub") {
        Text(
            "AUTHORIZED LOCAL MODULES",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(330.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(appTiles) { app ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(app.label) },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            app.label,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            app.detail,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryValue(label: String, value: String) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusDot() {
    Canvas(Modifier.size(8.dp)) {
        drawCircle(MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(5.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = Color(0xFF050807),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun TacticalGridBackground() {
    Canvas(Modifier.fillMaxSize()) {
        val gridColor = Color(0xFF1F6B45).copy(alpha = 0.16f)
        val step = 34.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 0.6.dp.toPx())
            x += step
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 0.6.dp.toPx())
            y += step
        }
        drawCircle(
            color = Color(0xFF39FF88).copy(alpha = 0.18f),
            radius = size.minDimension * 0.34f,
            center = Offset(size.width * 0.92f, size.height * 0.16f),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
