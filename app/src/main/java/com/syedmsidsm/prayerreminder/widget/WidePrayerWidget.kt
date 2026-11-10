package com.syedmsidsm.prayerreminder.widget

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
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
import androidx.glance.layout.fillMaxHeight
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
    val goldColor = ColorProvider(Color(0xFFD9AE5C))
    val ivoryColor = ColorProvider(Color(0xFFF7F4EC))
    val mutedColor = ColorProvider(Color(0xFFC8D2CF))
    val pastColor = ColorProvider(Color(0xFF9FB0AD))
    val groundColor = Color(0xFF0F2A2E)
    val nextCapsuleBg = Color(0xFF23403F)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(groundColor)
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
                        color = goldColor,
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
                        color = ivoryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            if (state.isRtl) {
                // RTL layout: Table on Left, Summary Card on Right
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    // Right-to-Left: Table column
                    Box(modifier = GlanceModifier.width(185.dp).fillMaxHeight()) {
                        TableColumn(
                            state = state,
                            goldColor = goldColor,
                            ivoryColor = ivoryColor,
                            mutedColor = mutedColor,
                            pastColor = pastColor,
                            nextCapsuleBg = nextCapsuleBg
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    // Summary card on Right
                    Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                        SummaryColumn(
                            context = context,
                            state = state,
                            goldColor = goldColor,
                            ivoryColor = ivoryColor,
                            mutedColor = mutedColor
                        )
                    }
                }
            } else {
                // LTR layout: Summary Card on Left, Table on Right
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                        SummaryColumn(
                            context = context,
                            state = state,
                            goldColor = goldColor,
                            ivoryColor = ivoryColor,
                            mutedColor = mutedColor
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    Box(modifier = GlanceModifier.width(185.dp).fillMaxHeight()) {
                        TableColumn(
                            state = state,
                            goldColor = goldColor,
                            ivoryColor = ivoryColor,
                            mutedColor = mutedColor,
                            pastColor = pastColor,
                            nextCapsuleBg = nextCapsuleBg
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryColumn(
    context: Context,
    state: WidgetState,
    goldColor: ColorProvider,
    ivoryColor: ColorProvider,
    mutedColor: ColorProvider
) {
    val textAlign = if (state.isRtl) TextAlign.End else TextAlign.Start
    val horizontalAlign = if (state.isRtl) Alignment.End else Alignment.Start

    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = horizontalAlign
    ) {
        // Place row with icon
        val placeText = if (state.isRtl) "${state.place} ⌂" else "⌂ ${state.place}"
        Text(
            text = placeText,
            style = TextStyle(
                color = mutedColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = textAlign
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Main Header (Next · Prayer)
        Text(
            text = state.mainHeader,
            style = TextStyle(
                color = goldColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = textAlign
            )
        )

        // Big Time
        Text(
            text = state.mainTime,
            style = TextStyle(
                color = ivoryColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = textAlign
            )
        )

        // Subtitle (Jamaat time or Azan ✓ time)
        Text(
            text = state.secondaryTime,
            style = TextStyle(
                color = mutedColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = textAlign
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Countdown Chronometer
        val chronometerViews = buildChronometerRemoteViews(
            context,
            state.countdownTargetEpochMillis,
            state.isRtl
        )
        AndroidRemoteViews(remoteViews = chronometerViews)

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Hijri date
        Text(
            text = state.hijriDate,
            style = TextStyle(
                color = mutedColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = textAlign
            )
        )
    }
}

@Composable
private fun TableColumn(
    state: WidgetState,
    goldColor: ColorProvider,
    ivoryColor: ColorProvider,
    mutedColor: ColorProvider,
    pastColor: ColorProvider,
    nextCapsuleBg: Color
) {
    val azanLabel = when (state.appLanguage) {
        "ar" -> "الأذان"
        "ur" -> "اذان"
        else -> "Azan"
    }
    val jamaatLabel = when (state.appLanguage) {
        "ar" -> "الإقامة"
        "ur" -> "جماعت"
        else -> "Jamaat"
    }

    Column(modifier = GlanceModifier.fillMaxSize()) {
        // Table Header
        if (state.isRtl) {
            Row(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Text(
                    text = jamaatLabel,
                    modifier = GlanceModifier.width(56.dp),
                    style = TextStyle(color = mutedColor, fontSize = 11.sp, textAlign = TextAlign.Start)
                )
                Text(
                    text = azanLabel,
                    modifier = GlanceModifier.width(56.dp),
                    style = TextStyle(color = mutedColor, fontSize = 11.sp, textAlign = TextAlign.Start)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
            }
        } else {
            Row(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = azanLabel,
                    modifier = GlanceModifier.width(56.dp),
                    style = TextStyle(color = mutedColor, fontSize = 11.sp, textAlign = TextAlign.End)
                )
                Text(
                    text = jamaatLabel,
                    modifier = GlanceModifier.width(56.dp),
                    style = TextStyle(color = mutedColor, fontSize = 11.sp, textAlign = TextAlign.End)
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(2.dp))

        // 5 Prayer Rows
        for (row in state.rows) {
            val textColor = when {
                row.isNext -> goldColor
                row.isPast -> pastColor
                else -> ivoryColor
            }
            val textWeight = if (row.isNext) FontWeight.Bold else FontWeight.Normal

            val rowModifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
                .then(
                    if (row.isNext) {
                        GlanceModifier
                            .cornerRadius(6.dp)
                            .background(nextCapsuleBg)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    } else {
                        GlanceModifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    }
                )

            Row(
                modifier = rowModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isRtl) {
                    Text(
                        text = row.jamaatFormatted,
                        modifier = GlanceModifier.width(56.dp),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.Start
                        )
                    )
                    Text(
                        text = row.azanFormatted,
                        modifier = GlanceModifier.width(56.dp),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.Start
                        )
                    )
                    Text(
                        text = row.prayerName,
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.End
                        )
                    )
                } else {
                    Text(
                        text = row.prayerName,
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.Start
                        )
                    )
                    Text(
                        text = row.azanFormatted,
                        modifier = GlanceModifier.width(56.dp),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.End
                        )
                    )
                    Text(
                        text = row.jamaatFormatted,
                        modifier = GlanceModifier.width(56.dp),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.End
                        )
                    )
                }
            }
        }
    }
}

private fun buildChronometerRemoteViews(context: Context, targetEpochMillis: Long, isRtl: Boolean): RemoteViews {
    val layoutId = if (isRtl) R.layout.widget_chronometer_rtl else R.layout.widget_chronometer
    val views = RemoteViews(context.packageName, layoutId)
    val remainingMs = targetEpochMillis - System.currentTimeMillis()
    if (remainingMs > 0) {
        val base = SystemClock.elapsedRealtime() + remainingMs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            views.setChronometerCountDown(R.id.widget_chronometer, true)
        }
        views.setChronometer(R.id.widget_chronometer, base, "%s", true)
    } else {
        views.setChronometer(R.id.widget_chronometer, SystemClock.elapsedRealtime(), "00:00", false)
    }
    return views
}
