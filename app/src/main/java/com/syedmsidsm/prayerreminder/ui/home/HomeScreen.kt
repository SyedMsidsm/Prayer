package com.syedmsidsm.prayerreminder.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.HijriNames
import com.syedmsidsm.prayerreminder.domain.model.NextPrayer
import com.syedmsidsm.prayerreminder.domain.model.NextPrayerPhase
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.ScheduleSource
import com.syedmsidsm.prayerreminder.ui.components.MosqueBackdrop
import com.syedmsidsm.prayerreminder.ui.components.SanctuaryBottomNavBar
import com.syedmsidsm.prayerreminder.ui.components.SanctuarySkyThemes
import com.syedmsidsm.prayerreminder.ui.components.SanctuaryTab
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SanctuaryDarkSurface = Color(0xFF03080C)
private val SanctuaryLightSurface = Color(0xFFF6F1E6)
private val AmberBright = Color(0xFFFBBF24)
private val AmberSoft = Color(0xFFFCD34D)
private val AmberTint = Color(0xFFFDE68A)
private val EmeraldSoft = Color(0xFF6EE7B7)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSettingsClick: () -> Unit = {},
    onNavigateToTimetables: () -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()
    val languageCode = currentLocale.language ?: "en"

    val amMarker = stringResource(R.string.am_marker)
    val pmMarker = stringResource(R.string.pm_marker)

    val isDark = PrayerReminderColors.isDark
    val surfaceColor = if (isDark) SanctuaryDarkSurface else SanctuaryLightSurface
    val cardTextPrimary = if (isDark) Color.White else Color(0xFF14262A)
    val cardTextSecondary = if (isDark) Color(0xFFA8A29E) else Color(0xFF5E6D70)
    val rowBgNormal = if (isDark) Color.White.copy(alpha = 0.055f) else Color.White.copy(alpha = 0.88f)
    val rowBgNext = if (isDark) Color.White.copy(alpha = 0.13f) else Color(0xFFFFF8E7)
    val rowBorderNormal = if (isDark) Color.White.copy(alpha = 0.11f) else Color(0xFFE6DEC8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        // 1. Seamless Layered Mosque Sanctuary Backdrop
        MosqueBackdrop(
            activeSky = uiState.activeSky,
            surfaceFadeColor = surfaceColor
        )

        // 2. Main Vertical Scroll Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // MINIMALIST HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Place & Hijri / Timetable Subtitle
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToTimetables() }
                        .padding(end = 8.dp, top = 2.dp, bottom = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = AmberBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.place,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val hijri = uiState.schedule?.hijri
                    val subtitleText = if (hijri != null) {
                        val hijriMonthName = HijriNames.getMonthName(hijri.month, languageCode)
                        "${hijri.day} $hijriMonthName ${hijri.year}"
                    } else {
                        uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM", currentLocale))
                    }
                    Text(
                        text = subtitleText,
                        color = Color(0xFFD6D3D1).copy(alpha = 0.88f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                    )
                }

                // Right: Sky Vibe Switcher & Theme Toggle Button ONLY
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sky Vibe Switcher Pill
                    val skyLabel = if (uiState.skyOverride.isBlank()) {
                        stringResource(R.string.auto_sky)
                    } else {
                        SanctuarySkyThemes.forPrayer(uiState.activeSky).label
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.38f),
                        border = BorderStroke(
                            1.dp,
                            if (uiState.skyOverride.isNotBlank()) AmberBright.copy(alpha = 0.55f)
                            else Color.White.copy(alpha = 0.14f)
                        ),
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { viewModel.cycleSkyVibe() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Sky Vibe",
                                tint = AmberBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = skyLabel,
                                color = AmberTint,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }

                    // Theme toggle button
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.38f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = AmberBright,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // MAIN SCROLLABLE BODY
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 6.dp, bottom = 104.dp)
            ) {
                // HERO NEXT PRAYER FOCUS
                val next = uiState.nextPrayer
                val schedule = uiState.schedule

                if (next != null) {
                    HeroNextPrayerSection(
                        next = next,
                        countdownHms = uiState.countdownHms,
                        progressPct = uiState.progressPct,
                        use24Hour = uiState.use24Hour,
                        amMarker = amMarker,
                        pmMarker = pmMarker,
                        hasWarning = schedule?.fixedTimeBeforeAzanWarnings?.contains(next.prayer) == true
                    )
                }

                // Status / Warning Banners
                if (schedule != null && schedule.source.isEstimated) {
                    Spacer(modifier = Modifier.height(10.dp))
                    BannerPill(
                        text = stringResource(R.string.banner_estimated),
                        bg = PrayerReminderColors.WarnBg,
                        textColor = PrayerReminderColors.WarnText
                    )
                } else if (schedule != null && schedule.source == ScheduleSource.NONE) {
                    Spacer(modifier = Modifier.height(10.dp))
                    BannerPill(
                        text = stringResource(R.string.banner_no_timetable),
                        bg = PrayerReminderColors.WarnBg,
                        textColor = PrayerReminderColors.WarnText
                    )
                }

                if (uiState.missedCount7d > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BannerPill(
                        text = stringResource(R.string.banner_missed),
                        bg = PrayerReminderColors.WarnBg,
                        textColor = PrayerReminderColors.WarnText
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // DAILY PRAYER LIST
                if (schedule != null && schedule.source != ScheduleSource.NONE) {
                    val prayers = listOf(
                        Prayer.FAJR,
                        Prayer.SUNRISE,
                        Prayer.DHUHR,
                        Prayer.ASR,
                        Prayer.MAGHRIB,
                        Prayer.ISHA
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (prayer in prayers) {
                            val azan = schedule.azan[prayer] ?: continue
                            val jamaat = schedule.jamaat[prayer]
                            val isNext = next?.prayer == prayer && !next.isTomorrow
                            val alertMode = uiState.alertModes[prayer] ?: if (prayer.hasReminders) {
                                if (uiState.reminderStates[prayer] != false) "sound" else "muted"
                            } else {
                                "muted"
                            }

                            SanctuaryPrayerRow(
                                prayer = prayer,
                                isFriday = schedule.isFriday,
                                azanTime = azan,
                                jamaatTime = jamaat,
                                isNext = isNext,
                                use24Hour = uiState.use24Hour,
                                amMarker = amMarker,
                                pmMarker = pmMarker,
                                alertMode = alertMode,
                                isDark = isDark,
                                rowBgNormal = rowBgNormal,
                                rowBgNext = rowBgNext,
                                rowBorderNormal = rowBorderNormal,
                                cardTextPrimary = cardTextPrimary,
                                cardTextSecondary = cardTextSecondary,
                                hasWarning = schedule.fixedTimeBeforeAzanWarnings.contains(prayer),
                                onToggleAlert = { viewModel.cyclePrayerAlertMode(prayer) }
                            )
                        }
                    }
                }
            }
        }

        // 3. FLOATING BOTTOM PILL NAVIGATION (HOME & SETTINGS)
        SanctuaryBottomNavBar(
            activeTab = SanctuaryTab.HOME,
            onHomeClick = {},
            onSettingsClick = onSettingsClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HeroNextPrayerSection(
    next: NextPrayer,
    countdownHms: String,
    progressPct: Float,
    use24Hour: Boolean,
    amMarker: String,
    pmMarker: String,
    hasWarning: Boolean
) {
    val prayerLabel = if (next.isJumuah) {
        stringResource(R.string.prayer_jumuah)
    } else {
        getPrayerName(next.prayer)
    }
    val arabicLabel = getPrayerArabic(next.prayer)
    val phaseTag = if (next.phase == NextPrayerPhase.BEFORE_AZAN) {
        stringResource(R.string.up_next)
    } else {
        stringResource(R.string.now_prayer)
    }

    val targetTime = if (next.phase == NextPrayerPhase.BEFORE_AZAN) next.azanTime else next.jamaatTime
    val (timeDigits, suffix) = TimeFormatter.formatTimeParts(targetTime, use24Hour, amMarker, pmMarker)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pill: "Up Next • الفجر"
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.Black.copy(alpha = 0.42f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
        ) {
            Text(
                text = "$phaseTag • $arabicLabel",
                color = AmberTint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Hero Prayer Name + Monospace Time
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$prayerLabel ",
                color = Color.White,
                fontSize = 33.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = timeDigits,
                color = AmberSoft,
                fontSize = 33.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light
            )
            if (suffix.isNotEmpty()) {
                Text(
                    text = " $suffix",
                    color = Color(0xFFD6D3D1),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Iqamah & Azan compact line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val azanFormatted = TimeFormatter.formatTime(next.azanTime, use24Hour, amMarker, pmMarker)
            val jamaatFormatted = TimeFormatter.formatTime(next.jamaatTime, use24Hour, amMarker, pmMarker)
            Text(
                text = "${stringResource(R.string.azan)} $azanFormatted  •  ${stringResource(R.string.jamaat)} $jamaatFormatted",
                color = Color(0xFFE7E5E4).copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            if (hasWarning) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.jamaat_fixed_before_azan),
                    tint = AmberBright,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Countdown & Progress Bar
        Column(
            modifier = Modifier.widthIn(max = 250.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.remaining),
                    color = Color(0xFFD6D3D1),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = countdownHms,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Progress Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.16f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPct.coerceIn(0.05f, 1.0f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AmberSoft, Color(0xFFF59E0B))
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun SanctuaryPrayerRow(
    prayer: Prayer,
    isFriday: Boolean,
    azanTime: java.time.LocalTime,
    jamaatTime: java.time.LocalTime?,
    isNext: Boolean,
    use24Hour: Boolean,
    amMarker: String,
    pmMarker: String,
    alertMode: String,
    isDark: Boolean,
    rowBgNormal: Color,
    rowBgNext: Color,
    rowBorderNormal: Color,
    cardTextPrimary: Color,
    cardTextSecondary: Color,
    hasWarning: Boolean,
    onToggleAlert: () -> Unit
) {
    val prayerName = if (prayer == Prayer.DHUHR && isFriday) {
        stringResource(R.string.prayer_jumuah)
    } else {
        getPrayerName(prayer)
    }
    val prayerIcon = getPrayerIcon(prayer)

    val rowBg = if (isNext) rowBgNext else rowBgNormal
    val borderStroke = if (isNext) {
        BorderStroke(1.2.dp, AmberBright.copy(alpha = 0.65f))
    } else {
        BorderStroke(1.dp, rowBorderNormal)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 58.dp),
        shape = RoundedCornerShape(16.dp),
        color = rowBg,
        border = borderStroke,
        shadowElevation = if (isNext) 6.dp else if (!isDark) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon Box + Name + Iqamah Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isNext) AmberBright
                            else if (isDark) Color.White.copy(alpha = 0.09f)
                            else Color(0xFFEDE6D4)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = prayerIcon,
                        contentDescription = null,
                        tint = if (isNext) Color(0xFF020617)
                        else if (isDark) Color(0xFFD6D3D1)
                        else Color(0xFF4A5859),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayerName,
                            color = if (isNext) cardTextPrimary else cardTextPrimary.copy(alpha = 0.92f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isDark) AmberBright.copy(alpha = 0.22f) else AmberBright.copy(alpha = 0.30f)
                            ) {
                                Text(
                                    text = stringResource(R.string.next_tag).uppercase(Locale.US),
                                    color = if (isDark) AmberSoft else Color(0xFF78350F),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (jamaatTime != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${stringResource(R.string.jamaat)} ${TimeFormatter.formatTime(jamaatTime, use24Hour, amMarker, pmMarker)}",
                                color = cardTextSecondary,
                                fontSize = 12.sp
                            )
                            if (hasWarning) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = stringResource(R.string.jamaat_fixed_before_azan),
                                    tint = AmberBright,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Right: Monospace Adhan Time + One-Tap Alert Mode Toggle Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = TimeFormatter.formatTime(azanTime, use24Hour, amMarker, pmMarker),
                    color = if (isNext) (if (isDark) AmberSoft else Color(0xFF92400E)) else cardTextPrimary,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                if (prayer.hasReminders) {
                    val (btnBg, btnBorder, btnTint, btnIcon) = when (alertMode) {
                        "sound" -> AlertBtnStyle(
                            bg = AmberBright.copy(alpha = if (isDark) 0.16f else 0.22f),
                            border = AmberBright.copy(alpha = 0.45f),
                            tint = if (isDark) AmberSoft else Color(0xFF92400E),
                            icon = Icons.AutoMirrored.Filled.VolumeUp
                        )
                        "vibrate" -> AlertBtnStyle(
                            bg = Color(0xFF34D399).copy(alpha = if (isDark) 0.16f else 0.20f),
                            border = Color(0xFF34D399).copy(alpha = 0.45f),
                            tint = if (isDark) EmeraldSoft else Color(0xFF065F46),
                            icon = Icons.Default.Vibration
                        )
                        else -> AlertBtnStyle(
                            bg = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFE7E2D6),
                            border = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFD5CEBF),
                            tint = cardTextSecondary,
                            icon = Icons.Default.NotificationsOff
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(btnBg)
                            .border(1.dp, btnBorder, RoundedCornerShape(12.dp))
                            .clickable { onToggleAlert() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = btnIcon,
                            contentDescription = "Toggle $prayerName alert ($alertMode)",
                            tint = btnTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFEDE8DC))
                            .border(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFDAD3C3),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = cardTextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class AlertBtnStyle(
    val bg: Color,
    val border: Color,
    val tint: Color,
    val icon: ImageVector
)

@Composable
private fun BannerPill(text: String, bg: Color, textColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = bg
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

private fun getPrayerIcon(prayer: Prayer): ImageVector = when (prayer) {
    Prayer.FAJR -> Icons.Default.WbTwilight
    Prayer.SUNRISE -> Icons.Default.WbSunny
    Prayer.DHUHR -> Icons.Default.Cloud
    Prayer.ASR -> Icons.Default.WbSunny
    Prayer.MAGHRIB -> Icons.Default.NightsStay
    Prayer.ISHA -> Icons.Default.Bedtime
}

private fun getPrayerArabic(prayer: Prayer): String = when (prayer) {
    Prayer.FAJR -> "الفجر"
    Prayer.SUNRISE -> "الشروق"
    Prayer.DHUHR -> "الظهر"
    Prayer.ASR -> "العصر"
    Prayer.MAGHRIB -> "المغرب"
    Prayer.ISHA -> "العشاء"
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
