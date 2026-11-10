package com.syedmsidsm.prayerreminder.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RemindersVoiceSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val voiceEnabled by viewModel.voiceEnabled.collectAsState()
    val voiceLanguage by viewModel.voiceLanguage.collectAsState()
    val voiceGender by viewModel.voiceGender.collectAsState()
    val voiceChime by viewModel.voiceChime.collectAsState()
    val voiceOnSilent by viewModel.voiceOnSilent.collectAsState()

    val beforeAzanMin by viewModel.remBeforeAzanMin.collectAsState()
    val jumuahBeforeAzanMin by viewModel.remJumuahBeforeAzanMin.collectAsState()
    val atAzanEnabled by viewModel.remAtAzan.collectAsState()
    val beforeJamaatMin by viewModel.remBeforeJamaatMin.collectAsState()

    val reminderMatrix by viewModel.reminderMatrix.collectAsState()
    val playingKind by viewModel.audioPlayer.playingKind.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reminders_voice),
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
            // Master Toggle: Spoken reminders
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.voice_enabled),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrayerReminderColors.Ink
                        )
                    }
                    Switch(
                        checked = voiceEnabled,
                        onCheckedChange = { viewModel.setVoiceEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrayerReminderColors.PrimaryBtnText,
                            checkedTrackColor = PrayerReminderColors.PrimaryBtnBg,
                            uncheckedThumbColor = PrayerReminderColors.Muted,
                            uncheckedTrackColor = PrayerReminderColors.Line
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice Language & Gender
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.voice_language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.Ink
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoiceOptionChip(
                            label = "اردو",
                            isSelected = voiceLanguage == "ur",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceLanguage("ur") }
                        )
                        VoiceOptionChip(
                            label = "العربية",
                            isSelected = voiceLanguage == "ar",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceLanguage("ar") }
                        )
                        VoiceOptionChip(
                            label = "English",
                            isSelected = voiceLanguage == "en",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceLanguage("en") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = PrayerReminderColors.Line)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.voice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.Ink
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoiceOptionChip(
                            label = stringResource(R.string.voice_male),
                            isSelected = voiceGender.lowercase() == "male",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceGender("male") }
                        )
                        VoiceOptionChip(
                            label = stringResource(R.string.voice_female),
                            isSelected = voiceGender.lowercase() == "female",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceGender("female") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder Timing Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Before Azan
                    TimingSectionHeader(
                        title = stringResource(R.string.before_azan),
                        isPlaying = playingKind == "before_azan",
                        onPlay = { viewModel.playSample("before_azan", beforeAzanMin) },
                        onStop = { viewModel.stopSample() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val beforeAzanOptions = listOf(0, 5, 10, 15, 20, 30)
                        for (min in beforeAzanOptions) {
                            val label = if (min == 0) stringResource(R.string.off) else stringResource(R.string.min_short, min)
                            ChoiceChip(
                                label = label,
                                isSelected = beforeAzanMin == min,
                                onClick = { viewModel.setRemBeforeAzanMin(min) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = PrayerReminderColors.Line)
                    Spacer(modifier = Modifier.height(16.dp))

                    // At Azan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.at_azan),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrayerReminderColors.Ink
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SamplePlayButton(
                                isPlaying = playingKind == "at_azan",
                                onPlay = { viewModel.playSample("at_azan") },
                                onStop = { viewModel.stopSample() }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = atAzanEnabled,
                                onCheckedChange = { viewModel.setRemAtAzan(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PrayerReminderColors.PrimaryBtnText,
                                    checkedTrackColor = PrayerReminderColors.PrimaryBtnBg,
                                    uncheckedThumbColor = PrayerReminderColors.Muted,
                                    uncheckedTrackColor = PrayerReminderColors.Line
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = PrayerReminderColors.Line)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Before Jamaat
                    TimingSectionHeader(
                        title = stringResource(R.string.before_jamaat),
                        isPlaying = playingKind == "before_jamaat",
                        onPlay = { viewModel.playSample("before_jamaat", beforeJamaatMin) },
                        onStop = { viewModel.stopSample() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val beforeJamaatOptions = listOf(0, 2, 5, 10, 15)
                        for (min in beforeJamaatOptions) {
                            val label = if (min == 0) stringResource(R.string.off) else stringResource(R.string.min_short, min)
                            ChoiceChip(
                                label = label,
                                isSelected = beforeJamaatMin == min,
                                onClick = { viewModel.setRemBeforeJamaatMin(min) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = PrayerReminderColors.Line)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Jumu'ah Before Azan
                    TimingSectionHeader(
                        title = stringResource(R.string.jumuah_before_azan),
                        isPlaying = playingKind == "jumuah_before_azan",
                        onPlay = { viewModel.playSample("jumuah_before_azan", jumuahBeforeAzanMin) },
                        onStop = { viewModel.stopSample() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val jumuahOptions = listOf(0, 5, 10, 15, 20, 30)
                        for (min in jumuahOptions) {
                            val label = if (min == 0) stringResource(R.string.off) else stringResource(R.string.min_short, min)
                            ChoiceChip(
                                label = label,
                                isSelected = jumuahBeforeAzanMin == min,
                                onClick = { viewModel.setRemJumuahBeforeAzanMin(min) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Matrix: Which reminders for each prayer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.which_reminders),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.Ink
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Column headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "",
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = stringResource(R.string.before_azan),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrayerReminderColors.Muted,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.at_azan),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrayerReminderColors.Muted,
                            modifier = Modifier.weight(0.9f)
                        )
                        Text(
                            text = stringResource(R.string.before_jamaat),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrayerReminderColors.Muted,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = PrayerReminderColors.Line, modifier = Modifier.padding(vertical = 8.dp))

                    val prayerEntries = listOf(
                        Pair("FAJR", R.string.prayer_fajr),
                        Pair("DHUHR", R.string.prayer_dhuhr),
                        Pair("ASR", R.string.prayer_asr),
                        Pair("MAGHRIB", R.string.prayer_maghrib),
                        Pair("ISHA", R.string.prayer_isha),
                        Pair("JUMUAH", R.string.prayer_jumuah)
                    )

                    for ((key, nameRes) in prayerEntries) {
                        val beforeAzanActive = reminderMatrix[Pair(key, "BEFORE_AZAN")] ?: true
                        val atAzanActive = reminderMatrix[Pair(key, "AT_AZAN")] ?: true
                        val beforeJamaatActive = reminderMatrix[Pair(key, "BEFORE_JAMAAT")] ?: true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(nameRes),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrayerReminderColors.Ink,
                                modifier = Modifier.weight(1.2f)
                            )

                            // Checkbox BEFORE_AZAN
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                Checkbox(
                                    checked = beforeAzanActive,
                                    onCheckedChange = { viewModel.setReminderMatrixEntry(key, "BEFORE_AZAN", it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrayerReminderColors.PrimaryBtnBg,
                                        checkmarkColor = PrayerReminderColors.PrimaryBtnText,
                                        uncheckedColor = PrayerReminderColors.Muted
                                    )
                                )
                            }

                            // Checkbox AT_AZAN
                            Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.CenterStart) {
                                Checkbox(
                                    checked = atAzanActive,
                                    onCheckedChange = { viewModel.setReminderMatrixEntry(key, "AT_AZAN", it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrayerReminderColors.PrimaryBtnBg,
                                        checkmarkColor = PrayerReminderColors.PrimaryBtnText,
                                        uncheckedColor = PrayerReminderColors.Muted
                                    )
                                )
                            }

                            // Checkbox BEFORE_JAMAAT
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                Checkbox(
                                    checked = beforeJamaatActive,
                                    onCheckedChange = { viewModel.setReminderMatrixEntry(key, "BEFORE_JAMAAT", it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrayerReminderColors.PrimaryBtnBg,
                                        checkmarkColor = PrayerReminderColors.PrimaryBtnText,
                                        uncheckedColor = PrayerReminderColors.Muted
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Other voice options: Speak on silent & Chime
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Speak on silent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.speak_on_silent),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrayerReminderColors.Ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.speak_on_silent_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = PrayerReminderColors.Muted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = voiceOnSilent,
                            onCheckedChange = { viewModel.setVoiceOnSilent(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrayerReminderColors.PrimaryBtnText,
                                checkedTrackColor = PrayerReminderColors.PrimaryBtnBg,
                                uncheckedThumbColor = PrayerReminderColors.Muted,
                                uncheckedTrackColor = PrayerReminderColors.Line
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = PrayerReminderColors.Line)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Chime
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.chime),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrayerReminderColors.Ink
                            )
                        }
                        Switch(
                            checked = voiceChime,
                            onCheckedChange = { viewModel.setVoiceChime(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrayerReminderColors.PrimaryBtnText,
                                checkedTrackColor = PrayerReminderColors.PrimaryBtnBg,
                                uncheckedThumbColor = PrayerReminderColors.Muted,
                                uncheckedTrackColor = PrayerReminderColors.Line
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TimingSectionHeader(
    title: String,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        SamplePlayButton(
            isPlaying = isPlaying,
            onPlay = onPlay,
            onStop = onStop
        )
    }
}

@Composable
private fun SamplePlayButton(
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { if (isPlaying) onStop() else onPlay() },
        shape = RoundedCornerShape(8.dp),
        color = PrayerReminderColors.Ivory,
        border = BorderStroke(1.dp, PrayerReminderColors.Line)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.play_sample),
                tint = PrayerReminderColors.GoldText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.play_sample),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrayerReminderColors.Ink
            )
        }
    }
}

@Composable
private fun VoiceOptionChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PrayerReminderColors.PrimaryBtnBg else PrayerReminderColors.Ivory,
        border = if (isSelected) null else BorderStroke(1.dp, PrayerReminderColors.Line)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) PrayerReminderColors.PrimaryBtnText else PrayerReminderColors.Ink,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(36.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PrayerReminderColors.PrimaryBtnBg else PrayerReminderColors.Ivory,
        border = if (isSelected) null else BorderStroke(1.dp, PrayerReminderColors.Line)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) PrayerReminderColors.PrimaryBtnText else PrayerReminderColors.Ink,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}
