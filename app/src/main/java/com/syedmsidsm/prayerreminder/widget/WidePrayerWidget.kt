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
    val launchAction = actionStartActivity<MainActivity>()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
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
                modifier = GlanceModifier.fillMaxWidth().height(100.dp)
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
                        color = colors.textPrimary,
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
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Top Row: Subtitle + Next Prayer Title (Left) & STARTS IN + Countdown (Right)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val placeTag = state.place.split(',').first().trim()
                    val nextSub = when (state.appLanguage) {
                        "ar" -> "${placeTag.uppercase()} · الصلاة القادمة"
                        "ur" -> "${placeTag.uppercase()} · اگلی نماز"
                        else -> "${placeTag.uppercase()} · NEXT PRAYER"
                    }

                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = nextSub,
                            style = TextStyle(
                                color = colors.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${state.prayerName} ",
                                style = TextStyle(
                                    color = colors.textPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = state.mainTime,
                                style = TextStyle(
                                    color = colors.textAccent,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    val startsInLabel = when (state.appLanguage) {
                        "ar" -> "يبدأ خلال"
                        "ur" -> "باقی وقت"
                        else -> "STARTS IN"
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = startsInLabel,
                            style = TextStyle(
                                color = colors.textSecondary,
                                fontSize = 10.sp,
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
                            modifier = GlanceModifier.height(20.dp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Bottom Row: 5 Fard Prayer Cards with clean themed active/inactive capsules
                val fardRows = state.rows.filter { it.prayer != Prayer.SUNRISE }
                val orderedRows = if (state.isRtl) fardRows.reversed() else fardRows

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
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
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                        Spacer(modifier = GlanceModifier.width(4.dp))
                    }
                }
            }
        }
    }
}
