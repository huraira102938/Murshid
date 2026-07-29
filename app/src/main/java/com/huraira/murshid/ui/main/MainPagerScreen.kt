package com.huraira.murshid.ui.main


import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.huraira.murshid.navigation.BottomNavItem
import com.huraira.murshid.navigation.Screen
import com.huraira.murshid.ui.screens.library.LibraryScreen
import com.huraira.murshid.ui.screens.updates.UpdatesScreen
import com.huraira.murshid.ui.screens.wallpapers.WallpapersScreen
import com.huraira.murshid.ui.theme.MurshidBlack
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.ui.theme.MurshidMutedGray
import kotlinx.coroutines.launch

private val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Wallpapers,
        label = "Wallpapers",
        filledIcon = Icons.Filled.Wallpaper,
        outlinedIcon = Icons.Outlined.Wallpaper
    ),
    BottomNavItem(
        screen = Screen.Library,
        label = "Library",
        filledIcon = Icons.Filled.AutoStories,
        outlinedIcon = Icons.Outlined.AutoStories
    ),
    BottomNavItem(
        screen = Screen.Updates,
        label = "Updates",
        filledIcon = Icons.Filled.NewReleases,
        outlinedIcon = Icons.Outlined.NewReleases
    )
)

@Composable
fun MainPagerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })

    fun shareApp() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Murshid — bold, minimal inspiration for leadership and resilience.")
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Murshid"))
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MurshidBlack) {
                bottomNavItems.forEachIndexed { index, item ->
                    val selected = pagerState.currentPage == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            scope.launch { pagerState.scrollToPage(index) }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.filledIcon else item.outlinedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MurshidGold,
                            selectedTextColor = MurshidGold,
                            unselectedIconColor = MurshidMutedGray,
                            unselectedTextColor = MurshidMutedGray,
                            indicatorColor = MurshidBlack
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) { page ->
            when (page) {
                0 -> WallpapersScreen(
                    onWallpaperClick = { id ->
                        navController.navigate(Screen.WallpaperDetail.createRoute(id))
                    },
                    onShare = { shareApp() },
                    onAbout = { navController.navigate(Screen.About.route) },
                    // region ADMIN — remove before Play Store release
                    onAdmin = { navController.navigate(Screen.AdminHome.route) }
                    // endregion
                )
                1 -> LibraryScreen(
                    onShare = { shareApp() },
                    onAbout = { navController.navigate(Screen.About.route) },
                    // region ADMIN — remove before Play Store release
                    onAdmin = { navController.navigate(Screen.AdminHome.route) }
                    // endregion
                )
                2 -> UpdatesScreen(
                    onUpdateClick = { id ->
                        navController.navigate(Screen.UpdateDetail.createRoute(id))
                    },
                    onShare = { shareApp() },
                    onAbout = { navController.navigate(Screen.About.route) },
                    // region ADMIN — remove before Play Store release
                    onAdmin = { navController.navigate(Screen.AdminHome.route) }
                    // endregion
                )
            }
        }
    }
}