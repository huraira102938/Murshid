package com.huraira.murshid.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.huraira.murshid.ui.main.MainPagerScreen
import com.huraira.murshid.ui.screens.about.AboutScreen
import com.huraira.murshid.ui.screens.updates.UpdateDetailScreen
import com.huraira.murshid.ui.screens.wallpapers.WallpaperDetailScreen

@Composable
fun MurshidNavHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(220)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(220)) }
    ) {
        composable(Screen.Main.route) {
            MainPagerScreen(navController = navController)
        }

        composable(
            route = Screen.WallpaperDetail.route,
            arguments = listOf(navArgument(Screen.WallpaperDetail.ARG_WALLPAPER_ID) { type = NavType.StringType }),
            enterTransition = { fadeIn(tween(280)) },
            exitTransition = { fadeOut(tween(200)) }
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Screen.WallpaperDetail.ARG_WALLPAPER_ID).orEmpty()
            WallpaperDetailScreen(
                wallpaperId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.UpdateDetail.route,
            arguments = listOf(navArgument(Screen.UpdateDetail.ARG_UPDATE_ID) { type = NavType.StringType }),
            enterTransition = { slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)) }
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Screen.UpdateDetail.ARG_UPDATE_ID).orEmpty()
            UpdateDetailScreen(
                updateId = id,
                onBack = { navController.popBackStack() },
                onShare = {}
            )
        }

        composable(
            route = Screen.About.route,
            enterTransition = { slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)) }
        ) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}