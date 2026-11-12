package com.mazbayid.aegisnet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DeepBlack = Color(0xFF050807)
private val Carbon = Color(0xFF0B120E)
private val DarkTerminal = Color(0xFF10241A)
private val SignalGreen = Color(0xFF39FF88)
private val MatrixGreen = Color(0xFF1F6B45)
private val SoftMint = Color(0xFFA7FFC8)
private val MutedGreen = Color(0xFF648A73)
private val ErrorRed = Color(0xFFFF6B6B)

private val AegisColorScheme = darkColorScheme(
    primary = SignalGreen,
    onPrimary = DeepBlack,
    secondary = MatrixGreen,
    onSecondary = SoftMint,
    background = DeepBlack,
    onBackground = SoftMint,
    surface = Carbon,
    onSurface = SoftMint,
    surfaceVariant = DarkTerminal,
    onSurfaceVariant = MutedGreen,
    outline = MatrixGreen,
    error = ErrorRed,
    onError = DeepBlack
)

@Composable
fun AegisNetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AegisColorScheme,
        content = content
    )
}
