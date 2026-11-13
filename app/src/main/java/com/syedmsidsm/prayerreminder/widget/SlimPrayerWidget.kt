package com.syedmsidsm.prayerreminder.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.unit.ColorProvider
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
    val whiteText = ColorProvider(Color.White)
    val goldText = ColorProvider(Color(0xFFFCD34D))
    val softGoldText = ColorProvider(Color(0xFFE2D9C8))
    val launchAction = actionStartActivity<MainActivity>()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(32.dp)
            .clickable(launchAction)
    ) {
        // 1. Layered Grand Mosque Background (Cropped Horizontally)
        Image(
            provider = ImageProvider(R.drawable.widget_mosque_sanctuary_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = GlanceModifier.fillMaxSize()
        )

        // 2. Subtle Golden Frame Overlay
        Image(
            provider = ImageProvider(R.drawable.widget_pill_frame),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = GlanceModifier.fillMaxSize()
        )

        if (!state.hasTable) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.place,
                    style = TextStyle(
                        color = goldText,
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
                        color = whiteText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Glowing amber sun/prayer dot + Prayer Name & Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.clickable(launchAction)
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.widget_sun_dot),
                        contentDescription = null,
                        modifier = GlanceModifier.size(10.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "${state.prayerName} ${state.mainTime}",
                        style = TextStyle(
                            color = whiteText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Center Spacer shows the illuminated mosque dome
                Spacer(modifier = GlanceModifier.defaultWeight())

                // Right: "Next in" above countdown chronometer + golden crescent moon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.clickable(launchAction)
                ) {
                    val nextInLabel = when (state.appLanguage) {
                        "ar" -> "خلال"
                        "ur" -> "باقی وقت"
                        else -> "Next in"
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nextInLabel,
                            style = TextStyle(
                                color = softGoldText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(1.dp))

                        val chronometerViews = WidgetThemeConfig.buildChronometer(
                            context = context,
                            targetEpochMillis = state.countdownTargetEpochMillis,
                            isRtl = state.isRtl,
                            textColorInt = 0xFFFCD34D.toInt()
                        )

                        AndroidRemoteViews(
                            remoteViews = chronometerViews,
                            modifier = GlanceModifier.height(18.dp)
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Image(
                        provider = ImageProvider(R.drawable.widget_crescent_gold),
                        contentDescription = null,
                        modifier = GlanceModifier.size(18.dp)
                    )
                }
            }
        }
    }
}
