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
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.syedmsidsm.prayerreminder.R
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

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(colors.background)
            .clickable(actionStartActivity<MainActivity>())
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(12.dp))
                val importMsg = when (state.appLanguage) {
                    "ar" -> "استورد الجدول الجديد"
                    "ur" -> "نیا نظام الاوقات شامل کریں"
                    else -> "Import the timetable"
                }
                Text(
                    text = importMsg,
                    style = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            val prayerUpper = state.prayerName.uppercase()
            val jamaatLabel = when (state.appLanguage) {
                "ar" -> "الإقامة"
                "ur" -> "جماعت"
                else -> "JAMAAT"
            }

            if (state.isRtl) {
                // RTL layout
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timer Capsule on Left
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

                    // Jamaat Section
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = jamaatLabel,
                            style = TextStyle(
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        )
                        Text(
                            text = if (state.phase == WidgetPrayerPhase.BEFORE_AZAN) {
                                state.secondaryTime.replace("Jamaat ", "").replace("الإقامة ", "").replace("جماعت ", "")
                            } else {
                                state.mainTime
                            },
                            style = TextStyle(
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(20.dp))

                    // Prayer Name & Azan Time
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = prayerUpper,
                            style = TextStyle(
                                color = colors.textAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        )
                        Text(
                            text = if (state.phase == WidgetPrayerPhase.BEFORE_AZAN) state.mainTime else state.secondaryTime,
                            style = TextStyle(
                                color = colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        )
                    }
                }
            } else {
                // LTR layout
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prayer Name & Azan Time
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = prayerUpper,
                            style = TextStyle(
                                color = colors.textAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (state.phase == WidgetPrayerPhase.BEFORE_AZAN) state.mainTime else state.secondaryTime,
                            style = TextStyle(
                                color = colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(24.dp))

                    // Jamaat Section
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = jamaatLabel,
                            style = TextStyle(
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (state.phase == WidgetPrayerPhase.BEFORE_AZAN) {
                                state.secondaryTime.replace("Jamaat ", "")
                            } else {
                                state.mainTime
                            },
                            style = TextStyle(
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Timer Capsule on Right
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
        }
    }
}
