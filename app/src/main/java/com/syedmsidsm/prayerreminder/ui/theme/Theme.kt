package com.syedmsidsm.prayerreminder.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat

private val LightColorScheme = lightColorScheme(
    primary = PrayerReminderColors.Ground,
    onPrimary = PrayerReminderColors.Ivory,
    secondary = PrayerReminderColors.Gold,
    onSecondary = PrayerReminderColors.Ground,
    background = PrayerReminderColors.Ivory,
    onBackground = PrayerReminderColors.Ink,
    surface = PrayerReminderColors.Surface,
    onSurface = PrayerReminderColors.Ink,
    surfaceVariant = PrayerReminderColors.NextRowBg,
    onSurfaceVariant = PrayerReminderColors.Ink,
    outline = PrayerReminderColors.Line
)

@Composable
fun PrayerReminderTheme(
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration).get(0)
    val languageCode = currentLocale?.language ?: "en"

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = getAppTypography(languageCode),
        content = content
    )
}
