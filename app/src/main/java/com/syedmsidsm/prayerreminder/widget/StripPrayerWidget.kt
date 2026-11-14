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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.syedmsidsm.prayerreminder.domain.model.Prayer
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
    val colors = WidgetThemeConfig.getColors(state.theme)
    val launchAction = actionStartActivity<MainActivity>()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .clickable(launchAction)
    ) {
        // 1. Theme Card Frame Background
        Image(
            provider = ImageProvider(colors.cardBackgroundRes),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = GlanceModifier.fillMaxSize()
        )

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(launchAction)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${state.place}",
                    style = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = state.hijriDate,
                    style = TextStyle(
                        color = colors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            val fardRows = state.rows.filter { it.prayer != Prayer.SUNRISE }
            val orderedRows = if (state.isRtl) fardRows.reversed() else fardRows
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight().clickable(launchAction),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (row in orderedRows) {
                    val isNext = row.isNext
                    val capsuleDrawable = if (isNext) colors.activeCapsuleRes else colors.inactiveCapsuleRes
                    val titleColor = if (isNext) colors.textOnActiveCapsule else colors.textSecondary
                    val timeColor = if (isNext) colors.textOnActiveCapsule else colors.textPrimary

                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .height(42.dp)
                            .background(ImageProvider(capsuleDrawable))
                            .padding(vertical = 3.dp, horizontal = 2.dp),
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
                                    fontSize = 10.sp,
                                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            )
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
                    Spacer(modifier = GlanceModifier.width(3.dp))
                }
            }
        }
    }
}
