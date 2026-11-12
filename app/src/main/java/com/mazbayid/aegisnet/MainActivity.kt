package com.mazbayid.aegisnet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mazbayid.aegisnet.ui.AegisNetApp
import com.mazbayid.aegisnet.ui.theme.AegisNetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AegisNetTheme {
                AegisNetApp()
            }
        }
    }
}
