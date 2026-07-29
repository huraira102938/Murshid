package com.huraira.murshid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.huraira.murshid.ui.theme.MurshidGold

/**
 * Shows [imageModel] (a local content:// Uri or a remote URL — anything Coil can load)
 * full-screen, edge to edge. Used by the admin upload/create sheets (Wallpapers, Library,
 * Updates) so the admin can check how a picked image will actually look before uploading,
 * without needing to publish it first.
 *
 * [simulateWallpaperCrop]: pass true (Wallpapers only) to render with [ContentScale.Crop],
 * matching exactly what [com.huraira.murshid.ui.screens.wallpapers.WallpaperDetailScreen]
 * does — so what the admin sees here is the *actual* crop a real device will show, not
 * just the untouched source image. Library/Updates previews should leave this false,
 * since those are shown uncropped (Fit) elsewhere in the app.
 */
@Composable
fun FullScreenImagePreviewDialog(
    imageModel: Any?,
    onDismiss: () -> Unit,
    simulateWallpaperCrop: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            MurshidAsyncImage(
                model = imageModel,
                contentDescription = null,
                contentScale = if (simulateWallpaperCrop) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            if (simulateWallpaperCrop) {
                Text(
                    text = "Preview as wallpaper — this is how it'll actually look on-screen",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MurshidGold)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close preview", tint = Color.White)
            }
        }
    }
}
