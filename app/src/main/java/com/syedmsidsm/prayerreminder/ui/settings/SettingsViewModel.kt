package com.syedmsidsm.prayerreminder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syedmsidsm.prayerreminder.audio.AudioSamplePlayer
import com.syedmsidsm.prayerreminder.data.SettingsRepository
import com.syedmsidsm.prayerreminder.data.TimetableRepository
import com.syedmsidsm.prayerreminder.domain.DayScheduleResolver
import com.syedmsidsm.prayerreminder.domain.model.DaySchedule
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val timetableRepository: TimetableRepository,
    private val dayScheduleResolver: DayScheduleResolver,
    val audioPlayer: AudioSamplePlayer,
    private val zoneId: ZoneId = ZoneId.of("Asia/Kolkata")
) : ViewModel() {

    val appLanguage: StateFlow<String> = settingsRepository.appLanguage.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val jamaatRules: StateFlow<Map<String, JamaatRule>> = settingsRepository.jamaatRules.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val voiceEnabled: StateFlow<Boolean> = settingsRepository.voiceEnabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val voiceLanguage: StateFlow<String> = settingsRepository.voiceLanguage.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "ur"
    )

    val voiceGender: StateFlow<String> = settingsRepository.voiceGender.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "male"
    )

    val voiceChime: StateFlow<Boolean> = settingsRepository.voiceChime.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val voiceOnSilent: StateFlow<Boolean> = settingsRepository.voiceOnSilent.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val remBeforeAzanMin: StateFlow<Int> = settingsRepository.remBeforeAzanMin.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        10
    )

    val remJumuahBeforeAzanMin: StateFlow<Int> = settingsRepository.remJumuahBeforeAzanMin.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        30
    )

    val remAtAzan: StateFlow<Boolean> = settingsRepository.remAtAzan.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val remBeforeJamaatMin: StateFlow<Int> = settingsRepository.remBeforeJamaatMin.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        5
    )

    val reminderMatrix: StateFlow<Map<Pair<String, String>, Boolean>> = settingsRepository.reminderMatrix.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val timetables: StateFlow<List<Timetable>> = timetableRepository.timetables

    val todaySchedule: StateFlow<DaySchedule?> = combine(
        timetableRepository.timetables,
        settingsRepository.jamaatRules
    ) { tables, rules ->
        val now = ZonedDateTime.now(zoneId)
        val today = now.toLocalDate()
        val hasToday = tables.any { table -> table.days.any { it.date == today.toString() } }
        val date = if (hasToday) today else LocalDate.of(2026, 3, 24)
        dayScheduleResolver.resolve(date, tables, rules)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setAppLanguage(languageCode: String) {
        viewModelScope.launch {
            settingsRepository.setAppLanguage(languageCode)
        }
    }

    fun setJamaatRule(prayerKey: String, rule: JamaatRule) {
        viewModelScope.launch {
            settingsRepository.setJamaatRule(prayerKey, rule)
        }
    }

    fun resetAllJamaatTo15Minutes() {
        viewModelScope.launch {
            settingsRepository.resetAllJamaatTo15Minutes()
        }
    }

    fun setVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVoiceEnabled(enabled)
        }
    }

    fun setVoiceLanguage(lang: String) {
        viewModelScope.launch {
            settingsRepository.setVoiceLanguage(lang)
        }
    }

    fun setVoiceGender(gender: String) {
        viewModelScope.launch {
            settingsRepository.setVoiceGender(gender)
        }
    }

    fun setVoiceChime(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVoiceChime(enabled)
        }
    }

    fun setVoiceOnSilent(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVoiceOnSilent(enabled)
        }
    }

    fun setRemBeforeAzanMin(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setRemBeforeAzanMin(minutes)
        }
    }

    fun setRemJumuahBeforeAzanMin(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setRemJumuahBeforeAzanMin(minutes)
        }
    }

    fun setRemAtAzan(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRemAtAzan(enabled)
        }
    }

    fun setRemBeforeJamaatMin(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setRemBeforeJamaatMin(minutes)
        }
    }

    fun setReminderMatrixEntry(prayerKey: String, kind: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReminderMatrixEntry(prayerKey, kind, enabled)
        }
    }

    fun playSample(kind: String, minutes: Int = 10) {
        val lang = voiceLanguage.value
        val gender = voiceGender.value
        val chime = voiceChime.value
        audioPlayer.playSample(
            language = lang,
            gender = gender,
            kind = kind,
            minutes = minutes,
            chime = chime
        )
    }

    fun stopSample() {
        audioPlayer.stop()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
