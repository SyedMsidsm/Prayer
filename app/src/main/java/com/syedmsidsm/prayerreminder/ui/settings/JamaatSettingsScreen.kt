package com.syedmsidsm.prayerreminder.ui.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import java.time.LocalTime

data class JamaatPrayerItem(
    val key: String,
    val prayer: Prayer?,
    val nameRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamaatSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val rules by viewModel.jamaatRules.collectAsState()
    val schedule by viewModel.todaySchedule.collectAsState()

    val amMarker = stringResource(R.string.am_marker)
    val pmMarker = stringResource(R.string.pm_marker)

    val items = listOf(
        JamaatPrayerItem("FAJR", Prayer.FAJR, R.string.prayer_fajr),
        JamaatPrayerItem("DHUHR", Prayer.DHUHR, R.string.prayer_dhuhr),
        JamaatPrayerItem("ASR", Prayer.ASR, R.string.prayer_asr),
        JamaatPrayerItem("MAGHRIB", Prayer.MAGHRIB, R.string.prayer_maghrib),
        JamaatPrayerItem("ISHA", Prayer.ISHA, R.string.prayer_isha),
        JamaatPrayerItem("JUMUAH", null, R.string.prayer_jumuah)
    )

    var timePickerTargetKey by remember { mutableStateOf<String?>(null) }
    var timePickerInitialTime by remember { mutableStateOf(LocalTime.of(12, 0)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.jamaat_times),
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
            // Intro text
            Text(
                text = stringResource(R.string.jamaat_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = PrayerReminderColors.Muted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prayer cards
            for (item in items) {
                val currentRule = rules[item.key] ?: JamaatRule.AfterAzan(15)

                // Today's Azan and Jamaat calculation for display
                val azan = if (item.prayer != null) {
                    schedule?.azan?.get(item.prayer)
                } else {
                    // For Jumu'ah, azan is Friday Dhuhr azan
                    schedule?.azan?.get(Prayer.DHUHR)
                }

                val jamaat = if (azan != null) {
                    when (currentRule) {
                        is JamaatRule.AfterAzan -> azan.plusMinutes(currentRule.minutes.toLong())
                        is JamaatRule.Fixed -> {
                            if (!currentRule.time.isAfter(azan)) azan.plusMinutes(15) else currentRule.time
                        }
                    }
                } else {
                    null
                }

                val isFixedBeforeAzan = if (azan != null && currentRule is JamaatRule.Fixed) {
                    !currentRule.time.isAfter(azan)
                } else {
                    false
                }

                JamaatPrayerCard(
                    prayerName = stringResource(item.nameRes),
                    rule = currentRule,
                    azanTime = azan,
                    jamaatTime = jamaat,
                    isFixedBeforeAzan = isFixedBeforeAzan,
                    amMarker = amMarker,
                    pmMarker = pmMarker,
                    onRuleChange = { newRule -> viewModel.setJamaatRule(item.key, newRule) },
                    onOpenTimePicker = {
                        val init = (currentRule as? JamaatRule.Fixed)?.time ?: azan?.plusMinutes(15) ?: LocalTime.of(12, 0)
                        timePickerInitialTime = init
                        timePickerTargetKey = item.key
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reset all to 15 minutes button
            OutlinedButton(
                onClick = { viewModel.resetAllJamaatTo15Minutes() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrayerReminderColors.Ink),
                border = BorderStroke(1.dp, PrayerReminderColors.Line)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = PrayerReminderColors.Ink,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.jamaat_reset),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // TimePicker Dialog for Fixed time
    if (timePickerTargetKey != null) {
        val pickerState = rememberTimePickerState(
            initialHour = timePickerInitialTime.hour,
            initialMinute = timePickerInitialTime.minute,
            is24Hour = false
        )

        Dialog(onDismissRequest = { timePickerTargetKey = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PrayerReminderColors.Surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.jamaat_fixed),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.Ink
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TimePicker(state = pickerState)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { timePickerTargetKey = null }) {
                            Text(stringResource(R.string.cancel), color = PrayerReminderColors.Muted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val selectedTime = LocalTime.of(pickerState.hour, pickerState.minute)
                                timePickerTargetKey?.let { key ->
                                    viewModel.setJamaatRule(key, JamaatRule.Fixed(selectedTime))
                                }
                                timePickerTargetKey = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrayerReminderColors.Ground)
                        ) {
                            Text(stringResource(R.string.done), color = PrayerReminderColors.Ivory)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JamaatPrayerCard(
    prayerName: String,
    rule: JamaatRule,
    azanTime: LocalTime?,
    jamaatTime: LocalTime?,
    isFixedBeforeAzan: Boolean,
    amMarker: String,
    pmMarker: String,
    onRuleChange: (JamaatRule) -> Unit,
    onOpenTimePicker: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Prayer Name + Today's Azan -> Jamaat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prayerName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrayerReminderColors.Ink
                )

                if (azanTime != null && jamaatTime != null) {
                    val azanFormatted = TimeFormatter.format12Hour(azanTime, amMarker, pmMarker)
                    val jamaatFormatted = TimeFormatter.format12Hour(jamaatTime, amMarker, pmMarker)
                    Text(
                        text = stringResource(R.string.jamaat_today, azanFormatted, jamaatFormatted),
                        style = MaterialTheme.typography.bodySmall,
                        color = PrayerReminderColors.Muted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Toggle: After azan | Fixed time
            val isAfter = rule is JamaatRule.AfterAzan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrayerReminderColors.Ivory, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clickable {
                            if (!isAfter) {
                                onRuleChange(JamaatRule.AfterAzan(15))
                            }
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAfter) PrayerReminderColors.Ground else PrayerReminderColors.Ivory
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.jamaat_after),
                            color = if (isAfter) PrayerReminderColors.Ivory else PrayerReminderColors.Ink,
                            fontWeight = if (isAfter) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clickable {
                            if (isAfter) {
                                val defaultFixed = azanTime?.plusMinutes(15) ?: LocalTime.of(13, 15)
                                onRuleChange(JamaatRule.Fixed(defaultFixed))
                            }
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = if (!isAfter) PrayerReminderColors.Ground else PrayerReminderColors.Ivory
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.jamaat_fixed),
                            color = if (!isAfter) PrayerReminderColors.Ivory else PrayerReminderColors.Ink,
                            fontWeight = if (!isAfter) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Control based on mode
            when (rule) {
                is JamaatRule.AfterAzan -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.jamaat_after_value, rule.minutes),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PrayerReminderColors.Ink
                        )

                        // Stepper: - and +
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrayerReminderColors.Ivory)
                                    .clickable {
                                        if (rule.minutes > 1) {
                                            onRuleChange(JamaatRule.AfterAzan(rule.minutes - 1))
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease minutes",
                                    tint = if (rule.minutes > 1) PrayerReminderColors.Ink else PrayerReminderColors.Muted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrayerReminderColors.Ivory)
                                    .clickable {
                                        if (rule.minutes < 60) {
                                            onRuleChange(JamaatRule.AfterAzan(rule.minutes + 1))
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase minutes",
                                    tint = if (rule.minutes < 60) PrayerReminderColors.Ink else PrayerReminderColors.Muted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                is JamaatRule.Fixed -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.jamaat_fixed_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = PrayerReminderColors.Muted,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onOpenTimePicker() },
                                color = PrayerReminderColors.Ivory,
                                border = BorderStroke(1.dp, PrayerReminderColors.Line)
                            ) {
                                Text(
                                    text = TimeFormatter.format12Hour(rule.time, amMarker, pmMarker),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PrayerReminderColors.GoldText,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Sanity check warning if fixed <= azan
                        if (isFixedBeforeAzan) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = PrayerReminderColors.WarnBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = PrayerReminderColors.WarnText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.jamaat_fixed_before_azan),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrayerReminderColors.WarnText,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
