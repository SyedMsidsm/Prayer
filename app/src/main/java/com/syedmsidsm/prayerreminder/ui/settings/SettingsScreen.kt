package com.syedmsidsm.prayerreminder.ui.settings

import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToJamaat: () -> Unit,
    onNavigateToRemindersVoice: () -> Unit,
    onNavigateToTimetables: () -> Unit,
    onImportNewTimetable: () -> Unit,
    onSendCurrentToFamily: (Timetable) -> Unit,
    onNavigateToReliability: () -> Unit = {},
    onAddWidget: () -> Unit = {}
) {
    val context = LocalContext.current
    val timetables by viewModel.timetables.collectAsState()
    val activeTimetable = timetables.firstOrNull()
    val voiceOn by viewModel.voiceEnabled.collectAsState()
    val beforeAzanMin by viewModel.remBeforeAzanMin.collectAsState()
    val beforeJamaatMin by viewModel.remBeforeJamaatMin.collectAsState()

    val currentAppLocales = AppCompatDelegate.getApplicationLocales()
    val currentLangTag = if (!currentAppLocales.isEmpty) {
        currentAppLocales.get(0)?.language ?: "en"
    } else {
        "en"
    }
    var showWidgetPickerDialog by remember { mutableStateOf(false) }

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
                            "Wide Card (4×2)",
                            "Next prayer countdown + full 5-prayer table",
                            com.syedmsidsm.prayerreminder.widget.WidePrayerWidgetReceiver::class.java
                        ),
                        Triple(
                            "Circular Ring (2×2)",
                            "Compact gold progress ring with live countdown",
                            com.syedmsidsm.prayerreminder.widget.SmallPrayerWidgetReceiver::class.java
                        ),
                        Triple(
                            "Slim Strip (4×1)",
                            "Horizontal 5-prayer strip with Hijri date",
                            com.syedmsidsm.prayerreminder.widget.StripPrayerWidgetReceiver::class.java
                        )
                    ).forEach { (title, subtitle, receiverClass) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showWidgetPickerDialog = false
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        val mgr = android.appwidget.AppWidgetManager.getInstance(context)
                                        if (mgr.isRequestPinAppWidgetSupported) {
                                            mgr.requestPinAppWidget(
                                                android.content.ComponentName(context, receiverClass),
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
                                    color = PrayerReminderColors.InkSubtle
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWidgetPickerDialog = false }) {
                    Text(stringResource(R.string.cancel), color = PrayerReminderColors.Forest)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        color = PrayerReminderColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
                            tint = PrayerReminderColors.Ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrayerReminderColors.Ivory
                )
            )
        },
        containerColor = PrayerReminderColors.Ivory
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Language Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = PrayerReminderColors.GoldText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.language),
                            style = MaterialTheme.typography.titleMedium,
                            color = PrayerReminderColors.Ink,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageChoiceChip(
                            label = "English",
                            isSelected = currentLangTag == "en",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                viewModel.setAppLanguage("en")
                            }
                        )
                        LanguageChoiceChip(
                            label = "العربية",
                            isSelected = currentLangTag == "ar",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
                                viewModel.setAppLanguage("ar")
                            }
                        )
                        LanguageChoiceChip(
                            label = "اردو",
                            isSelected = currentLangTag == "ur",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ur"))
                                viewModel.setAppLanguage("ur")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Timetable Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = PrayerReminderColors.GoldText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.timetable),
                            style = MaterialTheme.typography.titleMedium,
                            color = PrayerReminderColors.Ink,
                            fontWeight = FontWeight.Bold
                        )
                    }

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
                        } else {
                            0
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val statusLine = if (daysLeft > 0) {
                            "${currentTable.hijriMonthName} ${currentTable.hijriYear} · ${stringResource(R.string.days_left, daysLeft)}"
                        } else {
                            "Built-in Bhatkal schedule active · ${currentTable.hijriMonthName} ${currentTable.hijriYear} ready"
                        }
                        Text(
                            text = statusLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrayerReminderColors.Muted
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = PrayerReminderColors.Line)
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsNavigationRow(
                        icon = Icons.Default.Upload,
                        title = stringResource(R.string.import_new),
                        onClick = onImportNewTimetable
                    )

                    SettingsNavigationRow(
                        icon = Icons.Default.DateRange,
                        title = stringResource(R.string.all_timetables),
                        onClick = onNavigateToTimetables
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Navigation Cards: Jamaat times & Reminders & Voice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    SettingsItemRow(
                        icon = Icons.Default.AccessTime,
                        title = stringResource(R.string.jamaat_times),
                        subtitle = stringResource(R.string.jamaat_intro),
                        onClick = onNavigateToJamaat
                    )

                    HorizontalDivider(
                        color = PrayerReminderColors.Line,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    val voiceSummary = if (voiceOn) {
                        stringResource(R.string.voice_line, "${beforeAzanMin} min", "${beforeJamaatMin} min")
                    } else {
                        stringResource(R.string.voice_off_line)
                    }
                    SettingsItemRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = stringResource(R.string.reminders_voice),
                        subtitle = voiceSummary,
                        onClick = onNavigateToRemindersVoice
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Widget & Reliability
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    SettingsItemRow(
                        icon = Icons.Default.Widgets,
                        title = stringResource(R.string.widget),
                        subtitle = stringResource(R.string.add_widget),
                        onClick = { showWidgetPickerDialog = true }
                    )

                    HorizontalDivider(
                        color = PrayerReminderColors.Line,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsItemRow(
                        icon = Icons.Default.CheckCircle,
                        title = stringResource(R.string.reliability),
                        subtitle = stringResource(R.string.privacy_line),
                        onClick = onNavigateToReliability
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. About Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrayerReminderColors.GoldText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.about),
                            style = MaterialTheme.typography.titleMedium,
                            color = PrayerReminderColors.Ink,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${stringResource(R.string.app_name)} v1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrayerReminderColors.Ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.privacy_line),
                        style = MaterialTheme.typography.bodySmall,
                        color = PrayerReminderColors.Muted
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LanguageChoiceChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PrayerReminderColors.Ground else PrayerReminderColors.Ivory,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, PrayerReminderColors.Line)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) PrayerReminderColors.Ivory else PrayerReminderColors.Ink,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrayerReminderColors.Ink,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = PrayerReminderColors.Ink,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = PrayerReminderColors.Muted,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PrayerReminderColors.Ivory, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrayerReminderColors.GoldText,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrayerReminderColors.Ink
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PrayerReminderColors.Muted
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = PrayerReminderColors.Muted,
            modifier = Modifier.size(16.dp)
        )
    }
}
