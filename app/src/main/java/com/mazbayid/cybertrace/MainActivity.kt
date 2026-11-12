package com.mazbayid.cybertrace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF070B12)
private val Panel = Color(0xFF0D1420)
private val Border = Color(0xFF1B3544)
private val Neon = Color(0xFF55F2C2)
private val Muted = Color(0xFF7E91A5)
private val MainText = Color(0xFFE2EDF5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.rgb(7, 11, 18)
        window.navigationBarColor = android.graphics.Color.rgb(7, 11, 18)

        setContent {
            MaterialTheme {
                CyberTraceStartScreen()
            }
        }
    }
}

@Composable
private fun CyberTraceStartScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "CYBER//TRACE",
                            color = MainText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "NEXUS CITY  /  2089",
                            color = Muted,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    StatusIndicator()
                }

                Spacer(modifier = Modifier.height(34.dp))

                Text(
                    text = "INCOMING TRANSMISSION",
                    color = Neon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "The network\nis not what it seems.",
                    color = MainText,
                    fontSize = 32.sp,
                    lineHeight = 39.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "A signal has appeared in the noise. Someone is trying to reach you.",
                    color = Muted,
                    fontSize = 15.sp,
                    lineHeight = 23.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                TransmissionPanel()
            }

            Column {
                Text(
                    text = "PHASE 01  /  SYSTEM INITIALIZATION",
                    color = Muted,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FOUNDATION BUILD  •  0.1.0",
                    color = Neon,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator() {
    Row(
        modifier = Modifier
            .border(1.dp, Border, RoundedCornerShape(6.dp))
            .background(Panel, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(6.dp)
                .background(Neon, RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = "LOCAL SYSTEM",
            color = Neon,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun TransmissionPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .background(Panel, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "UNKNOWN SOURCE",
            color = Muted,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "> connection established",
            color = Neon,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "> encrypted packet detected",
            color = MainText,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "> awaiting decryption module...",
            color = Muted,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
