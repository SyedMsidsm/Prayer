package com.syedmsidsm.prayerreminder.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.ui.components.MosqueBackdrop
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors

private val AmberAccent = Color(0xFFFBBF24)
private val AmberTextDark = Color(0xFF020617)
private val AmberGlowText = Color(0xFFFCD34D)
private val SanctuaryDarkBg = Color(0xFF03080C)
private val SanctuaryLightBg = Color(0xFFF6F1E6)

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
    val beforeAzanOffsets by viewModel.remBeforeAzanOffsets.collectAsState()
    val jumuahBeforeAzanMin by viewModel.remJumuahBeforeAzanMin.collectAsState()
    val jumuahBeforeAzanOffsets by viewModel.remJumuahBeforeAzanOffsets.collectAsState()
    val atAzanEnabled by viewModel.remAtAzan.collectAsState()
    val beforeJamaatMin by viewModel.remBeforeJamaatMin.collectAsState()
    val beforeJamaatOffsets by viewModel.remBeforeJamaatOffsets.collectAsState()
    val afterPrayerOffsets by viewModel.remAfterPrayerOffsets.collectAsState()

    var customDialogTarget by remember { mutableStateOf<String?>(null) }
    var customMinuteInput by remember { mutableStateOf("") }

    val reminderMatrix by viewModel.reminderMatrix.collectAsState()
    val currentTrack by viewModel.audioPlayer.currentTrack.collectAsState()
    val playingKind by viewModel.audioPlayer.playingKind.collectAsState()

    val atAzanType by viewModel.atAzanType.collectAsState()
    val azanReciter by viewModel.azanReciter.collectAsState()
    val azanLength by viewModel.azanLength.collectAsState()
    val playDuaAfterAzan by viewModel.playDuaAfterAzan.collectAsState()

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
            activeSky = Prayer.ISHA,
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
                    text = stringResource(R.string.reminders_voice),
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
                    .padding(top = 4.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Master Toggle: Spoken reminders
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.voice_enabled),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Spoken countdowns, Azan audio & Jamaat reminders",
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                        Switch(
                            checked = voiceEnabled,
                            onCheckedChange = { viewModel.setVoiceEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmberTextDark,
                                checkedTrackColor = AmberAccent,
                                uncheckedThumbColor = textMuted,
                                uncheckedTrackColor = chipUnselectedBg
                            )
                        )
                    }
                }

                // Voice Language & Gender
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Text(
                        text = stringResource(R.string.voice_language),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoiceOptionChip(
                            label = "اردو",
                            isSelected = voiceLanguage == "ur",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceLanguage("ur") }
                        )
                        VoiceOptionChip(
                            label = "العربية",
                            isSelected = voiceLanguage == "ar",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceLanguage("ar") }
                        )
                        VoiceOptionChip(
                            label = "English",
                            isSelected = voiceLanguage == "en",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceLanguage("en") }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.voice),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoiceOptionChip(
                            label = stringResource(R.string.voice_male),
                            isSelected = voiceGender.lowercase() == "male",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceGender("male") }
                        )
                        VoiceOptionChip(
                            label = stringResource(R.string.voice_female),
                            isSelected = voiceGender.lowercase() == "female",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setVoiceGender("female") }
                        )
                    }
                }

                // Reminder Timings Card (Before Azan, At Azan, Before Jamaat, After Prayer, Jumu'ah)
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    // Before Azan
                    TimingSectionHeader(
                        title = stringResource(R.string.before_azan),
                        isPlaying = playingKind == "before_azan",
                        textColor = textPrimary,
                        chipBg = chipUnselectedBg,
                        border = glassBorder,
                        onPlay = { viewModel.playSample("before_azan", beforeAzanOffsets.firstOrNull() ?: beforeAzanMin) },
                        onStop = { viewModel.stopSample() }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.multiple_reminders_hint),
                        fontSize = 11.sp,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val beforeAzanOptions = (listOf(0, 5, 10, 15, 20, 25, 30, 45, 60) + beforeAzanOffsets).distinct().sorted()
                        for (min in beforeAzanOptions) {
                            val label = if (min == 0) stringResource(R.string.off) else stringResource(R.string.min_short, min)
                            val isSel = if (min == 0) beforeAzanOffsets.isEmpty() else beforeAzanOffsets.contains(min)
                            SanctuaryChoiceChip(
                                label = label,
                                isSelected = isSel,
                                unselectedBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                onClick = { viewModel.toggleBeforeAzanOffset(min) }
                            )
                        }
                        SanctuaryChoiceChip(
                            label = stringResource(R.string.custom_chip_label),
                            isSelected = false,
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = selectedGoldText,
                            onClick = {
                                customDialogTarget = "before_azan"
                                customMinuteInput = ""
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // At Azan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.at_azan),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SamplePlayButton(
                                isPlaying = playingKind == "at_azan",
                                chipBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                onPlay = { viewModel.playSample("at_azan") },
                                onStop = { viewModel.stopSample() }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Switch(
                                checked = atAzanEnabled,
                                onCheckedChange = { viewModel.setRemAtAzan(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AmberTextDark,
                                    checkedTrackColor = AmberAccent,
                                    uncheckedThumbColor = textMuted,
                                    uncheckedTrackColor = chipUnselectedBg
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Before Jamaat
                    TimingSectionHeader(
                        title = stringResource(R.string.before_jamaat),
                        isPlaying = playingKind == "before_jamaat",
                        textColor = textPrimary,
                        chipBg = chipUnselectedBg,
                        border = glassBorder,
                        onPlay = { viewModel.playSample("before_jamaat", beforeJamaatOffsets.firstOrNull() ?: beforeJamaatMin) },
                        onStop = { viewModel.stopSample() }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.multiple_reminders_hint),
                        fontSize = 11.sp,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val beforeJamaatOptions = (listOf(0, 2, 5, 10, 15, 20, 25, 30) + beforeJamaatOffsets).distinct().sorted()
                        for (min in beforeJamaatOptions) {
                            val label = if (min == 0) stringResource(R.string.off) else stringResource(R.string.min_short, min)
                            val isSel = if (min == 0) beforeJamaatOffsets.isEmpty() else beforeJamaatOffsets.contains(min)
                            SanctuaryChoiceChip(
                                label = label,
                                isSelected = isSel,
                                unselectedBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                onClick = { viewModel.toggleBeforeJamaatOffset(min) }
                            )
                        }
                        SanctuaryChoiceChip(
                            label = stringResource(R.string.custom_chip_label),
                            isSelected = false,
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = selectedGoldText,
                            onClick = {
                                customDialogTarget = "before_jamaat"
                                customMinuteInput = ""
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // After Prayer / Jamaat
                    TimingSectionHeader(
                        title = stringResource(R.string.after_prayer),
                        isPlaying = false,
                        textColor = textPrimary,
                        chipBg = chipUnselectedBg,
                        border = glassBorder,
                        onPlay = { viewModel.playSample("before_azan", afterPrayerOffsets.firstOrNull() ?: 10) },
                        onStop = { viewModel.stopSample() }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.multiple_reminders_hint),
                        fontSize = 11.sp,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val afterOptions = (listOf(0, 5, 10, 15, 20, 30) + afterPrayerOffsets).distinct().sorted()
                        for (min in afterOptions) {
                            val label = if (min == 0) stringResource(R.string.off) else stringResource(R.string.min_short, min)
                            val isSel = if (min == 0) afterPrayerOffsets.isEmpty() else afterPrayerOffsets.contains(min)
                            SanctuaryChoiceChip(
                                label = label,
                                isSelected = isSel,
                                unselectedBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                onClick = { viewModel.toggleAfterPrayerOffset(min) }
                            )
                        }
                        SanctuaryChoiceChip(
                            label = stringResource(R.string.custom_chip_label),
                            isSelected = false,
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = selectedGoldText,
                            onClick = {
                                customDialogTarget = "after_prayer"
                                customMinuteInput = ""
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Jumu'ah Before Azan
                    TimingSectionHeader(
                        title = stringResource(R.string.jumuah_before_azan),
                        isPlaying = playingKind == "jumuah_before_azan",
                        textColor = textPrimary,
                        chipBg = chipUnselectedBg,
                        border = glassBorder,
                        onPlay = { viewModel.playSample("jumuah_before_azan", jumuahBeforeAzanOffsets.firstOrNull() ?: jumuahBeforeAzanMin) },
                        onStop = { viewModel.stopSample() }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.multiple_reminders_hint),
                        fontSize = 11.sp,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val jumuahOptions = (listOf(0, 15, 30, 45, 60) + jumuahBeforeAzanOffsets).distinct().sorted()
                        for (min in jumuahOptions) {
                            val label = if (min == 0) stringResource(R.string.off) else stringResource(R.string.min_short, min)
                            val isSel = if (min == 0) jumuahBeforeAzanOffsets.isEmpty() else jumuahBeforeAzanOffsets.contains(min)
                            SanctuaryChoiceChip(
                                label = label,
                                isSelected = isSel,
                                unselectedBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                onClick = { viewModel.toggleJumuahBeforeAzanOffset(min) }
                            )
                        }
                        SanctuaryChoiceChip(
                            label = stringResource(R.string.custom_chip_label),
                            isSelected = false,
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = selectedGoldText,
                            onClick = {
                                customDialogTarget = "jumuah"
                                customMinuteInput = ""
                            }
                        )
                    }
                }

                // Azan Audio & Reciters Card
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Text(
                        text = stringResource(R.string.azan_sound),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.azan_sound_desc),
                        fontSize = 11.sp,
                        color = textMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode options: Both, Azan only, Speech only
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AzanModeOptionRow(
                            label = stringResource(R.string.azan_type_both),
                            isSelected = atAzanType == "both",
                            chipBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            onClick = { viewModel.setAtAzanType("both") }
                        )
                        AzanModeOptionRow(
                            label = stringResource(R.string.azan_type_azan_only),
                            isSelected = atAzanType == "azan_only",
                            chipBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            onClick = { viewModel.setAtAzanType("azan_only") }
                        )
                        AzanModeOptionRow(
                            label = stringResource(R.string.azan_type_speech_only),
                            isSelected = atAzanType == "speech_only",
                            chipBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            onClick = { viewModel.setAtAzanType("speech_only") }
                        )
                    }

                    if (atAzanType != "speech_only") {
                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = glassBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = stringResource(R.string.reciter),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3 Reciters
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Ali Ibn Ahmad Mala (Saudi Arabia)
                            SanctuaryReciterCard(
                                name = stringResource(R.string.reciter_ali_mala),
                                country = stringResource(R.string.reciter_ali_mala_country),
                                avatarDrawableRes = R.drawable.reciter_ali_mala,
                                flagDrawableRes = R.drawable.ic_flag_saudi,
                                isSelected = azanReciter == "ali_mala",
                                isPlaying = currentTrack == "azan_ali_mala",
                                chipBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                onSelect = { viewModel.setAzanReciter("ali_mala") },
                                onPlaySample = { viewModel.playAzanSample("ali_mala") },
                                onStopSample = { viewModel.stopSample() }
                            )

                            // NurDin Hamza Al Maghriby (Morocco)
                            SanctuaryReciterCard(
                                name = stringResource(R.string.reciter_nurdin_hamza),
                                country = stringResource(R.string.reciter_nurdin_hamza_country),
                                avatarDrawableRes = R.drawable.reciter_nurdin_hamza,
                                flagDrawableRes = R.drawable.ic_flag_morocco,
                                isSelected = azanReciter == "nurdin_hamza",
                                isPlaying = currentTrack == "azan_nurdin_hamza",
                                chipBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                onSelect = { viewModel.setAzanReciter("nurdin_hamza") },
                                onPlaySample = { viewModel.playAzanSample("nurdin_hamza") },
                                onStopSample = { viewModel.stopSample() }
                            )

                            // Najee Qazaz (Palestine)
                            SanctuaryReciterCard(
                                name = stringResource(R.string.reciter_najee_qazaz),
                                country = stringResource(R.string.reciter_najee_qazaz_country),
                                avatarDrawableRes = R.drawable.ic_default_avatar,
                                flagDrawableRes = R.drawable.ic_flag_palestine,
                                isSelected = azanReciter == "najee_qazaz",
                                isPlaying = currentTrack == "azan_najee_qazaz",
                                chipBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                onSelect = { viewModel.setAzanReciter("najee_qazaz") },
                                onPlaySample = { viewModel.playAzanSample("najee_qazaz") },
                                onStopSample = { viewModel.stopSample() }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = glassBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Azan Length
                        Text(
                            text = stringResource(R.string.azan_length),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VoiceOptionChip(
                                label = stringResource(R.string.azan_length_full),
                                isSelected = azanLength == "full",
                                unselectedBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setAzanLength("full") }
                            )
                            VoiceOptionChip(
                                label = stringResource(R.string.azan_length_short),
                                isSelected = azanLength == "four_lines",
                                unselectedBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setAzanLength("four_lines") }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = glassBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Dua after Azan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.dua_after_azan),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.dua_after_azan_desc),
                                    fontSize = 11.sp,
                                    color = textMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SamplePlayButton(
                                    isPlaying = currentTrack == "dua_after_azan" || playingKind == "dua_after_azan",
                                    chipBg = chipUnselectedBg,
                                    border = glassBorder,
                                    textPrimary = textPrimary,
                                    onPlay = { viewModel.playDuaSample() },
                                    onStop = { viewModel.stopSample() }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Switch(
                                    checked = playDuaAfterAzan,
                                    onCheckedChange = { viewModel.setPlayDuaAfterAzan(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AmberTextDark,
                                        checkedTrackColor = AmberAccent,
                                        uncheckedThumbColor = textMuted,
                                        uncheckedTrackColor = chipUnselectedBg
                                    )
                                )
                            }
                        }
                    }
                }

                // Reminder Matrix Card
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Text(
                        text = stringResource(R.string.which_reminders),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "", modifier = Modifier.weight(1.1f))
                        Text(
                            text = stringResource(R.string.before_azan),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted,
                            modifier = Modifier.weight(0.9f)
                        )
                        Text(
                            text = stringResource(R.string.at_azan),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted,
                            modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            text = stringResource(R.string.before_jamaat),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted,
                            modifier = Modifier.weight(0.9f)
                        )
                        Text(
                            text = stringResource(R.string.after_prayer),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted,
                            modifier = Modifier.weight(0.9f)
                        )
                    }

                    HorizontalDivider(color = glassBorder, modifier = Modifier.padding(vertical = 8.dp))

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
                        val afterPrayerActive = reminderMatrix[Pair(key, "AFTER_PRAYER")] ?: true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(nameRes),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary,
                                modifier = Modifier.weight(1.1f)
                            )

                            Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.CenterStart) {
                                Checkbox(
                                    checked = beforeAzanActive,
                                    onCheckedChange = { viewModel.setReminderMatrixEntry(key, "BEFORE_AZAN", it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AmberAccent,
                                        checkmarkColor = AmberTextDark,
                                        uncheckedColor = textMuted
                                    )
                                )
                            }

                            Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.CenterStart) {
                                Checkbox(
                                    checked = atAzanActive,
                                    onCheckedChange = { viewModel.setReminderMatrixEntry(key, "AT_AZAN", it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AmberAccent,
                                        checkmarkColor = AmberTextDark,
                                        uncheckedColor = textMuted
                                    )
                                )
                            }

                            Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.CenterStart) {
                                Checkbox(
                                    checked = beforeJamaatActive,
                                    onCheckedChange = { viewModel.setReminderMatrixEntry(key, "BEFORE_JAMAAT", it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AmberAccent,
                                        checkmarkColor = AmberTextDark,
                                        uncheckedColor = textMuted
                                    )
                                )
                            }

                            Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.CenterStart) {
                                Checkbox(
                                    checked = afterPrayerActive,
                                    onCheckedChange = { viewModel.setReminderMatrixEntry(key, "AFTER_PRAYER", it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AmberAccent,
                                        checkmarkColor = AmberTextDark,
                                        uncheckedColor = textMuted
                                    )
                                )
                            }
                        }
                    }
                }

                // Silent Mode & Chime Options Card
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.speak_on_silent),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.speak_on_silent_desc),
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = voiceOnSilent,
                            onCheckedChange = { viewModel.setVoiceOnSilent(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmberTextDark,
                                checkedTrackColor = AmberAccent,
                                uncheckedThumbColor = textMuted,
                                uncheckedTrackColor = chipUnselectedBg
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.chime),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Play gentle bell tone before announcements",
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                        Switch(
                            checked = voiceChime,
                            onCheckedChange = { viewModel.setVoiceChime(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmberTextDark,
                                checkedTrackColor = AmberAccent,
                                uncheckedThumbColor = textMuted,
                                uncheckedTrackColor = chipUnselectedBg
                            )
                        )
                    }
                }
            }
        }

        if (customDialogTarget != null) {
            AlertDialog(
                onDismissRequest = { customDialogTarget = null },
                title = {
                    Text(
                        text = stringResource(R.string.custom_reminder_title),
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        val prompt = when (customDialogTarget) {
                            "before_azan" -> "Enter minutes before Azan (1 - 180):"
                            "before_jamaat" -> "Enter minutes before Jamaat (1 - 180):"
                            "after_prayer" -> "Enter minutes after Prayer / Jamaat (1 - 180):"
                            "jumuah" -> "Enter minutes before Jumu'ah (1 - 180):"
                            else -> "Enter minutes:"
                        }
                        Text(
                            text = prompt,
                            fontSize = 13.sp,
                            color = textMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customMinuteInput,
                            onValueChange = { input ->
                                customMinuteInput = input.filter { it.isDigit() }.take(3)
                            },
                            label = { Text(stringResource(R.string.custom_reminder_hint)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberAccent,
                                focusedLabelColor = AmberAccent,
                                cursorColor = AmberAccent,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val min = customMinuteInput.toIntOrNull() ?: 0
                            if (min in 1..180) {
                                when (customDialogTarget) {
                                    "before_azan" -> viewModel.addCustomBeforeAzanOffset(min)
                                    "before_jamaat" -> viewModel.addCustomBeforeJamaatOffset(min)
                                    "after_prayer" -> viewModel.addCustomAfterPrayerOffset(min)
                                    "jumuah" -> viewModel.addCustomJumuahOffset(min)
                                }
                            }
                            customDialogTarget = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            contentColor = AmberTextDark
                        )
                    ) {
                        Text(stringResource(R.string.add), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { customDialogTarget = null }) {
                        Text(stringResource(R.string.cancel), color = textMuted)
                    }
                },
                containerColor = glassCardBg
            )
        }
    }
}

@Composable
private fun TimingSectionHeader(
    title: String,
    isPlaying: Boolean,
    textColor: Color,
    chipBg: Color,
    border: Color,
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        SamplePlayButton(
            isPlaying = isPlaying,
            chipBg = chipBg,
            border = border,
            textPrimary = textColor,
            onPlay = onPlay,
            onStop = onStop
        )
    }
}

