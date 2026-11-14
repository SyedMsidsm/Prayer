package com.syedmsidsm.prayerreminder.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syedmsidsm.prayerreminder.audio.AudioSamplePlayer
import com.syedmsidsm.prayerreminder.data.SettingsRepository
import com.syedmsidsm.prayerreminder.data.TimetableRepository
import com.syedmsidsm.prayerreminder.domain.DayScheduleResolver
import com.syedmsidsm.prayerreminder.domain.model.DaySchedule
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.notifications.NotificationHelper
import com.syedmsidsm.prayerreminder.reminders.AlarmScheduler
import com.syedmsidsm.prayerreminder.reminders.AppRefresher
import com.syedmsidsm.prayerreminder.reminders.FiredEventLog
import com.syedmsidsm.prayerreminder.reminders.MissedReminderDetector
import com.syedmsidsm.prayerreminder.reminders.ReminderKind
import com.syedmsidsm.prayerreminder.reminders.ReminderPlanner
import com.syedmsidsm.prayerreminder.reminders.ScheduledReminderEvent
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
    private val context: Context? = null,
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

    val themeMode: StateFlow<String> = settingsRepository.themeMode.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "system"
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    val fontScale: StateFlow<String> = settingsRepository.fontScale.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "normal"
    )

    fun setFontScale(scale: String) {
        viewModelScope.launch {
            settingsRepository.setFontScale(scale)
        }
    }

    val selectedCityId: StateFlow<String> = settingsRepository.selectedCityId.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "bhatkal"
    )

    fun setSelectedCityId(cityId: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedCityId(cityId)
            context?.let {
                AppRefresher.refreshAll(it, "city_changed")
                com.syedmsidsm.prayerreminder.widget.WidgetUpdater.updateAll(it)
            }
        }
    }

    val autoGps: StateFlow<Boolean> = settingsRepository.autoGps.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        true
    )

    fun setAutoGps(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoGps(enabled)
        }
    }

    val calcMethod: StateFlow<String> = settingsRepository.calcMethod.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "MWL"
    )

    fun setCalcMethod(method: String) {
        viewModelScope.launch {
            settingsRepository.setCalcMethod(method)
            context?.let {
                AppRefresher.refreshAll(it, "calc_method_changed")
                com.syedmsidsm.prayerreminder.widget.WidgetUpdater.updateAll(it)
            }
        }
    }

    val asrMethod: StateFlow<String> = settingsRepository.asrMethod.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "Standard"
    )

    fun setAsrMethod(method: String) {
        viewModelScope.launch {
            settingsRepository.setAsrMethod(method)
            context?.let {
                AppRefresher.refreshAll(it, "asr_method_changed")
                com.syedmsidsm.prayerreminder.widget.WidgetUpdater.updateAll(it)
            }
        }
    }

    val use24Hour: StateFlow<Boolean> = settingsRepository.use24Hour.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )

    fun setUse24Hour(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUse24Hour(enabled)
            context?.let {
                com.syedmsidsm.prayerreminder.widget.WidgetUpdater.updateAll(it)
            }
        }
    }

    fun adjustIqamahDelay(prayerKey: String, deltaMinutes: Int) {
        val currentRule = jamaatRules.value[prayerKey]
        val currentMin = (currentRule as? JamaatRule.AfterAzan)?.minutes ?: 15
        val newMin = (currentMin + deltaMinutes).coerceIn(0, 60)
        setJamaatRule(prayerKey, JamaatRule.AfterAzan(newMin))
    }

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

    val atAzanType: StateFlow<String> = settingsRepository.atAzanType.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "both"
    )

    val azanReciter: StateFlow<String> = settingsRepository.azanReciter.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "ali_mala"
    )

    val azanLength: StateFlow<String> = settingsRepository.azanLength.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "full"
    )

    val playDuaAfterAzan: StateFlow<Boolean> = settingsRepository.playDuaAfterAzan.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val widgetTheme: StateFlow<String> = settingsRepository.widgetTheme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "dark_teal"
    )

    val remBeforeAzanMin: StateFlow<Int> = settingsRepository.remBeforeAzanMin.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        10
    )

    val remBeforeAzanOffsets: StateFlow<Set<Int>> = settingsRepository.remBeforeAzanOffsets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        setOf(10)
    )

    val remJumuahBeforeAzanMin: StateFlow<Int> = settingsRepository.remJumuahBeforeAzanMin.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        30
    )

    val remJumuahBeforeAzanOffsets: StateFlow<Set<Int>> = settingsRepository.remJumuahBeforeAzanOffsets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        setOf(30)
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

    val remBeforeJamaatOffsets: StateFlow<Set<Int>> = settingsRepository.remBeforeJamaatOffsets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        setOf(5)
    )

    val remAfterPrayerOffsets: StateFlow<Set<Int>> = settingsRepository.remAfterPrayerOffsets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptySet()
    )

    val remSunriseBeforeOffsets: StateFlow<Set<Int>> = settingsRepository.remSunriseBeforeOffsets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        setOf(15)
    )

    val remSunriseAt: StateFlow<Boolean> = settingsRepository.remSunriseAt.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val reminderMatrix: StateFlow<Map<Pair<String, String>, Boolean>> = settingsRepository.reminderMatrix.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val prayerAlertModes: StateFlow<Map<Prayer, String>> = settingsRepository.prayerAlertModes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val lastTestResult: StateFlow<String> = settingsRepository.lastTestResult.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val isOnboardingDone: StateFlow<Boolean?> = settingsRepository.isOnboardingDone.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    val timetables: StateFlow<List<Timetable>> = timetableRepository.timetables

    val todaySchedule: StateFlow<DaySchedule?> = combine(
        timetableRepository.timetables,
        settingsRepository.jamaatRules
    ) { tables, rules ->
        val today = ZonedDateTime.now(zoneId).toLocalDate()
        dayScheduleResolver.resolve(today, tables, rules)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setAppLanguage(languageCode: String) {
        viewModelScope.launch {
            settingsRepository.setAppLanguage(languageCode)
            context?.let { ctx ->
                NotificationHelper(ctx).updateChannelNamesForLanguage(languageCode)
                AppRefresher.refreshAll(ctx, "language_changed")
            }
        }
    }

    fun setJamaatRule(prayerKey: String, rule: JamaatRule) {
        viewModelScope.launch {
            settingsRepository.setJamaatRule(prayerKey, rule)
            context?.let { AppRefresher.refreshAll(it, "jamaat_changed") }
        }
    }

    fun resetAllJamaatTo15Minutes() {
        viewModelScope.launch {
            settingsRepository.resetAllJamaatTo15Minutes()
            context?.let { AppRefresher.refreshAll(it, "jamaat_reset") }
        }
    }

    fun setVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVoiceEnabled(enabled)
            context?.let { AppRefresher.refreshAll(it, "voice_enabled_changed") }
        }
    }

    fun setVoiceLanguage(lang: String, playPreview: Boolean = false) {
        viewModelScope.launch {
            settingsRepository.setVoiceLanguage(lang)
            context?.let { AppRefresher.refreshAll(it, "voice_language_changed") }
        }
        if (playPreview) {
            val gender = voiceGender.value
            audioPlayer.playVoicePreview(lang, gender)
        }
    }

    fun setVoiceGender(gender: String, playPreview: Boolean = true) {
        viewModelScope.launch {
            settingsRepository.setVoiceGender(gender)
            context?.let { AppRefresher.refreshAll(it, "voice_gender_changed") }
        }
        if (playPreview) {
            val lang = voiceLanguage.value
            audioPlayer.playVoicePreview(lang, gender)
        }
    }

    fun playVoicePreview(gender: String) {
        val lang = voiceLanguage.value
        audioPlayer.playVoicePreview(lang, gender)
    }

    fun setVoiceChime(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVoiceChime(enabled)
            context?.let { AppRefresher.refreshAll(it, "voice_chime_changed") }
        }
    }

    fun setVoiceOnSilent(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVoiceOnSilent(enabled)
            context?.let { AppRefresher.refreshAll(it, "voice_silent_changed") }
        }
    }

    fun setPrayerAlertMode(prayer: Prayer, mode: String) {
        viewModelScope.launch {
            settingsRepository.setPrayerAlertMode(prayer, mode)
            context?.let { AppRefresher.refreshAll(it, "prayer_alert_mode_changed") }
        }
    }

    fun setRemBeforeAzanMin(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setRemBeforeAzanMin(minutes)
            context?.let { AppRefresher.refreshAll(it, "before_azan_min_changed") }
        }
    }

    fun setRemJumuahBeforeAzanMin(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setRemJumuahBeforeAzanMin(minutes)
            context?.let { AppRefresher.refreshAll(it, "jumuah_min_changed") }
        }
    }

    fun setRemAtAzan(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRemAtAzan(enabled)
            context?.let { AppRefresher.refreshAll(it, "at_azan_changed") }
        }
    }

    fun setRemBeforeJamaatMin(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setRemBeforeJamaatMin(minutes)
            context?.let { AppRefresher.refreshAll(it, "before_jamaat_min_changed") }
        }
    }

    fun toggleBeforeAzanOffset(minutes: Int) {
        viewModelScope.launch {
            if (minutes <= 0) {
                settingsRepository.setRemBeforeAzanOffsets(emptySet())
            } else {
                val current = remBeforeAzanOffsets.value.toMutableSet()
                if (current.contains(minutes)) {
                    current.remove(minutes)
                } else {
                    current.add(minutes)
                }
                settingsRepository.setRemBeforeAzanOffsets(current)
            }
            context?.let { AppRefresher.refreshAll(it, "before_azan_offsets_changed") }
        }
    }

    fun toggleBeforeJamaatOffset(minutes: Int) {
        viewModelScope.launch {
            if (minutes <= 0) {
                settingsRepository.setRemBeforeJamaatOffsets(emptySet())
            } else {
                val current = remBeforeJamaatOffsets.value.toMutableSet()
                if (current.contains(minutes)) {
                    current.remove(minutes)
                } else {
                    current.add(minutes)
                }
                settingsRepository.setRemBeforeJamaatOffsets(current)
            }
            context?.let { AppRefresher.refreshAll(it, "before_jamaat_offsets_changed") }
        }
    }

    fun toggleJumuahBeforeAzanOffset(minutes: Int) {
        viewModelScope.launch {
            if (minutes <= 0) {
                settingsRepository.setRemJumuahBeforeAzanOffsets(emptySet())
            } else {
                val current = remJumuahBeforeAzanOffsets.value.toMutableSet()
                if (current.contains(minutes)) {
                    current.remove(minutes)
                } else {
                    current.add(minutes)
                }
                settingsRepository.setRemJumuahBeforeAzanOffsets(current)
            }
            context?.let { AppRefresher.refreshAll(it, "jumuah_offsets_changed") }
        }
    }

    fun toggleAfterPrayerOffset(minutes: Int) {
        viewModelScope.launch {
            if (minutes <= 0) {
                settingsRepository.setRemAfterPrayerOffsets(emptySet())
            } else {
                val current = remAfterPrayerOffsets.value.toMutableSet()
                if (current.contains(minutes)) {
                    current.remove(minutes)
                } else {
                    current.add(minutes)
                }
                settingsRepository.setRemAfterPrayerOffsets(current)
            }
            context?.let { AppRefresher.refreshAll(it, "after_prayer_offsets_changed") }
        }
    }

    fun addCustomBeforeAzanOffset(minutes: Int) {
        if (minutes <= 0) return
        viewModelScope.launch {
            val current = remBeforeAzanOffsets.value.toMutableSet()
            current.add(minutes)
            settingsRepository.setRemBeforeAzanOffsets(current)
            context?.let { AppRefresher.refreshAll(it, "custom_before_azan_added") }
        }
    }

    fun addCustomBeforeJamaatOffset(minutes: Int) {
        if (minutes <= 0) return
        viewModelScope.launch {
            val current = remBeforeJamaatOffsets.value.toMutableSet()
            current.add(minutes)
            settingsRepository.setRemBeforeJamaatOffsets(current)
            context?.let { AppRefresher.refreshAll(it, "custom_before_jamaat_added") }
        }
    }

    fun addCustomAfterPrayerOffset(minutes: Int) {
        if (minutes <= 0) return
        viewModelScope.launch {
            val current = remAfterPrayerOffsets.value.toMutableSet()
            current.add(minutes)
            settingsRepository.setRemAfterPrayerOffsets(current)
            context?.let { AppRefresher.refreshAll(it, "custom_after_prayer_added") }
        }
    }

    fun addCustomJumuahOffset(minutes: Int) {
        if (minutes <= 0) return
        viewModelScope.launch {
            val current = remJumuahBeforeAzanOffsets.value.toMutableSet()
            current.add(minutes)
            settingsRepository.setRemJumuahBeforeAzanOffsets(current)
            context?.let { AppRefresher.refreshAll(it, "custom_jumuah_added") }
        }
    }

    fun toggleSunriseBeforeOffset(minutes: Int) {
        viewModelScope.launch {
            if (minutes == 0) {
                settingsRepository.setRemSunriseBeforeOffsets(emptySet())
            } else {
                val current = remSunriseBeforeOffsets.value.toMutableSet()
                if (current.contains(minutes)) {
                    current.remove(minutes)
                } else {
                    current.add(minutes)
                }
                settingsRepository.setRemSunriseBeforeOffsets(current)
            }
            context?.let { AppRefresher.refreshAll(it, "sunrise_offsets_changed") }
        }
    }

    fun addCustomSunriseOffset(minutes: Int) {
        if (minutes <= 0) return
        viewModelScope.launch {
            val current = remSunriseBeforeOffsets.value.toMutableSet()
            current.add(minutes)
            settingsRepository.setRemSunriseBeforeOffsets(current)
            context?.let { AppRefresher.refreshAll(it, "custom_sunrise_added") }
        }
    }

    fun setRemSunriseAt(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRemSunriseAt(enabled)
            context?.let { AppRefresher.refreshAll(it, "sunrise_at_changed") }
        }
    }

    fun setReminderMatrixEntry(prayerKey: String, kind: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReminderMatrixEntry(prayerKey, kind, enabled)
            context?.let { AppRefresher.refreshAll(it, "matrix_changed") }
        }
    }

    fun scheduleTestReminder() {
        val ctx = context ?: return
        val fireAtEpochSeconds = (System.currentTimeMillis() / 1000) + 60
        val event = ScheduledReminderEvent(
            code = ReminderPlanner.TEST_REMINDER_CODE,
            fireAtEpochSeconds = fireAtEpochSeconds,
            kind = ReminderKind.TEST
        )
        val firedLog = FiredEventLog(ctx)
        val missedDetector = MissedReminderDetector(ctx, firedLog)
        val scheduler = AlarmScheduler(ctx, missedDetector)
        scheduler.scheduleEvent(event)
    }

    fun setOnboardingDone(done: Boolean) {
        viewModelScope.launch {
            settingsRepository.setOnboardingDone(done)
        }
    }

    fun setAtAzanType(type: String) {
        viewModelScope.launch {
            settingsRepository.setAtAzanType(type)
            context?.let { AppRefresher.refreshAll(it, "at_azan_type_changed") }
        }
    }

    fun setAzanReciter(reciter: String) {
        viewModelScope.launch {
            settingsRepository.setAzanReciter(reciter)
            context?.let { AppRefresher.refreshAll(it, "azan_reciter_changed") }
        }
    }

    fun setAzanLength(length: String) {
        viewModelScope.launch {
            settingsRepository.setAzanLength(length)
            context?.let { AppRefresher.refreshAll(it, "azan_length_changed") }
        }
    }

    fun setPlayDuaAfterAzan(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPlayDuaAfterAzan(enabled)
            context?.let { AppRefresher.refreshAll(it, "play_dua_changed") }
        }
    }

    fun setWidgetTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setWidgetTheme(theme)
            context?.let {
                com.syedmsidsm.prayerreminder.widget.WidgetUpdater.updateAll(it)
            }
        }
    }

    fun playAzanSample(reciterId: String) {
        audioPlayer.playAzanSample(reciterId)
    }

    fun playDuaSample() {
        audioPlayer.playDuaSample()
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
