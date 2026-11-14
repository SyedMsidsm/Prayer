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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
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
    val colors = WidgetThemeConfig.getColors(state.theme)
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
            .cornerRadius(32.dp)
            .clickable(launchAction)
    ) {
        // 1. Theme Card Frame Background
        Image(
            provider = ImageProvider(colors.cardBackgroundRes),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = GlanceModifier.fillMaxSize()
        )

        // 2. Subtle Mosque Architectural Silhouette Backdrop at Bottom
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                provider = ImageProvider(colors.mosqueBackdropRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = GlanceModifier.fillMaxWidth().height(80.dp)
            )
        }

        if (!state.hasTable) {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.place,
                    style = TextStyle(
                        color = colors.textAccent,
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
                        color = colors.textPrimary,
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
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Top Row: NEXT · PRAYER (Left) & Arabic Calligraphy (Right)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val nextLabel = when (state.appLanguage) {
                        "ar" -> "التالي · ${state.prayerName}"
                        "ur" -> "اگلی · ${state.prayerName}"
                        else -> "NEXT · ${state.prayerName.uppercase()}"
                    }
                    Text(
                        text = nextLabel,
                        style = TextStyle(
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Text(
                        text = arabicName,
                        style = TextStyle(
                            color = colors.textAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Center: Big Bold Time + Themed Countdown Pill Chip
                Column(
                    modifier = GlanceModifier.fillMaxWidth().clickable(launchAction),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.mainTime,
                        style = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))

                    val inPrefix = when (state.appLanguage) {
                        "ar" -> "خلال "
                        "ur" -> "باقی "
                        else -> "in "
                    }

                    val chipViews = WidgetThemeConfig.buildCountdownChip(
                        context = context,
                        targetEpochMillis = state.countdownTargetEpochMillis,
                        isRtl = state.isRtl,
                        prefixText = inPrefix,
                        colors = colors
                    )

                    AndroidRemoteViews(
                        remoteViews = chipViews,
                        modifier = GlanceModifier.height(26.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Bottom Row: Jamaat / Sunrise (Left) & Place (Right)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val secondaryText = if (state.secondaryTime.isNotBlank()) {
                        "Jamaat ${state.secondaryTime}"
                    } else {
                        state.sunriseFormatted
                    }

                    Text(
                        text = secondaryText,
                        style = TextStyle(
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    val townName = state.place.split(',').first().trim()
                    Text(
                        text = townName,
                        style = TextStyle(
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
