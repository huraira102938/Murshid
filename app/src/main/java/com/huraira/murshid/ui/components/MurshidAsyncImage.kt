package com.huraira.murshid.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.huraira.murshid.ui.theme.MurshidSurface

/**
 * Thin wrapper around Coil's AsyncImage that always supplies a placeholder + error
 * fallback painter, so a failed or in-flight R2 fetch never leaves a blank tile or
 * hangs the screen (see Prompt 2 non-negotiables).
 */
@Composable
fun MurshidAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fallback = ColorPainter(MurshidSurface)
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        placeholder = fallback,
        error = fallback,
        modifier = modifier
    )
}
