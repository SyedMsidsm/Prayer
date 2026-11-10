package com.syedmsidsm.prayerreminder.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.os.ConfigurationCompat
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F2A2E),
    onPrimary = Color(0xFFF7F4EC),
    secondary = Color(0xFFD9AE5C),
    onSecondary = Color(0xFF0F2A2E),
    background = Color(0xFFF7F4EC),
    onBackground = Color(0xFF14262A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14262A),
    surfaceVariant = Color(0xFFF5ECD7),
    onSurfaceVariant = Color(0xFF14262A),
    outline = Color(0xFFEDE8DC)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD9AE5C),
    onPrimary = Color(0xFF0F2A2E),
    secondary = Color(0xFFD9AE5C),
    onSecondary = Color(0xFF0F2A2E),
    background = Color(0xFF091518),
    onBackground = Color(0xFFF3EFE6),
    surface = Color(0xFF13282D),
    onSurface = Color(0xFFF3EFE6),
    surfaceVariant = Color(0xFF1D3B40),
    onSurfaceVariant = Color(0xFFF3EFE6),
    outline = Color(0xFF213B40)
)

@Composable
fun PrayerReminderTheme(
    themeMode: String = "system",
    fontScale: String = "normal",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }
    PrayerReminderColors.isDark = isDark

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val bgColor = if (isDark) Color(0xFF091518).toArgb() else Color(0xFFF7F4EC).toArgb()
            @Suppress("DEPRECATION")
            window.statusBarColor = bgColor
            @Suppress("DEPRECATION")
            window.navigationBarColor = bgColor
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration).get(0)
    val languageCode = currentLocale?.language ?: "en"

    val scaleMultiplier = when (fontScale) {
        "small" -> 0.88f
        "large" -> 1.15f
        "xl" -> 1.28f
        else -> 1.0f
    }
    val currentDensity = LocalDensity.current
    val scaledDensity = remember(currentDensity, scaleMultiplier) {
        Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * scaleMultiplier
        )
    }

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            typography = getAppTypography(languageCode),
            content = content
        )
    }
}
