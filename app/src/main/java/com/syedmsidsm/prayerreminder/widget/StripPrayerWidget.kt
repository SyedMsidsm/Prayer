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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.syedmsidsm.prayerreminder.ui.MainActivity

class StripPrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = WidgetStateBuilder.build(context)

        provideContent {
            StripPrayerWidgetContent(state)
        }
    }
}

class StripPrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StripPrayerWidget()
}

@Composable
private fun StripPrayerWidgetContent(state: WidgetState) {
    val ivoryColor = ColorProvider(Color(0xFFF7F4EC))
    val mutedColor = ColorProvider(Color(0xFFC8D2CF))
    val pastColor = ColorProvider(Color(0xFF7B9390))
    val darkTextOnGold = ColorProvider(Color(0xFF0F2A2E))
    val groundColor = Color(0xFF0F2A2E)
    val goldCapsuleBg = Color(0xFFD9AE5C)
    val launchAction = actionStartActivity<MainActivity>()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(22.dp)
            .background(groundColor)
            .clickable(launchAction)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize().clickable(launchAction)) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${state.place}",
                    style = TextStyle(
                        color = mutedColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = state.hijriDate,
                    style = TextStyle(
                        color = mutedColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            val orderedRows = if (state.isRtl) state.rows.reversed() else state.rows
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight().clickable(launchAction),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (row in orderedRows) {
                    val colColor = when {
                        row.isNext -> darkTextOnGold
                        row.isPast -> pastColor
                        else -> ivoryColor
                    }
                    val subColor = when {
                        row.isNext -> darkTextOnGold
                        else -> mutedColor
                    }

                    val colModifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp)
                        .then(
                            if (row.isNext) {
                                GlanceModifier
                                    .cornerRadius(12.dp)
                                    .background(goldCapsuleBg)
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            } else {
                                GlanceModifier.padding(vertical = 4.dp, horizontal = 2.dp)
                            }
                        )

                    Column(
                        modifier = colModifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.prayerName,
                            style = TextStyle(
                                color = colColor,
                                fontSize = 11.sp,
                                fontWeight = if (row.isNext) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = row.azanShort,
                            style = TextStyle(
                                color = colColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                        if (row.isNext && state.countdownText.isNotEmpty()) {
                            Text(
                                text = state.countdownText,
                                style = TextStyle(
                                    color = subColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
