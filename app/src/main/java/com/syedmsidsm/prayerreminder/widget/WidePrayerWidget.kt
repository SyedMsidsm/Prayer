package com.syedmsidsm.prayerreminder.widget

import android.content.Context
import androidx.compose.runtime.Composable
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
import com.syedmsidsm.prayerreminder.R
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

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(colors.background)
            .clickable(actionStartActivity<MainActivity>())
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Top Row: Place & Hijri date on one side, Countdown capsule on the other
                val headerSubtitle = if (state.hijriDate.isNotEmpty()) {
                    "${state.place} · ${state.hijriDate}"
                } else {
                    state.place
                }

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.isRtl) {
                        // Chronometer on Left, Place & Hijri on Right
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
                                true,
                                colors.chronometerTextColorInt
                            )
                            AndroidRemoteViews(remoteViews = chronometerViews)
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        Text(
                            text = headerSubtitle,
                            style = TextStyle(
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.End
                            )
                        )
                    } else {
                        // Place & Hijri on Left, Chronometer on Right
                        Text(
                            text = headerSubtitle,
                            style = TextStyle(
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )

                        Spacer(modifier = GlanceModifier.defaultWeight())

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
                                false,
                                colors.chronometerTextColorInt
                            )
                            AndroidRemoteViews(remoteViews = chronometerViews)
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Middle: Next prayer name & Main time & Jamaat
                val textAlign = if (state.isRtl) TextAlign.End else TextAlign.Start
                val alignHoriz = if (state.isRtl) Alignment.End else Alignment.Start

                Column(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = alignHoriz
                ) {
                    Text(
                        text = state.prayerName,
                        style = TextStyle(
                            color = colors.textAccent,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = textAlign
                        )
                    )

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isRtl) {
                            Text(
                                text = state.secondaryTime,
                                style = TextStyle(
                                    color = colors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.End
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(12.dp))
                            Text(
                                text = state.mainTime,
                                style = TextStyle(
                                    color = colors.textPrimary,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End
                                )
                            )
                        } else {
                            Text(
                                text = state.mainTime,
                                style = TextStyle(
                                    color = colors.textPrimary,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(12.dp))
                            Text(
                                text = state.secondaryTime,
                                style = TextStyle(
                                    color = colors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Bottom Row: Horizontal 5-prayer row
                val displayRows = if (state.isRtl) state.rows.reversed() else state.rows
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in displayRows.indices) {
                        val row = displayRows[i]
                        val isNext = row.isNext
                        val isPast = row.isPast

                        val capsuleModifier = if (isNext) {
                            GlanceModifier
                                .defaultWeight()
                                .background(colors.activeCapsuleBg)
                                .cornerRadius(10.dp)
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        } else {
                            GlanceModifier
                                .defaultWeight()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        }

                        val nameColor = when {
                            isNext -> colors.textAccent
                            isPast -> colors.textSecondary
                            else -> colors.textPrimary
                        }

                        val timeColor = when {
                            isNext -> colors.textPrimary
                            isPast -> colors.textSecondary
                            else -> colors.textPrimary
                        }

                        val fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium

                        Column(
                            modifier = capsuleModifier,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = row.prayerName,
                                style = TextStyle(
                                    color = nameColor,
                                    fontSize = 11.sp,
                                    fontWeight = fontWeight,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            // Format without AM/PM for compact clean appearance
                            val compactTime = row.azanFormatted
                                .replace(" AM", "")
                                .replace(" PM", "")
                                .replace(" ص", "")
                                .replace(" م", "")
                            Text(
                                text = compactTime,
                                style = TextStyle(
                                    color = timeColor,
                                    fontSize = 13.sp,
                                    fontWeight = fontWeight,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
