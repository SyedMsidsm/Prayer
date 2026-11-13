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
    val whiteText = ColorProvider(Color.White)
    val goldText = ColorProvider(Color(0xFFFCD34D))
    val mutedText = ColorProvider(Color(0xFFCBD5E1))
    val launchAction = actionStartActivity<MainActivity>()

    val arabicName = when (state.nextPrayerKey.uppercase()) {
        "FAJR" -> "الفجر"
        "SUNRISE" -> "الشروق"
        "DHUHR" -> "الظهر"
        "ASR" -> "العصر"
        "MAGHRIB" -> "المغرب"
        "ISHA" -> "العشاء"
        else -> "الفجر"
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(26.dp)
            .clickable(launchAction)
    ) {
        // 1. Layered Grand Illuminated Mosque & Starry Night Background
        Image(
            provider = ImageProvider(R.drawable.widget_mosque_sanctuary_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = GlanceModifier.fillMaxSize()
        )

        // 2. Subtle Golden Frame Overlay
        Image(
            provider = ImageProvider(R.drawable.widget_card_frame),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = GlanceModifier.fillMaxSize()
        )

        if (!state.hasTable) {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.place,
                    style = TextStyle(
                        color = goldText,
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
                        color = whiteText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
            }
        } else {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Top Row: Prayer Name (Left) & Arabic Calligraphy (Right)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.prayerName,
                        style = TextStyle(
                            color = whiteText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Text(
                        text = arabicName,
                        style = TextStyle(
                            color = goldText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Center: Big Bold Time + Countdown Chronometer
                Column(
                    modifier = GlanceModifier.fillMaxWidth().clickable(launchAction),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.mainTime,
                        style = TextStyle(
                            color = whiteText,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))

                    val chronometerViews = WidgetThemeConfig.buildChronometer(
                        context = context,
                        targetEpochMillis = state.countdownTargetEpochMillis,
                        isRtl = state.isRtl,
                        textColorInt = 0xFFFCD34D.toInt()
                    )

                    AndroidRemoteViews(
                        remoteViews = chronometerViews,
                        modifier = GlanceModifier.height(20.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Bottom Row: Iqamah Jamaat (Left) & Place (Right)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val secondaryLabel = if (state.secondaryTime.isNotEmpty()) {
                        state.secondaryTime
                    } else {
                        state.place
                    }
                    Text(
                        text = secondaryLabel,
                        style = TextStyle(
                            color = mutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Text(
                        text = state.place,
                        style = TextStyle(
                            color = mutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
