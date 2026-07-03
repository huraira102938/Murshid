package com.huraira.murshid.ui.screens.main

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.huraira.murshid.navigation.MurshidNavHost

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    MurshidNavHost(navController = navController)
}