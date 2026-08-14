package com.huraira.murshid.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.huraira.murshid.ui.main.MainPagerScreen
import com.huraira.murshid.ui.screens.about.AboutScreen
import com.huraira.murshid.ui.screens.updates.UpdateDetailScreen
import com.huraira.murshid.ui.screens.wallpapers.WallpaperDetailScreen
import com.huraira.murshid.viewmodel.UpdatesViewModel
import com.huraira.murshid.viewmodel.WallpapersViewModel
// region ADMIN — remove before Play Store release
import com.huraira.murshid.ui.screens.admin.AdminCategoriesScreen
import com.huraira.murshid.ui.screens.admin.AdminHomeScreen
import com.huraira.murshid.ui.screens.admin.AdminLibraryScreen
import com.huraira.murshid.ui.screens.admin.AdminNotificationsScreen
import com.huraira.murshid.ui.screens.admin.AdminUpdatesScreen
import com.huraira.murshid.ui.screens.admin.AdminWallpapersScreen
// endregion

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
            // Reuse the same WallpapersViewModel the grid tab already loaded (both are
            // scoped to the Main route's backstack entry) instead of creating a fresh one
            // — that fresh instance would otherwise refetch the whole list from Firestore
            // again just to show a wallpaper the user is already looking at.
            val mainEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Main.route) }
            val sharedWallpapersViewModel: WallpapersViewModel = viewModel(mainEntry)
            WallpaperDetailScreen(
                wallpaperId = id,
                onBack = { navController.popBackStack() },
                viewModel = sharedWallpapersViewModel
            )
        }

        composable(
            route = Screen.UpdateDetail.route,
            arguments = listOf(navArgument(Screen.UpdateDetail.ARG_UPDATE_ID) { type = NavType.StringType }),
            enterTransition = { slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)) }
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Screen.UpdateDetail.ARG_UPDATE_ID).orEmpty()
            // Same sharing trick as WallpaperDetail above.
            val mainEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Main.route) }
            val sharedUpdatesViewModel: UpdatesViewModel = viewModel(mainEntry)
            UpdateDetailScreen(
                updateId = id,
                onBack = { navController.popBackStack() },
                onShare = {},
                viewModel = sharedUpdatesViewModel
            )
        }

        composable(
            route = Screen.About.route,
            enterTransition = { slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)) }
        ) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        // region ADMIN — remove before Play Store release
        composable(
            route = Screen.AdminHome.route,
            enterTransition = { slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)) }
        ) {
            AdminHomeScreen(
                onBack = { navController.popBackStack() },
                onWallpapers = { navController.navigate(Screen.AdminWallpapers.route) },
                onLibrary = { navController.navigate(Screen.AdminLibrary.route) },
                onUpdates = { navController.navigate(Screen.AdminUpdates.route) },
                onNotifications = { navController.navigate(Screen.AdminNotifications.route) }
            )
        }

        composable(
            route = Screen.AdminWallpapers.route,
            enterTransition = { slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)) }
        ) {
            AdminWallpapersScreen(
                onBack = { navController.popBackStack() },
                onManageCategories = { navController.navigate(Screen.AdminCategories.route) }
            )
        }

        composable(
            route = Screen.AdminCategories.route,
            enterTransition = { slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)) }
        ) {
            AdminCategoriesScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.AdminLibrary.route,
            enterTransition = { slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)) }
        ) {
            AdminLibraryScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.AdminUpdates.route,
            enterTransition = { slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)) }
        ) {
            AdminUpdatesScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.AdminNotifications.route,
            enterTransition = { slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)) }
        ) {
            AdminNotificationsScreen(onBack = { navController.popBackStack() })
        }
        // endregion
    }
}