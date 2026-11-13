package com.syedmsidsm.prayerreminder.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.HijriNames
import com.syedmsidsm.prayerreminder.domain.model.NextPrayerPhase
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.ScheduleSource
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSettingsClick: () -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()
    val languageCode = currentLocale.language ?: "en"

    val amMarker = stringResource(R.string.am_marker)
    val pmMarker = stringResource(R.string.pm_marker)

    val scrollState = rememberScrollState()

    // Mosque silhouette color
    val mosqueColor = if (PrayerReminderColors.isDark) {
        PrayerReminderColors.Gold.copy(alpha = 0.06f)
    } else {
        PrayerReminderColors.Ground.copy(alpha = 0.06f)
    }

    Scaffold(
        containerColor = PrayerReminderColors.Ivory,
        topBar = {
            // Refined top bar — no shadow, subtle bottom border
            Surface(
                color = PrayerReminderColors.Ivory,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .drawBehind {
                        // Subtle bottom border instead of shadow
                        drawLine(
                            color = PrayerReminderColors.Line,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrayerReminderColors.Ground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = PrayerReminderColors.Gold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.place,
                                style = MaterialTheme.typography.titleLarge,
                                color = PrayerReminderColors.Ink,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val table = uiState.activeTimetable
                            val scheduleSource = uiState.schedule?.source
                            if (scheduleSource == ScheduleSource.TABLE && table != null) {
                                val monthName = HijriNames.getMonthName(table.hijriMonth, languageCode)
                                val lastDateStr = runCatching {
                                    val ld = java.time.LocalDate.parse(table.lastDate)
                                    ld.format(DateTimeFormatter.ofPattern("d MMM", currentLocale))
                                }.getOrDefault(table.lastDate)

                                val subtitle = stringResource(
                                    R.string.timetable_line,
                                    "$monthName ${table.hijriYear}",
                                    lastDateStr
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrayerReminderColors.Muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else if (uiState.schedule?.hijri != null) {
                                val h = uiState.schedule!!.hijri!!
                                val monthName = HijriNames.getMonthName(h.month, languageCode)
                                val autoLabel = when (languageCode) {
                                    "ar" -> "$monthName ${h.year} · جدول بهتكل المدمج"
                                    "ur" -> "$monthName ${h.year} · بھٹکل کا بلٹ ان نظام الاوقات"
                                    else -> "$monthName ${h.year} · Built-in Bhatkal"
                                }
                                Text(
                                    text = autoLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrayerReminderColors.Muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrayerReminderColors.Surface,
                            border = BorderStroke(1.dp, PrayerReminderColors.Line),
                            shadowElevation = 1.dp
                        ) {
                            IconButton(
                                onClick = onToggleTheme,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (PrayerReminderColors.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme",
                                    tint = if (PrayerReminderColors.isDark) PrayerReminderColors.Gold else PrayerReminderColors.Ink,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        val btnBg = if (PrayerReminderColors.isDark) PrayerReminderColors.Gold else PrayerReminderColors.Ground
                        val btnFg = if (PrayerReminderColors.isDark) PrayerReminderColors.Ground else PrayerReminderColors.Ivory
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = btnBg,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .height(40.dp)
                                .clickable { onSettingsClick() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.settings),
                                    tint = btnFg,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.settings),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = btnFg
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mosque silhouette background — drawn at the top of the scrollable area
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .align(Alignment.TopCenter)
            ) {
                drawMosqueSilhouette(mosqueColor)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {

            // 2. Warning Banners (if estimated or no table or missed reminders)
            val schedule = uiState.schedule
            if (schedule != null && schedule.source.isEstimated) {
                Spacer(modifier = Modifier.height(12.dp))
                BannerCard(
                    text = stringResource(R.string.banner_estimated),
                    bg = PrayerReminderColors.WarnBg,
                    textColor = PrayerReminderColors.WarnText
                )
            } else if (schedule != null && schedule.source == ScheduleSource.NONE) {
                Spacer(modifier = Modifier.height(12.dp))
                BannerCard(
                    text = stringResource(R.string.banner_no_timetable),
                    bg = PrayerReminderColors.WarnBg,
                    textColor = PrayerReminderColors.WarnText
                )
            }

            if (uiState.missedCount7d > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                BannerCard(
                    text = stringResource(R.string.banner_missed),
                    bg = PrayerReminderColors.WarnBg,
                    textColor = PrayerReminderColors.WarnText
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Date Block (Gregorian + Hijri from table)
            val selectedDate = uiState.selectedDate
            val gregorianDateText = selectedDate.format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM", currentLocale)
            )
            Text(
                text = gregorianDateText,
                style = MaterialTheme.typography.titleLarge,
                color = PrayerReminderColors.Ink,
                fontWeight = FontWeight.SemiBold
            )

            val hijri = schedule?.hijri
            if (hijri != null) {
                val hijriMonthName = HijriNames.getMonthName(hijri.month, languageCode)
                val hijriSuffix = stringResource(R.string.hijri_suffix)
                val hijriText = "${hijri.day} $hijriMonthName ${hijri.year} $hijriSuffix"
                Text(
                    text = hijriText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PrayerReminderColors.GoldText,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Next Prayer Card (Premium design)
            val next = uiState.nextPrayer
            if (next != null) {
                NextPrayerCard(
                    next = next,
                    hasWarning = schedule?.fixedTimeBeforeAzanWarnings?.contains(next.prayer) == true,
                    amMarker = amMarker,
                    pmMarker = pmMarker,
                    voiceSummary = uiState.voiceSummary,
                    voiceEnabled = uiState.voiceEnabled
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Today's Prayers List
            if (schedule != null && schedule.source != ScheduleSource.NONE) {
                TodayPrayersList(
                    schedule = schedule,
                    nextPrayer = next,
                    currentTime = uiState.currentTime,
                    amMarker = amMarker,
                    pmMarker = pmMarker,
                    reminderStates = uiState.reminderStates,
                    onToggleReminder = { viewModel.togglePrayerReminder(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Direct Settings Card at Bottom of Home Page
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSettingsClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface),
                border = BorderStroke(1.dp, PrayerReminderColors.Line)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Subtle gradient overlay for premium feel
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        PrayerReminderColors.Gold.copy(alpha = 0.04f),
                                        Color.Transparent
                                    )
                                )
                            )
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (PrayerReminderColors.isDark) PrayerReminderColors.Gold else PrayerReminderColors.Ground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = if (PrayerReminderColors.isDark) PrayerReminderColors.Ground else PrayerReminderColors.Ivory,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrayerReminderColors.Ink
                        )
                        Text(
                            text = stringResource(R.string.home_settings_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = PrayerReminderColors.Muted
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PrayerReminderColors.GoldText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        }
    }
}

/**
 * Draws a subtle mosque silhouette at the top of the screen.
 * Central dome with crescent, two minarets on each side.
 */
private fun DrawScope.drawMosqueSilhouette(color: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    // Central dome
    val domePath = Path().apply {
        moveTo(cx - w * 0.22f, h)
        // Dome arch
        cubicTo(
            cx - w * 0.22f, h * 0.45f,
            cx - w * 0.08f, h * 0.25f,
            cx, h * 0.22f
        )
        cubicTo(
            cx + w * 0.08f, h * 0.25f,
            cx + w * 0.22f, h * 0.45f,
            cx + w * 0.22f, h
        )
        close()
    }
    drawPath(domePath, color, style = Fill)

    // Crescent finial on top of dome
    val crescentPath = Path().apply {
        // Small crescent at the peak
        val cy = h * 0.17f
        val r = w * 0.018f
        // Outer circle
        moveTo(cx + r, cy)
        cubicTo(cx + r, cy - r * 1.3f, cx - r, cy - r * 1.3f, cx - r, cy)
        cubicTo(cx - r, cy + r * 1.3f, cx + r, cy + r * 1.3f, cx + r, cy)
        // Inner cutout (shifted right for crescent shape)
        moveTo(cx + r * 0.4f, cy)
        cubicTo(cx + r * 0.4f, cy - r * 0.9f, cx - r * 0.6f, cy - r * 0.9f, cx - r * 0.6f, cy)
        cubicTo(cx - r * 0.6f, cy + r * 0.9f, cx + r * 0.4f, cy + r * 0.9f, cx + r * 0.4f, cy)
    }
    drawPath(crescentPath, color, style = Fill)

    // Spire above dome
    drawLine(
        color = color,
        start = Offset(cx, h * 0.22f),
        end = Offset(cx, h * 0.14f),
        strokeWidth = w * 0.005f
    )

    // Left minaret
    val lmX = cx - w * 0.30f
    val minaretWidth = w * 0.028f
    val leftMinaretPath = Path().apply {
        moveTo(lmX - minaretWidth, h)
        lineTo(lmX - minaretWidth, h * 0.40f)
        lineTo(lmX - minaretWidth * 0.6f, h * 0.36f)
        // Minaret cap (pointed)
        lineTo(lmX, h * 0.28f)
        lineTo(lmX + minaretWidth * 0.6f, h * 0.36f)
        lineTo(lmX + minaretWidth, h * 0.40f)
        lineTo(lmX + minaretWidth, h)
        close()
    }
    drawPath(leftMinaretPath, color, style = Fill)

    // Right minaret
    val rmX = cx + w * 0.30f
    val rightMinaretPath = Path().apply {
        moveTo(rmX - minaretWidth, h)
        lineTo(rmX - minaretWidth, h * 0.40f)
        lineTo(rmX - minaretWidth * 0.6f, h * 0.36f)
        lineTo(rmX, h * 0.28f)
        lineTo(rmX + minaretWidth * 0.6f, h * 0.36f)
        lineTo(rmX + minaretWidth, h * 0.40f)
        lineTo(rmX + minaretWidth, h)
        close()
    }
    drawPath(rightMinaretPath, color, style = Fill)

    // Small side domes (left and right of main dome)
    val smallDomeLeft = Path().apply {
        val sdCx = cx - w * 0.16f
        moveTo(sdCx - w * 0.06f, h)
        cubicTo(
            sdCx - w * 0.06f, h * 0.65f,
            sdCx, h * 0.55f,
            sdCx + w * 0.06f, h * 0.65f
        )
        lineTo(sdCx + w * 0.06f, h)
        close()
    }
    drawPath(smallDomeLeft, color.copy(alpha = color.alpha * 0.7f), style = Fill)

    val smallDomeRight = Path().apply {
        val sdCx = cx + w * 0.16f
        moveTo(sdCx - w * 0.06f, h)
        cubicTo(
            sdCx - w * 0.06f, h * 0.65f,
            sdCx, h * 0.55f,
            sdCx + w * 0.06f, h * 0.65f
        )
        lineTo(sdCx + w * 0.06f, h)
        close()
    }
    drawPath(smallDomeRight, color.copy(alpha = color.alpha * 0.7f), style = Fill)

    // Base wall connecting everything
    val basePath = Path().apply {
        moveTo(lmX - minaretWidth * 1.5f, h)
        lineTo(lmX - minaretWidth * 1.5f, h * 0.75f)
        lineTo(rmX + minaretWidth * 1.5f, h * 0.75f)
        lineTo(rmX + minaretWidth * 1.5f, h)
        close()
    }
    drawPath(basePath, color.copy(alpha = color.alpha * 0.5f), style = Fill)
}

@Composable
private fun BannerCard(text: String, bg: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NextPrayerCard(
    next: com.syedmsidsm.prayerreminder.domain.model.NextPrayer,
    hasWarning: Boolean = false,
    amMarker: String,
    pmMarker: String,
    voiceSummary: String,
    voiceEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Ground)
    ) {
        Column(
            modifier = Modifier
                .drawBehind {
                    // Subtle radial gradient for premium depth
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrayerReminderColors.Gold.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.8f, size.height * 0.2f),
                            radius = size.width * 0.6f
                        )
                    )
                }
                .padding(20.dp)
        ) {
            // Tag + Prayer Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrayerReminderColors.TagBg
                ) {
                    val tagText = if (next.phase == NextPrayerPhase.BEFORE_AZAN) {
                        stringResource(R.string.next_prayer)
                    } else {
                        stringResource(R.string.now_prayer)
                    }
                    Text(
                        text = tagText,
                        color = PrayerReminderColors.TagText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                val prayerName = if (next.isJumuah) {
                    stringResource(R.string.prayer_jumuah)
                } else {
                    getPrayerName(next.prayer)
                }
                Text(
                    text = prayerName,
                    color = PrayerReminderColors.CardTextLight,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Time Display
            if (next.phase == NextPrayerPhase.BEFORE_AZAN) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.azan),
                            color = PrayerReminderColors.OnDarkMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = TimeFormatter.format12Hour(next.azanTime, amMarker, pmMarker),
                            color = PrayerReminderColors.CardTextLight,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.jamaat),
                            color = PrayerReminderColors.OnDarkMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = TimeFormatter.format12Hour(next.jamaatTime, amMarker, pmMarker),
                                color = PrayerReminderColors.Gold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (hasWarning) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = stringResource(R.string.jamaat_fixed_before_azan),
                                    tint = PrayerReminderColors.Gold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Between azan and jamaat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.jamaat),
                            color = PrayerReminderColors.OnDarkMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = TimeFormatter.format12Hour(next.jamaatTime, amMarker, pmMarker),
                                color = PrayerReminderColors.CardTextLight,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (hasWarning) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = stringResource(R.string.jamaat_fixed_before_azan),
                                    tint = PrayerReminderColors.Gold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${stringResource(R.string.azan)} ✓",
                            color = PrayerReminderColors.OnDarkMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = TimeFormatter.format12Hour(next.azanTime, amMarker, pmMarker),
                            color = PrayerReminderColors.OnDarkMuted,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Countdown line — larger and more prominent
            val context = androidx.compose.ui.platform.LocalContext.current
            val resources = context.resources
            val countdownStr = TimeFormatter.formatDuration(
                duration = next.remainingDuration,
                formatHours = { h -> resources.getQuantityString(R.plurals.hours, h.toInt(), h.toInt()) },
                formatMinutes = { m -> resources.getQuantityString(R.plurals.minutes, m.toInt(), m.toInt()) },
                durationJoin = { h, m -> context.getString(R.string.duration_join, h, m) }
            )
            val countdownLabel = if (next.phase == NextPrayerPhase.BEFORE_AZAN) {
                stringResource(R.string.azan_in, countdownStr)
            } else {
                stringResource(R.string.jamaat_in, countdownStr)
            }
            Text(
                text = countdownLabel,
                color = PrayerReminderColors.Gold,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Decorative arabesque-style separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .drawBehind {
                        // Center-fading gold line
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PrayerReminderColors.Gold.copy(alpha = 0.3f),
                                    PrayerReminderColors.Gold.copy(alpha = 0.5f),
                                    PrayerReminderColors.Gold.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
            )
            // Small gold diamond at center of separator
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .offset(y = (-4).dp)
                    .size(7.dp)
                    .drawBehind {
                        val diamond = Path().apply {
                            moveTo(size.width / 2f, 0f)
                            lineTo(size.width, size.height / 2f)
                            lineTo(size.width / 2f, size.height)
                            lineTo(0f, size.height / 2f)
                            close()
                        }
                        drawPath(diamond, PrayerReminderColors.Gold.copy(alpha = 0.5f))
                    }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Voice info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = PrayerReminderColors.OnDarkMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val voiceLine = if (voiceEnabled && voiceSummary.isNotEmpty()) {
                    stringResource(
                        R.string.voice_line,
                        stringResource(R.string.min_short, 10),
                        stringResource(R.string.min_short, 5)
                    )
                } else {
                    stringResource(R.string.voice_off_line)
                }
                Text(
                    text = voiceLine,
                    color = PrayerReminderColors.OnDarkMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TodayPrayersList(
    schedule: com.syedmsidsm.prayerreminder.domain.model.DaySchedule,
    nextPrayer: com.syedmsidsm.prayerreminder.domain.model.NextPrayer?,
    currentTime: java.time.LocalTime,
    amMarker: String,
    pmMarker: String,
    reminderStates: Map<Prayer, Boolean>,
    onToggleReminder: (Prayer) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.today),
                    style = MaterialTheme.typography.labelLarge,
                    color = PrayerReminderColors.Muted,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = stringResource(R.string.azan),
                    style = MaterialTheme.typography.labelLarge,
                    color = PrayerReminderColors.Muted,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = stringResource(R.string.jamaat),
                    style = MaterialTheme.typography.labelLarge,
                    color = PrayerReminderColors.Muted,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.3f)
                )
                Spacer(modifier = Modifier.size(28.dp)) // space for bell
            }

            HorizontalDivider(color = PrayerReminderColors.Line, thickness = 0.5.dp)

            val prayers = listOf(
                Prayer.FAJR,
                Prayer.SUNRISE,
                Prayer.DHUHR,
                Prayer.ASR,
                Prayer.MAGHRIB,
                Prayer.ISHA
            )

            for (prayer in prayers) {
                val azan = schedule.azan[prayer] ?: continue
                val jamaat = schedule.jamaat[prayer]
                val isNext = nextPrayer?.prayer == prayer && !nextPrayer.isTomorrow
                val isPast = if (prayer == Prayer.SUNRISE) {
                    currentTime.isAfter(azan)
                } else {
                    val j = jamaat ?: azan.plusMinutes(15)
                    currentTime.isAfter(j)
                }

                PrayerRow(
                    prayer = prayer,
                    isFriday = schedule.isFriday,
                    azanTime = azan,
                    jamaatTime = jamaat,
                    hasWarning = schedule.fixedTimeBeforeAzanWarnings.contains(prayer),
                    isNext = isNext,
                    isPast = isPast,
                    amMarker = amMarker,
                    pmMarker = pmMarker,
                    isReminderEnabled = reminderStates[prayer] ?: true,
                    onToggleReminder = { onToggleReminder(prayer) }
                )
            }
        }
    }
}

@Composable
private fun PrayerRow(
    prayer: Prayer,
    isFriday: Boolean,
    azanTime: java.time.LocalTime,
    jamaatTime: java.time.LocalTime?,
    hasWarning: Boolean = false,
    isNext: Boolean,
    isPast: Boolean,
    amMarker: String,
    pmMarker: String,
    isReminderEnabled: Boolean,
    onToggleReminder: () -> Unit
) {
    val prayerName = if (prayer == Prayer.DHUHR && isFriday) {
        stringResource(R.string.prayer_jumuah)
    } else {
        getPrayerName(prayer)
    }

    val rowBg = if (isNext) PrayerReminderColors.NextRowBg else androidx.compose.ui.graphics.Color.Transparent
    val textColor = when {
        isNext -> PrayerReminderColors.Ink
        isPast -> PrayerReminderColors.Muted
        else -> PrayerReminderColors.Ink
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = rowBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isNext) {
                        // Gold left accent border for next prayer
                        Modifier.drawBehind {
                            drawRoundRect(
                                color = PrayerReminderColors.Gold,
                                topLeft = Offset(0f, size.height * 0.15f),
                                size = androidx.compose.ui.geometry.Size(2.dp.toPx(), size.height * 0.7f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx())
                            )
                        }
                    } else Modifier
                )
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Name + Next tag
            Row(
                modifier = Modifier.weight(1.5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prayerName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
                )
                if (isNext) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PrayerReminderColors.TagBg
                    ) {
                        Text(
                            text = stringResource(R.string.next_tag),
                            color = PrayerReminderColors.TagText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Azan time
            Text(
                text = TimeFormatter.format12Hour(azanTime, amMarker, pmMarker),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1.3f)
            )

            // Jamaat time
            val jamaatText = if (jamaatTime != null) {
                TimeFormatter.format12Hour(jamaatTime, amMarker, pmMarker)
            } else {
                "—"
            }
            Row(
                modifier = Modifier.weight(1.3f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = jamaatText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
                )
                if (hasWarning) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.jamaat_fixed_before_azan),
                        tint = PrayerReminderColors.WarnText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Bell icon
            if (prayer.hasReminders) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onToggleReminder() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isReminderEnabled) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = if (isReminderEnabled) {
                            PrayerReminderColors.GoldText
                        } else {
                            PrayerReminderColors.Muted
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun getPrayerName(prayer: Prayer): String {
    return when (prayer) {
        Prayer.FAJR -> stringResource(R.string.prayer_fajr)
        Prayer.SUNRISE -> stringResource(R.string.prayer_sunrise)
        Prayer.DHUHR -> stringResource(R.string.prayer_dhuhr)
        Prayer.ASR -> stringResource(R.string.prayer_asr)
        Prayer.MAGHRIB -> stringResource(R.string.prayer_maghrib)
        Prayer.ISHA -> stringResource(R.string.prayer_isha)
    }
}
