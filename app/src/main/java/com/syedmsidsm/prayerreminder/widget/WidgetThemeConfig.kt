package com.syedmsidsm.prayerreminder.widget

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import com.syedmsidsm.prayerreminder.R

data class WidgetColors(
    val background: Color,
    val textPrimary: ColorProvider,
    val textSecondary: ColorProvider,
    val textAccent: ColorProvider,
    val capsuleBackground: Color,
    val activeCapsuleBg: Color,
    val chronometerTextColorInt: Int,
    val mosqueBgDrawableRes: Int
)

object WidgetThemeConfig {
    fun getColors(theme: String): WidgetColors {
        return when (theme) {
            "light_cream" -> WidgetColors(
                background = Color(0xFFF7F4EB),
                textPrimary = ColorProvider(Color(0xFF14262A)),
                textSecondary = ColorProvider(Color(0xFF727C7C)),
                textAccent = ColorProvider(Color(0xFF1E4E4E)),
                capsuleBackground = Color(0xFFEDEAE1),
                activeCapsuleBg = Color(0xFFE4E4DC),
                chronometerTextColorInt = 0xFF14262A.toInt(),
                mosqueBgDrawableRes = R.drawable.widget_mosque_bg_dark
            )
            "warm_gold" -> WidgetColors(
                background = Color(0xFFD9AF5D),
                textPrimary = ColorProvider(Color(0xFF0A2C2F)),
                textSecondary = ColorProvider(Color(0xFF444A3B)),
                textAccent = ColorProvider(Color(0xFF0A2C2F)),
                capsuleBackground = Color(0xFFCA9F52),
                activeCapsuleBg = Color(0xFFBD9C55),
                chronometerTextColorInt = 0xFF0A2C2F.toInt(),
                mosqueBgDrawableRes = R.drawable.widget_mosque_bg_dark
            )
            else -> WidgetColors( // "dark_teal" default
                background = Color(0xFF0E2A2E),
                textPrimary = ColorProvider(Color(0xFFF7F4EC)),
                textSecondary = ColorProvider(Color(0xFFB7C5C4)),
                textAccent = ColorProvider(Color(0xFFD9AE5C)),
                capsuleBackground = Color(0xFF1B383C),
                activeCapsuleBg = Color(0xFF23403E),
                chronometerTextColorInt = 0xFFD9AE5C.toInt(),
                mosqueBgDrawableRes = R.drawable.widget_mosque_bg
            )
        }
    }

    fun buildChronometer(
        context: Context,
        targetEpochMillis: Long,
        isRtl: Boolean,
        textColorInt: Int
    ): RemoteViews {
        val layoutId = if (isRtl) R.layout.widget_chronometer_rtl else R.layout.widget_chronometer
        val views = RemoteViews(context.packageName, layoutId)
        views.setTextColor(R.id.widget_chronometer, textColorInt)
        views.setViewVisibility(R.id.widget_stopwatch_icon, View.GONE)

        val remainingMs = targetEpochMillis - System.currentTimeMillis()
        if (remainingMs > 0) {
            val base = SystemClock.elapsedRealtime() + remainingMs
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                views.setChronometerCountDown(R.id.widget_chronometer, true)
            }
            views.setChronometer(R.id.widget_chronometer, base, "%s", true)
        } else {
            views.setChronometer(R.id.widget_chronometer, SystemClock.elapsedRealtime(), "00:00", false)
        }
        return views
    }
}
