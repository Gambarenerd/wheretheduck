package com.whereduck.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DuckYellow,
    onPrimary = OnSurfaceLight,
    primaryContainer = DuckYellowLight,
    onPrimaryContainer = OnSurfaceLight,
    secondary = DuckGreen,
    onSecondary = SurfaceLight,
    secondaryContainer = DuckGreenLight,
    onSecondaryContainer = OnSurfaceLight,
    tertiary = AlertOrange,
    onTertiary = SurfaceLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = AlertRed,
    onError = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = DuckYellowDark,
    onPrimary = OnSurfaceDark,
    primaryContainer = DuckYellow,
    onPrimaryContainer = OnSurfaceLight,
    secondary = DuckGreenDark,
    onSecondary = OnSurfaceDark,
    secondaryContainer = DuckGreen,
    onSecondaryContainer = OnSurfaceLight,
    tertiary = AlertOrange,
    onTertiary = OnSurfaceDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = AlertRed,
    onError = OnSurfaceDark
)

@Composable
fun WhereTheDuckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
