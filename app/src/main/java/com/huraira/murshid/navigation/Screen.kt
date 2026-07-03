package com.huraira.murshid.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Wallpapers : Screen("wallpapers")
    data object Library : Screen("library")
    data object Updates : Screen("updates")
    data object About : Screen("about")

    data object WallpaperDetail : Screen("wallpaper_detail/{wallpaperId}") {
        fun createRoute(wallpaperId: String) = "wallpaper_detail/$wallpaperId"
        const val ARG_WALLPAPER_ID = "wallpaperId"
    }

    data object UpdateDetail : Screen("update_detail/{updateId}") {
        fun createRoute(updateId: String) = "update_detail/$updateId"
        const val ARG_UPDATE_ID = "updateId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val filledIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val outlinedIcon: androidx.compose.ui.graphics.vector.ImageVector
)