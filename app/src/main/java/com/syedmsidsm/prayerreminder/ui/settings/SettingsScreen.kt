package com.syedmsidsm.prayerreminder.ui.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.CityPresets
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.ui.components.MosqueBackdrop
import com.syedmsidsm.prayerreminder.ui.components.SanctuaryBottomNavBar
import com.syedmsidsm.prayerreminder.ui.components.SanctuaryTab
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import com.syedmsidsm.prayerreminder.widget.SmallPrayerWidgetReceiver
import com.syedmsidsm.prayerreminder.widget.StripPrayerWidgetReceiver
import com.syedmsidsm.prayerreminder.widget.WidePrayerWidgetReceiver
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val AmberAccent = Color(0xFFFBBF24)
private val AmberTextDark = Color(0xFF020617)
private val AmberGlowText = Color(0xFFFCD34D)
private val SanctuaryDarkBg = Color(0xFF03080C)
private val SanctuaryLightBg = Color(0xFFF6F1E6)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToJamaat: () -> Unit,
    onNavigateToRemindersVoice: () -> Unit,
    onNavigateToTimetables: () -> Unit,
    onImportNewTimetable: () -> Unit,
    onNavigateToReliability: () -> Unit = {},
    onAddWidget: () -> Unit = {}
) {
    val context = LocalContext.current
    val timetables by viewModel.timetables.collectAsState()
    val activeTimetable = timetables.firstOrNull()
    val voiceOn by viewModel.voiceEnabled.collectAsState()
    val beforeAzanOffsets by viewModel.remBeforeAzanOffsets.collectAsState()
    val beforeJamaatOffsets by viewModel.remBeforeJamaatOffsets.collectAsState()
    val azanReciter by viewModel.azanReciter.collectAsState()
    val playDua by viewModel.playDuaAfterAzan.collectAsState()
    val jamaatRules by viewModel.jamaatRules.collectAsState()

    val selectedCityId by viewModel.selectedCityId.collectAsState()
    val autoGps by viewModel.autoGps.collectAsState()
    val calcMethod by viewModel.calcMethod.collectAsState()
    val asrMethod by viewModel.asrMethod.collectAsState()
    val use24Hour by viewModel.use24Hour.collectAsState()

    val widgetTheme by viewModel.widgetTheme.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()

    val currentAppLocales = AppCompatDelegate.getApplicationLocales()
    val currentLangTag = if (!currentAppLocales.isEmpty) {
        currentAppLocales.get(0)?.language ?: "en"
    } else {
        "en"
    }
    var showWidgetPickerDialog by remember { mutableStateOf(false) }

    val isDark = PrayerReminderColors.isDark
    val surfaceBg = if (isDark) SanctuaryDarkBg else SanctuaryLightBg
    val glassCardBg = if (isDark) Color(0xFF0B161E).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.94f)
    val glassBorder = if (isDark) Color.White.copy(alpha = 0.11f) else Color(0xFFE4DCC7)
    val chipUnselectedBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3EFE4)
    val textPrimary = if (isDark) Color.White else Color(0xFF14262A)
    val textMuted = if (isDark) Color(0xFFA8A29E) else Color(0xFF5E6D70)
    val selectedGoldText = if (isDark) AmberGlowText else Color(0xFF92400E)

    if (showWidgetPickerDialog) {
        AlertDialog(
            onDismissRequest = { showWidgetPickerDialog = false },
            containerColor = PrayerReminderColors.Surface,
            title = {
                Text(
                    text = stringResource(R.string.add_widget),
                    color = PrayerReminderColors.Ink,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Triple(
                            "4×2 Compact 5-Prayer Strip",
                            "Next prayer countdown + full 5-prayer strip",
                            WidePrayerWidgetReceiver::class.java
                        ),
                        Triple(
                            "2×2 Lock-Screen Countdown Square",
                            "Compact gold progress ring with live countdown",
                            SmallPrayerWidgetReceiver::class.java
                        ),
                        Triple(
                            "4×1 Minimal Horizontal Pill",
                            "Horizontal bar with next prayer & Hijri date",
                            StripPrayerWidgetReceiver::class.java
                        )
                    ).forEach { (title, subtitle, receiverClass) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showWidgetPickerDialog = false
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val mgr = AppWidgetManager.getInstance(context)
                                        if (mgr.isRequestPinAppWidgetSupported) {
                                            mgr.requestPinAppWidget(
                                                ComponentName(context, receiverClass),
                                                null,
                                                null
                                            )
                                        }
                                    } else {
                                        onAddWidget()
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Ivory)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PrayerReminderColors.Ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrayerReminderColors.Muted
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWidgetPickerDialog = false }) {
                    Text(stringResource(R.string.cancel), color = PrayerReminderColors.Ink)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
    ) {
        MosqueBackdrop(
            activeSky = Prayer.ISHA,
            surfaceFadeColor = surfaceBg
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Minimalist Sanctuary Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.38f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.settings),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 6.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. LOCATION & MASJID TIMETABLE CARD
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Location & Schedule",
                                color = textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (autoGps) Color(0xFF10B981).copy(alpha = 0.18f) else chipUnselectedBg,
                            border = BorderStroke(
                                1.dp,
                                if (autoGps) Color(0xFF34D399).copy(alpha = 0.45f) else glassBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { viewModel.setAutoGps(!autoGps) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = null,
                                    tint = if (autoGps) Color(0xFF34D399) else textMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (autoGps) "Auto GPS On" else "Auto GPS Off",
                                    color = if (autoGps) (if (isDark) Color(0xFF6EE7B7) else Color(0xFF065F46)) else textMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2-column grid of City Presets
                    val cityRows = CityPresets.ALL.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (pair in cityRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (city in pair) {
                                    val isSelected = city.id == selectedCityId
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) AmberAccent else chipUnselectedBg,
                                        border = BorderStroke(1.dp, if (isSelected) AmberGlowText else glassBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 44.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { viewModel.setSelectedCityId(city.id) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = city.displayName,
                                                color = if (isSelected) AmberTextDark else textPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = AmberTextDark,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Active Timetable Status + OCR Scan / All Timetables
                    val today = LocalDate.now()
                    val currentTable = timetables.firstOrNull { table ->
                        val first = runCatching { LocalDate.parse(table.firstDate) }.getOrNull()
                        val last = runCatching { LocalDate.parse(table.lastDate) }.getOrNull()
                        first != null && last != null && !today.isBefore(first) && !today.isAfter(last)
                    } ?: activeTimetable

                    if (currentTable != null) {
                        val lastDate = runCatching { LocalDate.parse(currentTable.lastDate) }.getOrNull()
                        val daysLeft = if (lastDate != null) {
                            ChronoUnit.DAYS.between(today, lastDate).toInt().coerceAtLeast(0)
                        } else 0

                        Spacer(modifier = Modifier.height(10.dp))
                        val statusLine = if (daysLeft > 0) {
                            "${currentTable.hijriMonthName} ${currentTable.hijriYear} · ${stringResource(R.string.days_left, daysLeft)}"
                        } else {
                            "Built-in Bhatkal schedule active · ${currentTable.hijriMonthName} ${currentTable.hijriYear} ready"
                        }
                        Text(
                            text = statusLine,
                            color = textMuted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(4.dp))

                    SettingsNavigationRow(
                        icon = Icons.Default.Upload,
                        title = stringResource(R.string.import_new),
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        onClick = onImportNewTimetable
                    )
                    SettingsNavigationRow(
                        icon = Icons.Default.DateRange,
                        title = stringResource(R.string.all_timetables),
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        onClick = onNavigateToTimetables
                    )
                }

                // 2. CALCULATION AUTHORITY, ASR JURISTIC & TIME FORMAT CARD
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Text(
                        text = "Calculation Method",
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val methods = listOf(
                        "MWL" to "Muslim World League",
                        "ISNA" to "ISNA (North America)",
                        "UmmAlQura" to "Umm al-Qura",
                        "Karachi" to "Karachi University"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in methods.chunked(2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for ((id, label) in row) {
                                    val selected = calcMethod == id
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (selected) AmberAccent.copy(alpha = 0.22f) else chipUnselectedBg,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) AmberAccent else glassBorder
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { viewModel.setCalcMethod(id) }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (selected) selectedGoldText else textPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Asr Juristic", color = textMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            SanctuarySegmentedToggle(
                                options = listOf("Standard" to "Standard", "Hanafi" to "Hanafi"),
                                selectedKey = asrMethod,
                                bg = if (isDark) Color.Black.copy(alpha = 0.38f) else chipUnselectedBg,
                                border = glassBorder,
                                textMuted = textMuted,
                                onSelect = { viewModel.setAsrMethod(it) }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Time Format", color = textMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            SanctuarySegmentedToggle(
                                options = listOf("12h" to "12h", "24h" to "24h"),
                                selectedKey = if (use24Hour) "24h" else "12h",
                                bg = if (isDark) Color.Black.copy(alpha = 0.38f) else chipUnselectedBg,
                                border = glassBorder,
                                textMuted = textMuted,
                                onSelect = { viewModel.setUse24Hour(it == "24h") }
                            )
                        }
                    }
                }

                // 3. EASY IQAMAH OFFSET STEPPERS + FULL JAMAAT SETTINGS
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Iqamah Delay (Minutes)",
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val stepperKeys = listOf(
                            "FAJR" to "Fajr",
                            "DHUHR" to "Dhuhr",
                            "ASR" to "Asr",
                            "MAGHRIB" to "Maghrib",
                            "ISHA" to "Isha"
                        )
                        for ((pKey, pLabel) in stepperKeys) {
                            val rule = jamaatRules[pKey]
                            val offsetText = when (rule) {
                                is JamaatRule.AfterAzan -> "+${rule.minutes}"
                                is JamaatRule.Fixed -> "Fix"
                                null -> "+15"
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = chipUnselectedBg,
                                border = BorderStroke(1.dp, glassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = pLabel,
                                        color = textMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = offsetText,
                                        color = selectedGoldText,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE6DFCF))
                                                .clickable { viewModel.adjustIqamahDelay(pKey, -5) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("-", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE6DFCF))
                                                .clickable { viewModel.adjustIqamahDelay(pKey, 5) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = glassBorder)
                    SettingsItemRow(
                        icon = Icons.Default.AccessTime,
                        title = stringResource(R.string.jamaat_times),
                        subtitle = "Set custom minutes or fixed Jamaat time & Jumu'ah",
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        iconBg = chipUnselectedBg,
                        onClick = onNavigateToJamaat
                    )
                }

                // 4. AZAN RECITERS, DUA & MULTIPLE REMINDERS
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    val reciterLabel = when (azanReciter) {
                        "nurdin" -> "NurDin Hamza Al Maghriby 🇲🇦"
                        "najee" -> "Najee Qazaz 🇵🇸"
                        else -> "Ali Ibn Ahmad Mala 🇸🇦"
                    }
                    val azanListStr = beforeAzanOffsets.sortedDescending().joinToString(", ") { "${it}m" }.ifEmpty { "Off" }
                    val jamListStr = beforeJamaatOffsets.sortedDescending().joinToString(", ") { "${it}m" }.ifEmpty { "Off" }
                    val duaStatus = if (playDua) "Dua After Azan On" else "Dua Off"
                    val voiceSummary = if (voiceOn) {
                        "$reciterLabel • $duaStatus\nBefore Azan: $azanListStr • Before Jamaat: $jamListStr"
                    } else {
                        stringResource(R.string.voice_off_line)
                    }

                    SettingsItemRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = stringResource(R.string.reminders_voice),
                        subtitle = voiceSummary,
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        iconBg = chipUnselectedBg,
                        onClick = onNavigateToRemindersVoice
                    )
                }

                // 5. APPEARANCE, FONT SIZE, LANGUAGE, WIDGETS & RELIABILITY
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.language),
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChoiceChip(
                            label = "English",
                            isSelected = currentLangTag == "en",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                viewModel.setAppLanguage("en")
                                viewModel.setVoiceLanguage("en")
                            }
                        )
                        ChoiceChip(
                            label = "العربية",
                            isSelected = currentLangTag == "ar",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
                                viewModel.setAppLanguage("ar")
                                viewModel.setVoiceLanguage("ar")
                            }
                        )
                        ChoiceChip(
                            label = "اردو",
                            isSelected = currentLangTag == "ur",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ur"))
                                viewModel.setAppLanguage("ur")
                                viewModel.setVoiceLanguage("ur")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val themeTitle = when (currentLangTag) {
                            "ar" -> "المظهر (فاتح / داكن)"
                            "ur" -> "تھیم (لائٹ / ڈارک)"
                            else -> "Appearance (Light / Dark)"
                        }
                        Text(
                            text = themeTitle,
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChoiceChip(
                            label = "☀️ ${stringResource(R.string.theme_light)}",
                            isSelected = themeMode == "light",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setThemeMode("light") }
                        )
                        ChoiceChip(
                            label = "🌙 ${stringResource(R.string.theme_dark)}",
                            isSelected = themeMode == "dark",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setThemeMode("dark") }
                        )
                        ChoiceChip(
                            label = stringResource(R.string.theme_system),
                            isSelected = themeMode == "system",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setThemeMode("system") }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.font_size),
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val fontOptions = listOf(
                            "small" to stringResource(R.string.font_small),
                            "normal" to stringResource(R.string.font_normal),
                            "large" to stringResource(R.string.font_large),
                            "xl" to stringResource(R.string.font_xl)
                        )
                        for ((scaleKey, scaleLabel) in fontOptions) {
                            ChoiceChip(
                                label = scaleLabel,
                                isSelected = fontScale == scaleKey,
                                unselectedBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setFontScale(scaleKey) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)

                    SettingsItemRow(
                        icon = Icons.Default.Widgets,
                        title = stringResource(R.string.widget),
                        subtitle = stringResource(R.string.add_widget),
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        iconBg = chipUnselectedBg,
                        onClick = { showWidgetPickerDialog = true }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WidgetThemeChip(
                            label = stringResource(R.string.widget_theme_dark),
                            swatchColor = Color(0xFF07131A),
                            isSelected = widgetTheme == "dark_teal",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setWidgetTheme("dark_teal") }
                        )
                        WidgetThemeChip(
                            label = stringResource(R.string.widget_theme_cream),
                            swatchColor = Color(0xFFF7F4EB),
                            isSelected = widgetTheme == "light_cream",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setWidgetTheme("light_cream") }
                        )
                        WidgetThemeChip(
                            label = stringResource(R.string.widget_theme_gold),
                            swatchColor = Color(0xFFD9AE5C),
                            isSelected = widgetTheme == "warm_gold",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setWidgetTheme("warm_gold") }
                        )
                    }

                    HorizontalDivider(color = glassBorder, modifier = Modifier.padding(top = 6.dp))

                    SettingsItemRow(
                        icon = Icons.Default.CheckCircle,
                        title = stringResource(R.string.reliability),
                        subtitle = stringResource(R.string.privacy_line),
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        iconBg = chipUnselectedBg,
                        onClick = onNavigateToReliability
                    )
                }

                // 6. ABOUT CARD
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.about),
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${stringResource(R.string.app_name)} v3.0.0",
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.privacy_line),
                        color = textMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Floating Bottom Pill Navigation
        SanctuaryBottomNavBar(
            activeTab = SanctuaryTab.SETTINGS,
            onHomeClick = onBack,
            onSettingsClick = {},
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SanctuaryGlassCard(
    bg: Color,
    border: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SanctuarySegmentedToggle(
    options: List<Pair<String, String>>,
    selectedKey: String,
    bg: Color,
    border: Color,
    textMuted: Color,
    onSelect: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            for ((key, label) in options) {
                val active = key == selectedKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) AmberAccent else Color.Transparent)
                        .clickable { onSelect(key) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (active) AmberTextDark else textMuted,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    isSelected: Boolean,
    unselectedBg: Color,
    border: Color,
    textPrimary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AmberAccent else unselectedBg,
        border = BorderStroke(1.dp, if (isSelected) AmberGlowText else border)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) AmberTextDark else textPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    textColor: Color,
    mutedColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 11.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AmberAccent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = mutedColor,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: Color,
    mutedColor: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AmberAccent,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = mutedColor,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = mutedColor,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun WidgetThemeChip(
    label: String,
    swatchColor: Color,
    isSelected: Boolean,
    unselectedBg: Color,
    border: Color,
    textPrimary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AmberAccent else unselectedBg,
        border = BorderStroke(1.dp, if (isSelected) AmberGlowText else border)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(swatchColor)
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) AmberTextDark else textPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}
