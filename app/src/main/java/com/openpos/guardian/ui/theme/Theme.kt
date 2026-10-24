package com.openpos.guardian.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.openpos.guardian.domain.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Teal400,
    onPrimary = Navy900,
    primaryContainer = Teal700,
    onPrimaryContainer = Teal200,
    secondary = Slate400,
    onSecondary = Navy900,
    secondaryContainer = Navy700,
    onSecondaryContainer = Slate100,
    tertiary = Emerald500,
    onTertiary = Navy900,
    background = Navy900,
    onBackground = Slate100,
    surface = Navy800,
    onSurface = Slate100,
    surfaceVariant = Navy700,
    onSurfaceVariant = Slate300,
    error = Rose500,
    onError = Color.White,
    outline = Navy600
)

private val LightColorScheme = lightColorScheme(
    primary = Teal700,
    onPrimary = Color.White,
    primaryContainer = Teal200,
    onPrimaryContainer = Navy900,
    secondary = Navy600,
    onSecondary = Color.White,
    secondaryContainer = Slate200,
    onSecondaryContainer = Navy900,
    tertiary = Emerald600,
    onTertiary = Color.White,
    background = Slate50,
    onBackground = Navy900,
    surface = Color.White,
    onSurface = Navy900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Navy700,
    error = Rose600,
    onError = Color.White,
    outline = Slate300
)

@Composable
fun OpenPOSGuardianTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
