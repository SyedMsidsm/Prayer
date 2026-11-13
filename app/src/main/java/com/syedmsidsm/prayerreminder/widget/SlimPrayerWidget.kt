package com.syedmsidsm.prayerreminder.widget

import android.content.Context
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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.syedmsidsm.prayerreminder.ui.MainActivity

class SlimPrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = WidgetStateBuilder.build(context)

        provideContent {
            SlimPrayerWidgetContent(context, state)
        }
    }
}

class SlimPrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SlimPrayerWidget()
}

@Composable
private fun SlimPrayerWidgetContent(context: Context, state: WidgetState) {
    val colors = WidgetThemeConfig.getColors(state.theme)
    val launchAction = actionStartActivity<MainActivity>()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(colors.background)
            .clickable(launchAction)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!state.hasTable) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.place,
                    style = TextStyle(
                        color = colors.textAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(10.dp))
                val importMsg = when (state.appLanguage) {
                    "ar" -> "استورد جدول المسجد"
                    "ur" -> "ٹائم ٹیبل شامل کریں"
                    else -> "Import timetable"
                }
                Text(
                    text = importMsg,
                    style = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Next Prayer Name (Amber) + Adhan Time (Bold White)
                Column(
                    modifier = GlanceModifier.clickable(launchAction),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.prayerName.uppercase(),
                        style = TextStyle(
                            color = colors.textAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(1.dp))
                    Text(
                        text = state.mainTime,
                        style = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(20.dp))

                // Column 2: Jamaat Label (Muted) + Jamaat Time (Bold White)
                val jamaatLabel = when (state.appLanguage) {
                    "ar" -> "الجماعة"
                    "ur" -> "جماعت"
                    else -> "JAMAAT"
                }
                val cleanJamaatTime = state.secondaryTime
                    .replace("Jamaat", "")
                    .replace("جماعت", "")
                    .replace("الجماعة", "")
                    .replace("الإقامة", "")
                    .replace("Azan ✓", "")
                    .trim()
                    .ifEmpty { state.secondaryTime.ifEmpty { state.mainTime } }
                Column(
                    modifier = GlanceModifier.clickable(launchAction),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = jamaatLabel,
                        style = TextStyle(
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(1.dp))
                    Text(
                        text = cleanJamaatTime,
                        style = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Column 3: Elevated Pill Capsule with Clock Icon + Countdown Chronometer
                val chronometerViews = WidgetThemeConfig.buildChronometer(
                    context = context,
                    targetEpochMillis = state.countdownTargetEpochMillis,
                    isRtl = state.isRtl,
                    textColorInt = colors.chronometerTextColorInt
                )

                Box(
                    modifier = GlanceModifier
                        .background(Color.White.copy(alpha = 0.08f))
                        .cornerRadius(16.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable(launchAction),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⏱ ",
                            style = TextStyle(
                                color = colors.textAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        AndroidRemoteViews(
                            remoteViews = chronometerViews,
                            modifier = GlanceModifier.height(18.dp)
                        )
                    }
                }
            }
        }
    }
}
