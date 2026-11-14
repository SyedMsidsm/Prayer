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
    val cardBackgroundRes: Int,
    val pillBackgroundRes: Int,
    val activeCapsuleRes: Int,
    val inactiveCapsuleRes: Int,
    val textPrimary: ColorProvider,
    val textSecondary: ColorProvider,
    val textAccent: ColorProvider,
    val textOnActiveCapsule: ColorProvider,
    val chronometerTextColorInt: Int
)

object WidgetThemeConfig {
    fun getColors(theme: String): WidgetColors {
        return when (theme) {
            "light_cream" -> WidgetColors(
                cardBackgroundRes = R.drawable.widget_bg_light_cream,
                pillBackgroundRes = R.drawable.widget_pill_bg_light_cream,
                activeCapsuleRes = R.drawable.widget_capsule_active_cream,
                inactiveCapsuleRes = R.drawable.widget_capsule_inactive_cream,
                textPrimary = ColorProvider(Color(0xFF0F172A)),
                textSecondary = ColorProvider(Color(0xFF64748B)),
                textAccent = ColorProvider(Color(0xFFB45309)),
                textOnActiveCapsule = ColorProvider(Color(0xFF1E1B18)),
                chronometerTextColorInt = 0xFFB45309.toInt()
            )
            "warm_gold" -> WidgetColors(
                cardBackgroundRes = R.drawable.widget_bg_warm_gold,
                pillBackgroundRes = R.drawable.widget_pill_bg_warm_gold,
                activeCapsuleRes = R.drawable.widget_capsule_active_gold,
                inactiveCapsuleRes = R.drawable.widget_capsule_inactive_gold,
                textPrimary = ColorProvider(Color(0xFFFFFBEB)),
                textSecondary = ColorProvider(Color(0xFFD4C7B0)),
                textAccent = ColorProvider(Color(0xFFFBBF24)),
                textOnActiveCapsule = ColorProvider(Color(0xFF1E1B18)),
                chronometerTextColorInt = 0xFFFBBF24.toInt()
            )
            else -> WidgetColors( // "dark_teal" Sanctuary Dark default (matches Sanctuary design)
                cardBackgroundRes = R.drawable.widget_sanctuary_bg,
                pillBackgroundRes = R.drawable.widget_sanctuary_bg,
                activeCapsuleRes = R.drawable.widget_capsule_active,
                inactiveCapsuleRes = R.drawable.widget_capsule_inactive,
                textPrimary = ColorProvider(Color.White),
                textSecondary = ColorProvider(Color(0xFF94A3B8)),
                textAccent = ColorProvider(Color(0xFFFCD34D)),
                textOnActiveCapsule = ColorProvider(Color(0xFF1E1B18)),
                chronometerTextColorInt = 0xFFFCD34D.toInt()
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
