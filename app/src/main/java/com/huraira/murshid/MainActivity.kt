package com.huraira.murshid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.huraira.murshid.ui.screens.main.MainScreen
import com.huraira.murshid.ui.theme.MurshidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MurshidTheme {
                MainScreen()
            }
        }
    }
}