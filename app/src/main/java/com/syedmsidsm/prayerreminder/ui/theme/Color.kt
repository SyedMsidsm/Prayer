package com.syedmsidsm.prayerreminder.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object PrayerReminderColors {
    var isDark by mutableStateOf(false)

    // Fixed brand colors for the hero card / splash
    val Ground = Color(0xFF0F2A2E)
    val Deep = Color(0xFF0F2A2E)
    val CardTextLight = Color(0xFFF7F4EC)
    val Gold = Color(0xFFD9AE5C)
    val OnDarkMuted = Color(0xFFC8D2CF)
    val PastOnDark = Color(0xFF9FB0AD)
    val NextRowBgDark = Color(0xFF23403F)
    val TagBg = Color(0xFFEBD9AE)
    val TagText = Color(0xFF6B4A12)
    val FlagBg = Color(0xFFFCD98A)
    val FlagOutline = Color(0xFFE0A526)

    // Adaptive Light / Dark theme tokens
    val Ivory: Color
        get() = if (isDark) Color(0xFF091518) else Color(0xFFF7F4EC)

    val Surface: Color
        get() = if (isDark) Color(0xFF13282D) else Color(0xFFFFFFFF)

    val Ink: Color
        get() = if (isDark) Color(0xFFF3EFE6) else Color(0xFF14262A)

    val Muted: Color
        get() = if (isDark) Color(0xFF9FB2B0) else Color(0xFF5E6D70)

    val GoldText: Color
        get() = if (isDark) Color(0xFFE5C170) else Color(0xFF8A6420)

    val NextRowBg: Color
        get() = if (isDark) Color(0xFF1D3B40) else Color(0xFFF5ECD7)

    val WarnBg: Color
        get() = if (isDark) Color(0xFF332610) else Color(0xFFFBEFD5)

    val WarnText: Color
        get() = if (isDark) Color(0xFFF5D38E) else Color(0xFF5C3D00)

    val OkBg: Color
        get() = if (isDark) Color(0xFF123324) else Color(0xFFE3F1E8)

    val OkText: Color
        get() = if (isDark) Color(0xFF7BE0A8) else Color(0xFF1F7A4D)

    val Line: Color
        get() = if (isDark) Color(0xFF213B40) else Color(0xFFEDE8DC)

    val PrimaryBtnBg: Color
        get() = if (isDark) Color(0xFFD9AE5C) else Color(0xFF0F2A2E)

    val PrimaryBtnText: Color
        get() = if (isDark) Color(0xFF091518) else Color(0xFFF7F4EC)
}
