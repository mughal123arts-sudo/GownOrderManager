package com.mughalarts.gownordermanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mughalarts.gownordermanager.navigation.GownApp
import com.mughalarts.gownordermanager.ui.theme.GownOrderManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GownOrderManagerTheme {
                GownApp()
            }
        }
    }
}
