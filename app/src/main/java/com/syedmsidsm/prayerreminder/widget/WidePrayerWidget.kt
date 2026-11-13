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

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(colors.background)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
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
                    "ar" -> "استورد جدول المسجد"
                    "ur" -> "مسجد کا نظام الاوقات شامل کریں"
                    else -> "Import masjid timetable"
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
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) {
                // Header: Place on one side, Next prayer info on the other
                val nextTag = when (state.appLanguage) {
                    "ar" -> "التالي:"
                    "ur" -> "اگلی نماز:"
                    else -> "Next:"
                }

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 ${state.place}",
                        style = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$nextTag ${state.prayerName} ",
                            style = TextStyle(
                                color = colors.textAccent,
                                fontSize = 11.sp,
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
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // 5-Column Prayer Strip for the 5 Fard Prayers
                val fardRows = state.rows.filter { it.prayer != Prayer.SUNRISE }
                val orderedRows = if (state.isRtl) fardRows.reversed() else fardRows

                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (row in orderedRows) {
                        val isNext = row.isNext
                        val capsuleBg = if (isNext) Color(0xFFFBBF24) else colors.capsuleBackground
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
                            Spacer(modifier = GlanceModifier.height(3.dp))
                            Text(
                                text = row.azanShort.ifEmpty { row.azanFormatted.take(5) },
                                style = TextStyle(
                                    color = timeColor,
                                    fontSize = 13.sp,
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
