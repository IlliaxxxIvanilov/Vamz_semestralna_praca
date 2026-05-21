package com.example.semestralka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.semestralka.ui.navigation.ZilinaGuideNavGraph
import com.example.semestralka.ui.theme.ZilinaGuideTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZilinaGuideTheme {
                ZilinaGuideNavGraph()
            }
        }
    }
}