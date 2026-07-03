package com.huraira.murshid.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Murshid is deliberately AMOLED-black and cinematic at all times, so we
// intentionally do not offer a light scheme — the brand identity is dark.
private val MurshidDarkColorScheme = darkColorScheme(
    primary = MurshidGold,
    onPrimary = MurshidBlack,
    primaryContainer = MurshidGoldDim,
    onPrimaryContainer = MurshidWhite,
    secondary = MurshidGoldBright,
    onSecondary = MurshidBlack,
    background = MurshidBlack,
    onBackground = MurshidWhite,
    surface = MurshidSurface,
    onSurface = MurshidWhite,
    surfaceVariant = MurshidSurfaceElevated,
    onSurfaceVariant = MurshidLightGray,
    outline = MurshidCardStroke,
    error = MurshidError,
    onError = MurshidBlack
)

@Composable
fun MurshidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = MurshidDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MurshidBlack.toArgb()
            window.navigationBarColor = MurshidBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}