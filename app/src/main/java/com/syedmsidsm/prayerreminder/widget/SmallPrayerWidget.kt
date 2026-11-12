package com.syedmsidsm.prayerreminder.widget

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.ui.MainActivity

class SmallPrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = WidgetStateBuilder.build(context)

        provideContent {
            SmallPrayerWidgetContent(context, state)
        }
    }
}

class SmallPrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallPrayerWidget()
}

@Composable
private fun SmallPrayerWidgetContent(context: Context, state: WidgetState) {
    val horizontalAlign = if (state.isRtl) Alignment.End else Alignment.Start
    val textAlign = if (state.isRtl) TextAlign.End else TextAlign.Start

    val colors = WidgetThemeConfig.getColors(state.theme)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(colors.background)
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp)
    ) {
        if (!state.hasTable) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = horizontalAlign,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.place,
                    style = TextStyle(
                        color = colors.textAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = textAlign
                    )
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                val importMsg = when (state.appLanguage) {
                    "ar" -> "استورد الجدول الجديد"
                    "ur" -> "نیا نظام الاوقات شامل کریں"
                    else -> "Import the new timetable"
                }
                Text(
                    text = importMsg,
                    style = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = textAlign
                    )
                )
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = horizontalAlign
            ) {
                // Place name
                Text(
                    text = state.place,
                    style = TextStyle(
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = textAlign
                    )
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Title (Prayer name or Prayer · Jamaat)
                val title = if (state.phase == WidgetPrayerPhase.BEFORE_AZAN) {
                    state.prayerName
                } else {
                    when (state.appLanguage) {
                        "ar" -> "${state.prayerName} · الإقامة"
                        "ur" -> "${state.prayerName} · جماعت"
                        else -> "${state.prayerName} · Jamaat"
                    }
                }
                Text(
                    text = title,
                    style = TextStyle(
                        color = colors.textAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = textAlign
                    )
                )

                // Big Time
                Text(
                    text = state.mainTime,
                    style = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = textAlign
                    )
                )

                // Subtitle
                Text(
                    text = state.secondaryTime,
                    style = TextStyle(
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = textAlign
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Countdown Capsule Row
                Box(
                    modifier = GlanceModifier
                        .background(colors.capsuleBackground)
                        .cornerRadius(12.dp)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val chronometerViews = WidgetThemeConfig.buildChronometer(
                        context,
                        state.countdownTargetEpochMillis,
                        state.isRtl,
                        colors.chronometerTextColorInt
                    )
                    AndroidRemoteViews(remoteViews = chronometerViews)
                }
            }
        }
    }
}
