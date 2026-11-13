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
import androidx.glance.background
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
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.syedmsidsm.prayerreminder.R
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
    val darkTextOnGold = ColorProvider(Color(0xFF1E1B18))
    val whiteText = ColorProvider(Color.White)
    val softMutedText = ColorProvider(Color(0xFFE2E8F0))
    val goldText = ColorProvider(Color(0xFFFCD34D))
    val launchAction = actionStartActivity<MainActivity>()

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
                        color = whiteText,
                        fontSize = 13.sp,
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
                // Top Bar: 📍 Location (Left) + Next: Prayer Countdown (Right)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = GlanceModifier.clickable(launchAction)
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.widget_pin_red),
                            contentDescription = null,
                            modifier = GlanceModifier.size(13.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            text = state.place,
                            style = TextStyle(
                                color = whiteText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Next: Prayer + Chronometer countdown in warm amber
                    val chronometerViews = WidgetThemeConfig.buildChronometer(
                        context = context,
                        targetEpochMillis = state.countdownTargetEpochMillis,
                        isRtl = state.isRtl,
                        textColorInt = 0xFFFCD34D.toInt()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = GlanceModifier.clickable(launchAction)
                    ) {
                        val nextLabel = when (state.appLanguage) {
                            "ar" -> "التالي: "
                            "ur" -> "اگلی: "
                            else -> "Next: "
                        }
                        Text(
                            text = "$nextLabel${state.prayerName} ",
                            style = TextStyle(
                                color = goldText,
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

                // Middle area lets the glowing central dome, crescent & minarets shine through
                Spacer(modifier = GlanceModifier.defaultWeight())

                // Bottom Row: 5 Fard Prayers with golden active capsule
                val fardRows = state.rows.filter { it.prayer != Prayer.SUNRISE }
                val orderedRows = if (state.isRtl) fardRows.reversed() else fardRows

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (row in orderedRows) {
                        val isNext = row.isNext
                        val capsuleDrawable = if (isNext) R.drawable.widget_capsule_active else R.drawable.widget_capsule_inactive
                        val titleColor = if (isNext) darkTextOnGold else softMutedText
                        val timeColor = if (isNext) darkTextOnGold else whiteText

                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .height(46.dp)
                                .background(ImageProvider(capsuleDrawable))
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .clickable(launchAction),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = row.prayerName,
                                    style = TextStyle(
                                        color = titleColor,
                                        fontSize = 11.sp,
                                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(1.dp))
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
                        }
                        if (row != orderedRows.last()) {
                            Spacer(modifier = GlanceModifier.width(4.dp))
                        }
                    }
                }
            }
        }
    }
}
