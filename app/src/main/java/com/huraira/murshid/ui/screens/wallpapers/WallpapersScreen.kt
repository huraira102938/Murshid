package com.huraira.murshid.ui.screens.wallpapers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huraira.murshid.ui.components.MurshidTopBar
import com.huraira.murshid.ui.components.WallpaperThumbnail
import com.huraira.murshid.viewmodel.WallpapersViewModel

@Composable
fun WallpapersScreen(
    onWallpaperClick: (String) -> Unit,
    onShare: () -> Unit,
    onAbout: () -> Unit,
    viewModel: WallpapersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MurshidTopBar(
                title = "Wallpapers",
                onShare = onShare,
                onAbout = onAbout
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.wallpapers, key = { it.id }) { wallpaper ->
                WallpaperThumbnail(
                    wallpaper = wallpaper,
                    onClick = { onWallpaperClick(wallpaper.id) }
                )
            }
        }
    }
}