@Composable
private fun SamplePlayButton(
    isPlaying: Boolean,
    chipBg: Color,
    border: Color,
    textPrimary: Color,
    onPlay: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { if (isPlaying) onStop() else onPlay() },
        shape = RoundedCornerShape(8.dp),
        color = if (isPlaying) AmberAccent else chipBg,
        border = BorderStroke(1.dp, if (isPlaying) AmberGlowText else border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.play_sample),
                tint = if (isPlaying) AmberTextDark else AmberAccent,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.play_sample),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlaying) AmberTextDark else textPrimary
            )
        }
    }
}

@Composable
private fun VoiceOptionChip(
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
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AmberAccent else unselectedBg,
        border = BorderStroke(1.dp, if (isSelected) AmberGlowText else border)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) AmberTextDark else textPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SanctuaryChoiceChip(
    label: String,
    isSelected: Boolean,
    unselectedBg: Color,
    border: Color,
    textPrimary: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) AmberAccent else unselectedBg,
        border = BorderStroke(1.dp, if (isSelected) AmberGlowText else border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AmberTextDark,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = if (isSelected) AmberTextDark else textPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AzanModeOptionRow(
    label: String,
    isSelected: Boolean,
    chipBg: Color,
    border: Color,
    textPrimary: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AmberAccent.copy(alpha = 0.18f) else chipBg,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) AmberAccent else border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AmberAccent,
                    unselectedColor = Color(0xFFA8A29E)
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textPrimary
            )
        }
    }
}

@Composable
private fun SanctuaryReciterCard(
    name: String,
    country: String,
    avatarDrawableRes: Int,
    flagDrawableRes: Int,
    isSelected: Boolean,
    isPlaying: Boolean,
    chipBg: Color,
    border: Color,
    textPrimary: Color,
    textMuted: Color,
    onSelect: () -> Unit,
    onPlaySample: () -> Unit,
    onStopSample: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) AmberAccent.copy(alpha = 0.16f) else chipBg,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) AmberAccent else border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AmberAccent,
                    unselectedColor = textMuted
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Image(
                painter = painterResource(id = avatarDrawableRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = flagDrawableRes),
                        contentDescription = country,
                        modifier = Modifier
                            .size(width = 18.dp, height = 12.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = country,
                        fontSize = 11.sp,
                        color = textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            SamplePlayButton(
                isPlaying = isPlaying,
                chipBg = chipBg,
                border = border,
                textPrimary = textPrimary,
                onPlay = onPlaySample,
                onStop = onStopSample
            )
        }
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
        shape = RoundedCornerShape(22.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
