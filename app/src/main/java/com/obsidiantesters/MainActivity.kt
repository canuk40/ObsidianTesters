package com.obsidiantesters

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.obsidiantesters.ui.navigation.ObsidianTestersNavGraph
import com.obsidiantesters.ui.theme.ObsidianTestersTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ObsidianTestersTheme {
                ObsidianTestersNavGraph()
            }
        }
    }
}
