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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.ui.components.MosqueBackdrop
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import java.time.LocalTime

private val AmberAccent = Color(0xFFFBBF24)
private val AmberTextDark = Color(0xFF020617)
private val AmberGlowText = Color(0xFFFCD34D)
private val SanctuaryDarkBg = Color(0xFF03080C)
private val SanctuaryLightBg = Color(0xFFF6F1E6)

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

    val isDark = PrayerReminderColors.isDark
    val surfaceBg = if (isDark) SanctuaryDarkBg else SanctuaryLightBg
    val glassCardBg = if (isDark) Color(0xFF0B161E).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.94f)
    val glassBorder = if (isDark) Color.White.copy(alpha = 0.11f) else Color(0xFFE4DCC7)
    val chipUnselectedBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3EFE4)
    val textPrimary = if (isDark) Color.White else Color(0xFF14262A)
    val textMuted = if (isDark) Color(0xFFA8A29E) else Color(0xFF5E6D70)
    val selectedGoldText = if (isDark) AmberGlowText else Color(0xFF92400E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
    ) {
        MosqueBackdrop(
            activeSky = Prayer.MAGHRIB,
            surfaceFadeColor = surfaceBg
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Sanctuary Top Bar
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
                    text = stringResource(R.string.jamaat_times),
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
                    .padding(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Description Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = glassCardBg,
                    border = BorderStroke(1.dp, glassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.jamaat_intro),
                        color = textMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                // Prayer cards
                for (item in items) {
                    val currentRule = rules[item.key] ?: JamaatRule.AfterAzan(15)

                    val azan = if (item.prayer != null) {
                        schedule?.azan?.get(item.prayer)
                    } else {
                        schedule?.azan?.get(Prayer.DHUHR)
                    }

                    val jamaat = if (azan != null) {
                        when (currentRule) {
                            is JamaatRule.AfterAzan -> azan.plusMinutes(currentRule.minutes.toLong())
                            is JamaatRule.Fixed -> {
                                if (!currentRule.time.isAfter(azan)) azan.plusMinutes(15) else currentRule.time
                            }
                        }
                    } else null

                    val isFixedBeforeAzan = if (azan != null && currentRule is JamaatRule.Fixed) {
                        !currentRule.time.isAfter(azan)
                    } else false

                    SanctuaryJamaatPrayerCard(
                        prayerName = stringResource(item.nameRes),
                        rule = currentRule,
                        azanTime = azan,
                        jamaatTime = jamaat,
                        isFixedBeforeAzan = isFixedBeforeAzan,
                        amMarker = amMarker,
                        pmMarker = pmMarker,
                        isDark = isDark,
                        cardBg = glassCardBg,
                        border = glassBorder,
                        chipBg = chipUnselectedBg,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        selectedGoldText = selectedGoldText,
                        onRuleChange = { newRule -> viewModel.setJamaatRule(item.key, newRule) },
                        onOpenTimePicker = {
                            val init = (currentRule as? JamaatRule.Fixed)?.time ?: azan?.plusMinutes(15) ?: LocalTime.of(12, 0)
                            timePickerInitialTime = init
                            timePickerTargetKey = item.key
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Reset all to 15 minutes button
                OutlinedButton(
                    onClick = { viewModel.resetAllJamaatTo15Minutes() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, glassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.jamaat_reset),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
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
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF0F1E28) else Color.White,
                border = BorderStroke(1.dp, glassBorder),
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
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TimePicker(
                        state = pickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = if (isDark) Color(0xFF142633) else Color(0xFFF3EFE4),
                            selectorColor = AmberAccent,
                            periodSelectorBorderColor = glassBorder,
                            periodSelectorSelectedContainerColor = AmberAccent,
                            periodSelectorSelectedContentColor = AmberTextDark,
                            periodSelectorUnselectedContentColor = textMuted,
                            timeSelectorSelectedContainerColor = AmberAccent,
                            timeSelectorSelectedContentColor = AmberTextDark,
                            timeSelectorUnselectedContainerColor = chipUnselectedBg,
                            timeSelectorUnselectedContentColor = textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { timePickerTargetKey = null }) {
                            Text(stringResource(R.string.cancel), color = textMuted)
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
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberAccent,
                                contentColor = AmberTextDark
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.done),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SanctuaryJamaatPrayerCard(
    prayerName: String,
    rule: JamaatRule,
    azanTime: LocalTime?,
    jamaatTime: LocalTime?,
    isFixedBeforeAzan: Boolean,
    amMarker: String,
    pmMarker: String,
    isDark: Boolean,
    cardBg: Color,
    border: Color,
    chipBg: Color,
    textPrimary: Color,
    textMuted: Color,
    selectedGoldText: Color,
    onRuleChange: (JamaatRule) -> Unit,
    onOpenTimePicker: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(1.dp, border)
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                if (azanTime != null && jamaatTime != null) {
                    val azanFormatted = TimeFormatter.format12Hour(azanTime, amMarker, pmMarker)
                    val jamaatFormatted = TimeFormatter.format12Hour(jamaatTime, amMarker, pmMarker)
                    Text(
                        text = stringResource(R.string.jamaat_today, azanFormatted, jamaatFormatted),
                        fontSize = 11.sp,
                        color = selectedGoldText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Toggle: After azan | Fixed time
            val isAfter = rule is JamaatRule.AfterAzan
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = chipBg,
                border = BorderStroke(1.dp, border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    // After azan tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isAfter) AmberAccent else Color.Transparent)
                            .clickable {
                                if (!isAfter) onRuleChange(JamaatRule.AfterAzan(15))
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.jamaat_after),
                            color = if (isAfter) AmberTextDark else textMuted,
                            fontWeight = if (isAfter) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                    // Fixed time tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (!isAfter) AmberAccent else Color.Transparent)
                            .clickable {
                                if (isAfter) {
                                    val defaultFixed = azanTime?.plusMinutes(15) ?: LocalTime.of(13, 15)
                                    onRuleChange(JamaatRule.Fixed(defaultFixed))
                                }
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.jamaat_fixed),
                            color = if (!isAfter) AmberTextDark else textMuted,
                            fontWeight = if (!isAfter) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )

                        // Steppers - and +
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(chipBg)
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
                                    tint = if (rule.minutes > 1) AmberAccent else textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(chipBg)
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
                                    tint = if (rule.minutes < 60) AmberAccent else textMuted,
                                    modifier = Modifier.size(18.dp)
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
                                fontSize = 11.sp,
                                color = textMuted,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onOpenTimePicker() },
                                color = chipBg,
                                border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = TimeFormatter.format12Hour(rule.time, amMarker, pmMarker),
                                    fontSize = 14.sp,
                                    color = selectedGoldText,
                                    fontFamily = FontFamily.Monospace,
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
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.jamaat_fixed_before_azan),
                                        fontSize = 11.sp,
                                        color = Color(0xFFFCA5A5),
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
