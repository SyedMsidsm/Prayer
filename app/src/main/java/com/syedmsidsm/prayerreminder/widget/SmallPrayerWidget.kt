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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.ui.MainActivity
import java.util.Locale

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
    val goldColor = ColorProvider(Color(0xFFD9AE5C))
    val ivoryColor = ColorProvider(Color(0xFFF7F4EC))
    val mutedColor = ColorProvider(Color(0xFFC8D2CF))
    val darkTextOnGold = ColorProvider(Color(0xFF0F2A2E))
    val groundColor = Color(0xFF0F2A2E)
    val goldCapsuleBg = Color(0xFFD9AE5C)
    val launchAction = actionStartActivity<MainActivity>()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(groundColor)
            .clickable(launchAction)
            .padding(12.dp)
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
                Text(
                    text = "Open app",
                    style = TextStyle(
                        color = ivoryColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            val ringBitmap = buildProgressRingBitmap(state.progressFraction)
            Column(
                modifier = GlanceModifier.fillMaxSize().clickable(launchAction),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top-left Place header
                Text(
                    text = "📍 ${state.place}",
                    modifier = GlanceModifier.fillMaxWidth(),
                    style = TextStyle(
                        color = ivoryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = if (state.isRtl) TextAlign.End else TextAlign.Start
                    )
                )

                // Center Progress Ring + Next Prayer Time
                Box(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth().clickable(launchAction),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(ringBitmap),
                        contentDescription = null,
                        modifier = GlanceModifier.size(124.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .cornerRadius(10.dp)
                                .background(goldCapsuleBg)
                                .padding(horizontal = 8.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = state.prayerName.uppercase(Locale.ROOT),
                                style = TextStyle(
                                    color = darkTextOnGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        Text(
                            text = state.mainTime,
                            style = TextStyle(
                                color = ivoryColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )

                        val chronometerViews = buildChronometerRemoteViews(
                            context,
                            state.countdownTargetEpochMillis,
                            state.isRtl
                        )
                        AndroidRemoteViews(remoteViews = chronometerViews)
                    }
                }

                // Bottom Jamaat line
                Text(
                    text = state.secondaryTime,
                    style = TextStyle(
                        color = mutedColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

private fun buildProgressRingBitmap(progressFraction: Float): Bitmap {
    val size = 320
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val stroke = 20f
    val pad = stroke + 6f
    val oval = RectF(pad, pad, size - pad, size - pad)

    val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#254548")
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawArc(oval, 0f, 360f, false, trackPaint)

    val sweepAngle = 360f * progressFraction.coerceIn(0.15f, 0.92f)
    val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#D9AE5C")
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawArc(oval, -90f, sweepAngle, false, arcPaint)
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
