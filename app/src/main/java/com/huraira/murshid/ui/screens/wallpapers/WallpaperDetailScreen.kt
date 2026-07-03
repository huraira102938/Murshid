package com.huraira.murshid.ui.screens.wallpapers

import android.app.WallpaperManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.huraira.murshid.data.model.WallpaperItem
import com.huraira.murshid.ui.components.GoldFilledButton
import com.huraira.murshid.ui.components.GoldOutlinedButton
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.ui.theme.MurshidSurface
import com.huraira.murshid.util.MediaSaver
import com.huraira.murshid.viewmodel.WallpapersViewModel
import kotlinx.coroutines.launch

@Composable
fun WallpaperDetailScreen(
    wallpaperId: String,
    onBack: () -> Unit,
    viewModel: WallpapersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val wallpapers = uiState.wallpapers
    val startIndex = remember(wallpaperId) {
        wallpapers.indexOfFirst { it.id == wallpaperId }.coerceAtLeast(0)
    }

    if (wallpapers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Wallpaper not found.", color = Color.White)
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { wallpapers.size }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            WallpaperPage(wallpaper = wallpapers[page])
        }

        // Back button floats above the pager, always in the top-left
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(4.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun WallpaperPage(wallpaper: WallpaperItem) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showApplyDialog by remember(wallpaper.id) { mutableStateOf(false) }
    var isApplying by remember(wallpaper.id) { mutableStateOf(false) }

    fun applyWallpaper(target: Int) {
        isApplying = true
        scope.launch {
            val bitmap = MediaSaver.getBitmap(context, wallpaper.imageUrl)
            val success = try {
                if (bitmap != null) {
                    WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, target)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
            isApplying = false
            showApplyDialog = false
            Toast.makeText(
                context,
                if (success) "Wallpaper applied" else "Failed to apply wallpaper",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = wallpaper.imageUrl,
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom scrim + title + actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text(
                text = wallpaper.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MurshidGold
            )
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GoldFilledButton(
                    text = if (isApplying) "Applying…" else "Apply Wallpaper",
                    icon = Icons.Filled.Wallpaper,
                    modifier = Modifier.weight(1f),
                    onClick = { showApplyDialog = true }
                )
                GoldOutlinedButton(
                    text = "Download",
                    icon = Icons.Filled.Download,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch {
                            val ok = MediaSaver.saveToGallery(context, wallpaper.imageUrl, wallpaper.title)
                            Toast.makeText(
                                context,
                                if (ok) "Saved to gallery" else "Failed to save",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }

    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { if (!isApplying) showApplyDialog = false },
            containerColor = MurshidSurface,
            title = { Text("Apply wallpaper to", color = Color.White) },
            text = {
                Column {
                    Text(
                        "Choose where to set \"${wallpaper.title}\".",
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    TextButton(onClick = { applyWallpaper(WallpaperManager.FLAG_SYSTEM) }) {
                        Text("Home screen", color = MurshidGold)
                    }
                    TextButton(onClick = { applyWallpaper(WallpaperManager.FLAG_LOCK) }) {
                        Text("Lock screen", color = MurshidGold)
                    }
                    TextButton(onClick = {
                        applyWallpaper(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    }) {
                        Text("Both", color = MurshidGold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { if (!isApplying) showApplyDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}