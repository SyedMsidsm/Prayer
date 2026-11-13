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
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
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
    val colors = WidgetThemeConfig.getColors(state.theme)
    val launchAction = actionStartActivity<MainActivity>()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(26.dp)
            .background(colors.background)
            .clickable(launchAction)
            .padding(14.dp)
    ) {
        if (!state.hasTable) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.place,
                    style = TextStyle(
                        color = colors.textAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                val importMsg = when (state.appLanguage) {
                    "ar" -> "استورد جدول المسجد"
                    "ur" -> "ٹائم ٹیبل شامل کریں"
                    else -> "Tap to import"
                }
                Text(
                    text = importMsg,
                    style = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) {
                // 1. Top Location Header
                Text(
                    text = state.place,
                    style = TextStyle(
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                // 2. Next Prayer Name (Amber) + Big Bold Adhan Time
                Column {
                    Text(
                        text = state.prayerName,
                        style = TextStyle(
                            color = colors.textAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(1.dp))
                    Text(
                        text = state.mainTime,
                        style = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // 3. Jamaat Time Line
                if (state.secondaryTime.isNotEmpty()) {
                    Text(
                        text = state.secondaryTime,
                        style = TextStyle(
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // 4. Bottom Countdown Chronometer Pill
                val chronometerViews = WidgetThemeConfig.buildChronometer(
                    context = context,
                    targetEpochMillis = state.countdownTargetEpochMillis,
                    isRtl = state.isRtl,
                    textColorInt = colors.chronometerTextColorInt
                )

                Box(
                    modifier = GlanceModifier
                        .background(Color.White.copy(alpha = 0.08f))
                        .cornerRadius(12.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
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
