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
    val mosqueBackdropRes: Int,
    val pillDotRes: Int,
    val pillButtonRes: Int,
    val chipBgRes: Int,
    val chipTextColorInt: Int,
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
                mosqueBackdropRes = R.drawable.widget_mosque_silhouette,
                pillDotRes = R.drawable.widget_pill_dot_cream,
                pillButtonRes = R.drawable.widget_pill_button_cream,
                chipBgRes = R.drawable.widget_chip_bg_cream,
                chipTextColorInt = 0xFF2B2108.toInt(),
                activeCapsuleRes = R.drawable.widget_capsule_active_cream,
                inactiveCapsuleRes = R.drawable.widget_capsule_inactive_cream,
                textPrimary = ColorProvider(Color(0xFF2B2108)),
                textSecondary = ColorProvider(Color(0xFF6E5F3A)),
                textAccent = ColorProvider(Color(0xFFA35A00)),
                textOnActiveCapsule = ColorProvider(Color(0xFF2B2108)),
                chronometerTextColorInt = 0xFFA35A00.toInt()
            )
            "warm_gold" -> WidgetColors(
                cardBackgroundRes = R.drawable.widget_bg_warm_gold,
                pillBackgroundRes = R.drawable.widget_pill_bg_warm_gold,
                mosqueBackdropRes = R.drawable.widget_mosque_silhouette,
                pillDotRes = R.drawable.widget_pill_dot_gold,
                pillButtonRes = R.drawable.widget_pill_button_gold,
                chipBgRes = R.drawable.widget_chip_bg_gold,
                chipTextColorInt = 0xFFFFD24D.toInt(),
                activeCapsuleRes = R.drawable.widget_capsule_active_gold,
                inactiveCapsuleRes = R.drawable.widget_capsule_inactive_gold,
                textPrimary = ColorProvider(Color(0xFF231700)),
                textSecondary = ColorProvider(Color(0xFF4D3500)),
                textAccent = ColorProvider(Color(0xFF231700)),
                textOnActiveCapsule = ColorProvider(Color(0xFFFFD24D)),
                chronometerTextColorInt = 0xFF231700.toInt()
            )
            else -> WidgetColors( // "dark_teal" – Minimalist Sanctuary Palette from Pill/Square/Main HTML
                cardBackgroundRes = R.drawable.widget_bg_dark_teal,
                pillBackgroundRes = R.drawable.widget_pill_bg_dark_teal,
                mosqueBackdropRes = R.drawable.widget_mosque_silhouette,
                pillDotRes = R.drawable.widget_pill_dot_teal,
                pillButtonRes = R.drawable.widget_pill_button_teal,
                chipBgRes = R.drawable.widget_chip_bg_teal,
                chipTextColorInt = 0xFF1A1200.toInt(),
                activeCapsuleRes = R.drawable.widget_capsule_active,
                inactiveCapsuleRes = R.drawable.widget_capsule_inactive,
                textPrimary = ColorProvider(Color(0xFFF4F8F9)),
                textSecondary = ColorProvider(Color(0xFFA3B7C3)),
                textAccent = ColorProvider(Color(0xFFFFBF1F)),
                textOnActiveCapsule = ColorProvider(Color(0xFF1A1200)),
                chronometerTextColorInt = 0xFFFFBF1F.toInt()
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

    fun buildCountdownChip(
        context: Context,
        targetEpochMillis: Long,
        isRtl: Boolean,
        prefixText: String,
        colors: WidgetColors
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_countdown_chip)
        views.setInt(R.id.widget_chip_container, "setBackgroundResource", colors.chipBgRes)
        views.setTextViewText(R.id.widget_chip_prefix, prefixText)
        views.setTextColor(R.id.widget_chip_prefix, colors.chipTextColorInt)
        views.setTextColor(R.id.widget_chip_chronometer, colors.chipTextColorInt)

        val remainingMs = targetEpochMillis - System.currentTimeMillis()
        if (remainingMs > 0) {
            val base = SystemClock.elapsedRealtime() + remainingMs
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                views.setChronometerCountDown(R.id.widget_chip_chronometer, true)
            }
            views.setChronometer(R.id.widget_chip_chronometer, base, "%s", true)
        } else {
            views.setChronometer(R.id.widget_chip_chronometer, SystemClock.elapsedRealtime(), "00:00", false)
        }
        return views
    }
}
