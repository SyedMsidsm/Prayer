package com.syedmsidsm.prayerreminder.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
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
            .cornerRadius(40.dp)
            .clickable(launchAction)
    ) {
        // 1. Pill Background Frame
        Image(
            provider = ImageProvider(colors.pillBackgroundRes),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = GlanceModifier.fillMaxSize()
        )

        if (!state.hasTable) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
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
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(start = 18.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Glowing Dot with Halo (22dp)
                Image(
                    provider = ImageProvider(colors.pillDotRes),
                    contentDescription = null,
                    modifier = GlanceModifier.size(22.dp)
                )

                Spacer(modifier = GlanceModifier.width(12.dp))

                // Middle Left: Subtitle (Prayer · Place) & Big Time
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val placeTag = state.place.split(',').first().trim()
                    val subtitle = "${state.prayerName.uppercase()} · ${placeTag.uppercase()}"
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = state.mainTime,
                        style = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Middle Right: Next In + Live Countdown
                val nextInLabel = when (state.appLanguage) {
                    "ar" -> "خلال"
                    "ur" -> "باقی وقت"
                    else -> "NEXT IN"
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nextInLabel,
                        style = TextStyle(
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    val chronometerViews = WidgetThemeConfig.buildChronometer(
                        context = context,
                        targetEpochMillis = state.countdownTargetEpochMillis,
                        isRtl = state.isRtl,
                        textColorInt = colors.chronometerTextColorInt
                    )
                    AndroidRemoteViews(
                        remoteViews = chronometerViews,
                        modifier = GlanceModifier.height(18.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.width(10.dp))

                // Right: 44dp Circle Button with Crescent Moon
                Image(
                    provider = ImageProvider(colors.pillButtonRes),
                    contentDescription = null,
                    modifier = GlanceModifier.size(44.dp)
                )
            }
        }
    }
}
