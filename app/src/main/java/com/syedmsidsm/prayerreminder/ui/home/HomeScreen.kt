package com.syedmsidsm.prayerreminder.ui.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()
    val languageCode = currentLocale.language ?: "en"

    val amMarker = stringResource(R.string.am_marker)
    val pmMarker = stringResource(R.string.pm_marker)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrayerReminderColors.Ivory)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 1. Header (Place name, Timetable subtitle, Settings button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrayerReminderColors.Ground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = PrayerReminderColors.Gold,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = uiState.place,
                        style = MaterialTheme.typography.headlineLarge,
                        color = PrayerReminderColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                    val table = uiState.activeTimetable
                    if (table != null) {
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrayerReminderColors.Muted
                        )
                    }
                }
            }

            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = PrayerReminderColors.Ink
                )
            }
        }

        // 2. Warning Banners (if estimated or no table)
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

        // 4. Next Prayer Card (Dark Ground #0F2A2E)
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BannerCard(text: String, bg: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(14.dp)
        )
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Ground)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
                    color = PrayerReminderColors.Ivory,
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
                            color = PrayerReminderColors.Ivory,
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
                                color = PrayerReminderColors.Ivory,
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

            Spacer(modifier = Modifier.height(12.dp))

            // Countdown line
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = PrayerReminderColors.NextRowBgDark)
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
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
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

            HorizontalDivider(color = PrayerReminderColors.Line)

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
                .padding(horizontal = 8.dp, vertical = 12.dp),
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
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PrayerReminderColors.TagBg
                    ) {
                        Text(
                            text = stringResource(R.string.next_tag),
                            color = PrayerReminderColors.TagText,
                            fontSize = 10.sp,
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
                            if (isNext) PrayerReminderColors.Ground else PrayerReminderColors.GoldText
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
