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
import androidx.glance.unit.ColorProvider
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.ui.MainActivity

class WidePrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = WidgetStateBuilder.build(context)

        provideContent {
            WidePrayerWidgetContent(context, state)
        }
    }
}

class WidePrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WidePrayerWidget()
}

@Composable
private fun WidePrayerWidgetContent(context: Context, state: WidgetState) {
    val colors = WidgetThemeConfig.getColors(state.theme)
    val darkTextOnGold = ColorProvider(Color(0xFF020617))
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                val importMsg = when (state.appLanguage) {
                    "ar" -> "استورد جدول المسجد"
                    "ur" -> "ٹائم ٹیبل شامل کریں"
                    else -> "Tap to import mosque timetable"
                }
                Text(
                    text = importMsg,
                    style = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 13.sp,
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
                // 1. Top Bar: Location & Hijri Date (Left) + Countdown Pill (Right)
                val topInfoText = if (state.hijriDate.isNotEmpty()) {
                    "${state.place} · ${state.hijriDate}"
                } else {
                    state.place
                }

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = topInfoText,
                        style = TextStyle(
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Countdown Pill
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
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱ ",
                                style = TextStyle(
                                    color = colors.textAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            AndroidRemoteViews(
                                remoteViews = chronometerViews,
                                modifier = GlanceModifier.height(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // 2. Hero Middle Row: Next Prayer Name + Big Time (Left) & Jamaat Time (Right)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = state.prayerName,
                            style = TextStyle(
                                color = colors.textAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(1.dp))
                        Text(
                            text = state.mainTime,
                            style = TextStyle(
                                color = colors.textPrimary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    if (state.secondaryTime.isNotEmpty()) {
                        Text(
                            text = state.secondaryTime,
                            style = TextStyle(
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // 3. Bottom Row: Full 5 Fard Prayer Strip with Elevated Active Capsule
                val fardRows = state.rows.filter { it.prayer != Prayer.SUNRISE }
                val orderedRows = if (state.isRtl) fardRows.reversed() else fardRows

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (row in orderedRows) {
                        val isNext = row.isNext
                        val capsuleBg = if (isNext) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.08f)
                        val titleColor = if (isNext) darkTextOnGold else colors.textSecondary
                        val timeColor = if (isNext) darkTextOnGold else colors.textPrimary

                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(capsuleBg)
                                .cornerRadius(12.dp)
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = row.prayerName,
                                style = TextStyle(
                                    color = titleColor,
                                    fontSize = 10.sp,
                                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = row.azanShort.ifEmpty { row.azanFormatted.take(5) },
                                style = TextStyle(
                                    color = timeColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                        if (row != orderedRows.last()) {
                            Spacer(modifier = GlanceModifier.width(5.dp))
                        }
                    }
                }
            }
        }
    }
}
