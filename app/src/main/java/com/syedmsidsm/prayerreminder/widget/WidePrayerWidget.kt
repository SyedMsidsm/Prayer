package com.syedmsidsm.prayerreminder.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
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
    val pastColor = ColorProvider(Color(0xFF7B9390))
    val groundColor = Color(0xFF0F2A2E)
    val darkTextOnGold = ColorProvider(Color(0xFF0F2A2E))
    val goldCapsuleBg = Color(0xFFD9AE5C)
    val launchAction = actionStartActivity<MainActivity>()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(groundColor)
            .clickable(launchAction)
            .padding(14.dp)
    ) {
        if (!state.hasTable) {
            Column(
                modifier = GlanceModifier.fillMaxSize().clickable(launchAction),
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
            val progressBarBitmap = buildProgressBarBitmap(state.progressFraction)
            if (state.isRtl) {
                Row(modifier = GlanceModifier.fillMaxSize().clickable(launchAction)) {
                    Box(modifier = GlanceModifier.width(188.dp).fillMaxHeight().clickable(launchAction)) {
                        TableColumn(
                            state = state,
                            goldColor = goldColor,
                            ivoryColor = ivoryColor,
                            mutedColor = mutedColor,
                            pastColor = pastColor,
                            darkTextOnGold = darkTextOnGold,
                            goldCapsuleBg = goldCapsuleBg
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight().clickable(launchAction)) {
                        SummaryColumn(
                            context = context,
                            state = state,
                            progressBarBitmap = progressBarBitmap,
                            goldColor = goldColor,
                            ivoryColor = ivoryColor,
                            mutedColor = mutedColor,
                            darkTextOnGold = darkTextOnGold,
                            goldCapsuleBg = goldCapsuleBg
                        )
                    }
                }
            } else {
                Row(modifier = GlanceModifier.fillMaxSize().clickable(launchAction)) {
                    Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight().clickable(launchAction)) {
                        SummaryColumn(
                            context = context,
                            state = state,
                            progressBarBitmap = progressBarBitmap,
                            goldColor = goldColor,
                            ivoryColor = ivoryColor,
                            mutedColor = mutedColor,
                            darkTextOnGold = darkTextOnGold,
                            goldCapsuleBg = goldCapsuleBg
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    Box(modifier = GlanceModifier.width(188.dp).fillMaxHeight().clickable(launchAction)) {
                        TableColumn(
                            state = state,
                            goldColor = goldColor,
                            ivoryColor = ivoryColor,
                            mutedColor = mutedColor,
                            pastColor = pastColor,
                            darkTextOnGold = darkTextOnGold,
                            goldCapsuleBg = goldCapsuleBg
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
    progressBarBitmap: Bitmap,
    goldColor: ColorProvider,
    ivoryColor: ColorProvider,
    mutedColor: ColorProvider,
    darkTextOnGold: ColorProvider,
    goldCapsuleBg: Color
) {
    val textAlign = if (state.isRtl) TextAlign.End else TextAlign.Start
    val horizontalAlign = if (state.isRtl) Alignment.End else Alignment.Start

    Column(
        modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = horizontalAlign
    ) {
        // Place row with pin
        val placeText = if (state.isRtl) "${state.place} 📍" else "📍 ${state.place}"
        Text(
            text = placeText,
            style = TextStyle(
                color = ivoryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = textAlign
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Gold Capsule Pill: NEXT · PRAYER
        Box(
            modifier = GlanceModifier
                .cornerRadius(12.dp)
                .background(goldCapsuleBg)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = state.mainHeader,
                style = TextStyle(
                    color = darkTextOnGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = textAlign
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(2.dp))

        // Big Time
        Text(
            text = state.mainTime,
            style = TextStyle(
                color = ivoryColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = textAlign
            )
        )

        // Subtitle (Jamaat time or Azan ✓ time)
        Text(
            text = state.secondaryTime,
            style = TextStyle(
                color = mutedColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = textAlign
            )
        )

        Spacer(modifier = GlanceModifier.height(3.dp))

        // Countdown Chronometer
        val chronometerViews = buildChronometerRemoteViews(
            context,
            state.countdownTargetEpochMillis,
            state.isRtl
        )
        AndroidRemoteViews(remoteViews = chronometerViews)

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Gold Progress Bar
        Image(
            provider = ImageProvider(progressBarBitmap),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxWidth().height(5.dp)
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Gregorian + Hijri date footer
        val dateFooter = if (state.gregorianDate.isNotEmpty()) {
            "${state.gregorianDate} · ${state.hijriDate}"
        } else {
            state.hijriDate
        }
        Text(
            text = dateFooter,
            style = TextStyle(
                color = mutedColor,
                fontSize = 10.sp,
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
    darkTextOnGold: ColorProvider,
    goldCapsuleBg: Color
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

    Column(modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>())) {
        // Table Header
        if (state.isRtl) {
            Row(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Text(
                    text = jamaatLabel,
                    modifier = GlanceModifier.width(56.dp),
                    style = TextStyle(color = mutedColor, fontSize = 10.sp, textAlign = TextAlign.Start)
                )
                Text(
                    text = azanLabel,
                    modifier = GlanceModifier.width(56.dp),
                    style = TextStyle(color = mutedColor, fontSize = 10.sp, textAlign = TextAlign.Start)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
            }
        } else {
            Row(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = azanLabel,
                    modifier = GlanceModifier.width(56.dp),
                    style = TextStyle(color = mutedColor, fontSize = 10.sp, textAlign = TextAlign.End)
                )
                Text(
                    text = jamaatLabel,
                    modifier = GlanceModifier.width(56.dp),
                    style = TextStyle(color = mutedColor, fontSize = 10.sp, textAlign = TextAlign.End)
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(2.dp))

        // 5 Prayer Rows
        for (row in state.rows) {
            val textColor = when {
                row.isNext -> darkTextOnGold
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
                            .cornerRadius(10.dp)
                            .background(goldCapsuleBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    } else {
                        GlanceModifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                            fontSize = 11.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.Start
                        )
                    )
                    Text(
                        text = row.azanFormatted,
                        modifier = GlanceModifier.width(56.dp),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.Start
                        )
                    )
                    Text(
                        text = row.prayerName,
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 11.sp,
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
                            fontSize = 11.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.Start
                        )
                    )
                    Text(
                        text = row.azanFormatted,
                        modifier = GlanceModifier.width(56.dp),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.End
                        )
                    )
                    Text(
                        text = row.jamaatFormatted,
                        modifier = GlanceModifier.width(56.dp),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = textWeight,
                            textAlign = TextAlign.End
                        )
                    )
                }
            }
        }

        if (state.sunriseFormatted.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "☀ ${state.sunriseFormatted}",
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp),
                style = TextStyle(
                    color = mutedColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = if (state.isRtl) TextAlign.Start else TextAlign.End
                )
            )
        }
    }
}

private fun buildProgressBarBitmap(progressFraction: Float): Bitmap {
    val width = 320
    val height = 12
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val radius = height / 2f

    val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#284649")
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, trackPaint)

    val fillWidth = (width * progressFraction.coerceIn(0.08f, 1f))
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#D9AE5C")
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(RectF(0f, 0f, fillWidth, height.toFloat()), radius, radius, fillPaint)
    return bmp
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
