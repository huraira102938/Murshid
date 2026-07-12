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

    // region ADMIN — remove before Play Store release
    data object AdminHome : Screen("admin_home")
    data object AdminWallpapers : Screen("admin_wallpapers")
    data object AdminCategories : Screen("admin_categories")
    data object AdminLibrary : Screen("admin_library")
    data object AdminUpdates : Screen("admin_updates")
    data object AdminNotifications : Screen("admin_notifications")
    // endregion
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val filledIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val outlinedIcon: androidx.compose.ui.graphics.vector.ImageVector
)