package com.syedmsidsm.prayerreminder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context? = null,
    private val dataStore: DataStore<Preferences> = requireNotNull(context) { "Context must be provided if dataStore is not injected" }.dataStore
) {

    companion object {
        val PRAYER_KEYS = listOf("FAJR", "SUNRISE", "DHUHR", "ASR", "MAGHRIB", "ISHA", "JUMUAH")
        val REMINDER_KINDS = listOf("BEFORE_AZAN", "AT_AZAN", "BEFORE_JAMAAT", "AFTER_PRAYER")

        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_FONT_SCALE = stringPreferencesKey("font_scale")

        val KEY_REM_BEFORE_AZAN_MIN = intPreferencesKey("rem_before_azan_min")
        val KEY_REM_JUMUAH_BEFORE_AZAN_MIN = intPreferencesKey("rem_jumuah_before_azan_min")
        val KEY_REM_AT_AZAN = booleanPreferencesKey("rem_at_azan")
        val KEY_REM_BEFORE_JAMAAT_MIN = intPreferencesKey("rem_before_jamaat_min")
        val KEY_REM_BEFORE_AZAN_OFFSETS = stringPreferencesKey("rem_before_azan_offsets")
        val KEY_REM_BEFORE_JAMAAT_OFFSETS = stringPreferencesKey("rem_before_jamaat_offsets")
        val KEY_REM_AFTER_PRAYER_OFFSETS = stringPreferencesKey("rem_after_prayer_offsets")
        val KEY_REM_JUMUAH_BEFORE_AZAN_OFFSETS = stringPreferencesKey("rem_jumuah_before_azan_offsets")
        val KEY_REM_SUNRISE_BEFORE_OFFSETS = stringPreferencesKey("rem_sunrise_before_offsets")
        val KEY_REM_SUNRISE_AT = booleanPreferencesKey("rem_sunrise_at")

        val KEY_VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val KEY_VOICE_LANGUAGE = stringPreferencesKey("voice_language")
        val KEY_VOICE_GENDER = stringPreferencesKey("voice_gender")
        val KEY_VOICE_CHIME = booleanPreferencesKey("voice_chime")
        val KEY_VOICE_ON_SILENT = booleanPreferencesKey("voice_on_silent")

        val KEY_OEM_STEP_CONFIRMED = booleanPreferencesKey("oem_step_confirmed")
        val KEY_LAST_TEST_RESULT = stringPreferencesKey("last_test_result")

        val KEY_AT_AZAN_TYPE = stringPreferencesKey("at_azan_type")
        val KEY_AZAN_RECITER = stringPreferencesKey("azan_reciter")
        val KEY_AZAN_LENGTH = stringPreferencesKey("azan_length")
        val KEY_PLAY_DUA_AFTER_AZAN = booleanPreferencesKey("play_dua_after_azan")
        val KEY_WIDGET_THEME = stringPreferencesKey("widget_theme")

        val KEY_SELECTED_CITY_ID = stringPreferencesKey("selected_city_id")
        val KEY_AUTO_GPS = booleanPreferencesKey("auto_gps")
        val KEY_CALC_METHOD = stringPreferencesKey("calc_method")
        val KEY_ASR_METHOD = stringPreferencesKey("asr_method")
        val KEY_USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val KEY_SKY_OVERRIDE = stringPreferencesKey("sky_override")
    }

    val atAzanType: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_AT_AZAN_TYPE] ?: "both"
    }

    suspend fun setAtAzanType(type: String) {
        dataStore.edit { it[KEY_AT_AZAN_TYPE] = type }
    }

    val azanReciter: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_AZAN_RECITER] ?: "ali_mala"
    }

    suspend fun setAzanReciter(reciter: String) {
        dataStore.edit { it[KEY_AZAN_RECITER] = reciter }
    }

    val azanLength: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_AZAN_LENGTH] ?: "full"
    }

    suspend fun setAzanLength(length: String) {
        dataStore.edit { it[KEY_AZAN_LENGTH] = length }
    }

    val playDuaAfterAzan: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_PLAY_DUA_AFTER_AZAN] ?: true
    }

    suspend fun setPlayDuaAfterAzan(enabled: Boolean) {
        dataStore.edit { it[KEY_PLAY_DUA_AFTER_AZAN] = enabled }
    }

    val widgetTheme: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_WIDGET_THEME] ?: "dark_teal"
    }

    suspend fun setWidgetTheme(theme: String) {
        dataStore.edit { it[KEY_WIDGET_THEME] = theme }
    }

    val appLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_APP_LANGUAGE] ?: ""
    }

    suspend fun setAppLanguage(languageCode: String) {
        dataStore.edit { it[KEY_APP_LANGUAGE] = languageCode }
    }

    val themeMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    val fontScale: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_FONT_SCALE] ?: "normal"
    }

    suspend fun setFontScale(scale: String) {
        dataStore.edit { it[KEY_FONT_SCALE] = scale }
    }

    val isOnboardingDone: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    suspend fun setOnboardingDone(done: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    val voiceEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_VOICE_ENABLED] ?: true
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_VOICE_ENABLED] = enabled }
    }

    val voiceLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_VOICE_LANGUAGE] ?: prefs[KEY_APP_LANGUAGE]?.ifEmpty { null } ?: "ur"
    }

    suspend fun setVoiceLanguage(lang: String) {
        dataStore.edit { it[KEY_VOICE_LANGUAGE] = lang }
    }

    val voiceGender: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_VOICE_GENDER] ?: "male"
    }

    suspend fun setVoiceGender(gender: String) {
        dataStore.edit { it[KEY_VOICE_GENDER] = gender }
    }

    val voiceChime: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_VOICE_CHIME] ?: true
    }

    suspend fun setVoiceChime(enabled: Boolean) {
        dataStore.edit { it[KEY_VOICE_CHIME] = enabled }
    }

    val voiceOnSilent: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_VOICE_ON_SILENT] ?: false
    }

    suspend fun setVoiceOnSilent(enabled: Boolean) {
        dataStore.edit { it[KEY_VOICE_ON_SILENT] = enabled }
    }

    val remBeforeAzanMin: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_REM_BEFORE_AZAN_MIN] ?: 10
    }

    val remBeforeAzanOffsets: Flow<Set<Int>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_REM_BEFORE_AZAN_OFFSETS]
        if (raw.isNullOrBlank()) {
            val def = prefs[KEY_REM_BEFORE_AZAN_MIN] ?: 10
            if (def > 0) setOf(def) else emptySet()
        } else {
            raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }
    }

    suspend fun setRemBeforeAzanOffsets(offsets: Set<Int>) {
        dataStore.edit { prefs ->
            prefs[KEY_REM_BEFORE_AZAN_OFFSETS] = offsets.sortedDescending().joinToString(",")
            prefs[KEY_REM_BEFORE_AZAN_MIN] = offsets.maxOrNull() ?: 0
        }
    }

    suspend fun setRemBeforeAzanMin(minutes: Int) {
        dataStore.edit {
            it[KEY_REM_BEFORE_AZAN_MIN] = minutes
            it[KEY_REM_BEFORE_AZAN_OFFSETS] = if (minutes > 0) minutes.toString() else ""
        }
    }

    val remJumuahBeforeAzanMin: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_REM_JUMUAH_BEFORE_AZAN_MIN] ?: 30
    }

    val remJumuahBeforeAzanOffsets: Flow<Set<Int>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_REM_JUMUAH_BEFORE_AZAN_OFFSETS]
        if (raw.isNullOrBlank()) {
            val def = prefs[KEY_REM_JUMUAH_BEFORE_AZAN_MIN] ?: 30
            if (def > 0) setOf(def) else emptySet()
        } else {
            raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }
    }

    suspend fun setRemJumuahBeforeAzanOffsets(offsets: Set<Int>) {
        dataStore.edit { prefs ->
            prefs[KEY_REM_JUMUAH_BEFORE_AZAN_OFFSETS] = offsets.sortedDescending().joinToString(",")
            prefs[KEY_REM_JUMUAH_BEFORE_AZAN_MIN] = offsets.maxOrNull() ?: 0
        }
    }

    suspend fun setRemJumuahBeforeAzanMin(minutes: Int) {
        dataStore.edit {
            it[KEY_REM_JUMUAH_BEFORE_AZAN_MIN] = minutes
            it[KEY_REM_JUMUAH_BEFORE_AZAN_OFFSETS] = if (minutes > 0) minutes.toString() else ""
        }
    }

    val remAtAzan: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_REM_AT_AZAN] ?: true
    }

    suspend fun setRemAtAzan(enabled: Boolean) {
        dataStore.edit { it[KEY_REM_AT_AZAN] = enabled }
    }

    val remSunriseBeforeOffsets: Flow<Set<Int>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_REM_SUNRISE_BEFORE_OFFSETS]
        if (raw == null) {
            setOf(15) // Default 15m before sunrise
        } else if (raw.isBlank()) {
            emptySet()
        } else {
            raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }
    }

    suspend fun setRemSunriseBeforeOffsets(offsets: Set<Int>) {
        dataStore.edit { prefs ->
            prefs[KEY_REM_SUNRISE_BEFORE_OFFSETS] = offsets.sortedDescending().joinToString(",")
        }
    }

    val remSunriseAt: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_REM_SUNRISE_AT] ?: true
    }

    suspend fun setRemSunriseAt(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_REM_SUNRISE_AT] = enabled
        }
    }

    val remBeforeJamaatMin: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_REM_BEFORE_JAMAAT_MIN] ?: 5
    }

    val remBeforeJamaatOffsets: Flow<Set<Int>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_REM_BEFORE_JAMAAT_OFFSETS]
        if (raw.isNullOrBlank()) {
            val def = prefs[KEY_REM_BEFORE_JAMAAT_MIN] ?: 5
            if (def > 0) setOf(def) else emptySet()
        } else {
            raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }
    }

    suspend fun setRemBeforeJamaatOffsets(offsets: Set<Int>) {
        dataStore.edit { prefs ->
            prefs[KEY_REM_BEFORE_JAMAAT_OFFSETS] = offsets.sortedDescending().joinToString(",")
            prefs[KEY_REM_BEFORE_JAMAAT_MIN] = offsets.maxOrNull() ?: 0
        }
    }

    suspend fun setRemBeforeJamaatMin(minutes: Int) {
        dataStore.edit {
            it[KEY_REM_BEFORE_JAMAAT_MIN] = minutes
            it[KEY_REM_BEFORE_JAMAAT_OFFSETS] = if (minutes > 0) minutes.toString() else ""
        }
    }

    val remAfterPrayerOffsets: Flow<Set<Int>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_REM_AFTER_PRAYER_OFFSETS]
        if (raw.isNullOrBlank()) {
            emptySet()
        } else {
            raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }
    }

    suspend fun setRemAfterPrayerOffsets(offsets: Set<Int>) {
        dataStore.edit { prefs ->
            prefs[KEY_REM_AFTER_PRAYER_OFFSETS] = offsets.sortedDescending().joinToString(",")
        }
    }

    val oemStepConfirmed: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_OEM_STEP_CONFIRMED] ?: false
    }

    suspend fun setOemStepConfirmed(confirmed: Boolean) {
        dataStore.edit { it[KEY_OEM_STEP_CONFIRMED] = confirmed }
    }

    val lastTestResult: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_TEST_RESULT] ?: ""
    }

    suspend fun setLastTestResult(result: String) {
        dataStore.edit { it[KEY_LAST_TEST_RESULT] = result }
    }

    /**
     * Reads all Jamaat rules for FAJR, DHUHR, ASR, MAGHRIB, ISHA, JUMUAH
     */
    val jamaatRules: Flow<Map<String, JamaatRule>> = dataStore.data.map { prefs ->
        val map = mutableMapOf<String, JamaatRule>()
        for (key in PRAYER_KEYS) {
            val mode = prefs[stringPreferencesKey("jamaat_${key}_mode")] ?: "after"
            if (mode == "fixed") {
                val fixedTimeStr = prefs[stringPreferencesKey("jamaat_${key}_fixed")] ?: ""
                val fixedTime = runCatching { LocalTime.parse(fixedTimeStr) }.getOrNull()
                if (fixedTime != null) {
                    map[key] = JamaatRule.Fixed(fixedTime)
                } else {
                    map[key] = JamaatRule.AfterAzan(15)
                }
            } else {
                val minutes = prefs[intPreferencesKey("jamaat_${key}_minutes")] ?: 15
                map[key] = JamaatRule.AfterAzan(minutes)
            }
        }
        map
    }

    suspend fun setJamaatRule(prayerKey: String, rule: JamaatRule) {
        dataStore.edit { prefs ->
            when (rule) {
                is JamaatRule.AfterAzan -> {
                    prefs[stringPreferencesKey("jamaat_${prayerKey}_mode")] = "after"
                    prefs[intPreferencesKey("jamaat_${prayerKey}_minutes")] = rule.minutes
                }
                is JamaatRule.Fixed -> {
                    prefs[stringPreferencesKey("jamaat_${prayerKey}_mode")] = "fixed"
                    prefs[stringPreferencesKey("jamaat_${prayerKey}_fixed")] = rule.time.toString()
                }
            }
        }
    }

    suspend fun resetAllJamaatTo15Minutes() {
        dataStore.edit { prefs ->
            for (key in PRAYER_KEYS) {
                prefs[stringPreferencesKey("jamaat_${key}_mode")] = "after"
                prefs[intPreferencesKey("jamaat_${key}_minutes")] = 15
            }
        }
    }

    /**
     * Reminder matrix: (PrayerKey, Kind) -> Boolean (default true)
     */
    val reminderMatrix: Flow<Map<Pair<String, String>, Boolean>> = dataStore.data.map { prefs ->
        val map = mutableMapOf<Pair<String, String>, Boolean>()
        for (prayer in PRAYER_KEYS) {
            for (kind in REMINDER_KINDS) {
                val key = booleanPreferencesKey("rem_${prayer}_${kind}")
                map[Pair(prayer, kind)] = prefs[key] ?: true
            }
        }
        map
    }

    suspend fun setReminderMatrixEntry(prayerKey: String, kind: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("rem_${prayerKey}_${kind}")] = enabled
        }
    }

    /**
     * Checks whether reminders for a specific prayer are enabled.
     * Per Spec Section 9: The bell on Home toggles all three rem_<P>_* for that prayer
     * (active if ANY is true; toggling when active sets all 3 to false; otherwise restores all 3 to true).
     */
    fun isPrayerReminderEnabled(prayer: Prayer): Flow<Boolean> = dataStore.data.map { prefs ->
        if (!prayer.hasReminders) return@map false
        val prayerKey = prayer.name
        val anyActive = REMINDER_KINDS.any { kind ->
            prefs[booleanPreferencesKey("rem_${prayerKey}_${kind}")] ?: true
        }
        anyActive
    }

    suspend fun togglePrayerReminder(prayer: Prayer) {
        if (!prayer.hasReminders) return
        val prayerKeysToToggle = if (prayer == Prayer.DHUHR) listOf("DHUHR", "JUMUAH") else listOf(prayer.name)
        dataStore.edit { prefs ->
            val currentlyActive = REMINDER_KINDS.any { kind ->
                prefs[booleanPreferencesKey("rem_${prayer.name}_${kind}")] ?: true
            }
            val targetState = !currentlyActive
            for (pk in prayerKeysToToggle) {
                for (kind in REMINDER_KINDS) {
                    prefs[booleanPreferencesKey("rem_${pk}_${kind}")] = targetState
                }
            }
            prefs[stringPreferencesKey("alert_mode_${prayer.name}")] = if (targetState) "sound" else "muted"
        }
    }

    val selectedCityId: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_CITY_ID] ?: "bhatkal"
    }

    suspend fun setSelectedCityId(cityId: String) {
        dataStore.edit { it[KEY_SELECTED_CITY_ID] = cityId }
    }

    val autoGps: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_AUTO_GPS] ?: true
    }

    suspend fun setAutoGps(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_GPS] = enabled }
    }

    val calcMethod: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_CALC_METHOD] ?: "MWL"
    }

    suspend fun setCalcMethod(method: String) {
        dataStore.edit { it[KEY_CALC_METHOD] = method }
    }

    val asrMethod: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_ASR_METHOD] ?: "Standard"
    }

    suspend fun setAsrMethod(method: String) {
        dataStore.edit { it[KEY_ASR_METHOD] = method }
    }

    val use24Hour: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_USE_24_HOUR] ?: false
    }

    suspend fun setUse24Hour(enabled: Boolean) {
        dataStore.edit { it[KEY_USE_24_HOUR] = enabled }
    }

    val skyOverride: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_SKY_OVERRIDE] ?: ""
    }

    suspend fun setSkyOverride(sky: String) {
        dataStore.edit { it[KEY_SKY_OVERRIDE] = sky }
    }

    val prayerAlertModes: Flow<Map<Prayer, String>> = dataStore.data.map { prefs ->
        val map = mutableMapOf<Prayer, String>()
        for (prayer in Prayer.entries) {
            val savedMode = prefs[stringPreferencesKey("alert_mode_${prayer.name}")]
            if (savedMode != null) {
                map[prayer] = savedMode
            } else if (!prayer.hasReminders) {
                map[prayer] = "muted"
            } else {
                val anyActive = REMINDER_KINDS.any { kind ->
                    prefs[booleanPreferencesKey("rem_${prayer.name}_${kind}")] ?: true
                }
                map[prayer] = if (anyActive) "sound" else "muted"
            }
        }
        map
    }

    suspend fun cyclePrayerAlertMode(prayer: Prayer) {
        val prayerKeysToToggle = when {
            !prayer.hasReminders -> emptyList()
            prayer == Prayer.DHUHR -> listOf("DHUHR", "JUMUAH")
            else -> listOf(prayer.name)
        }
        dataStore.edit { prefs ->
            val currentMode = prefs[stringPreferencesKey("alert_mode_${prayer.name}")] ?: run {
                if (!prayer.hasReminders) {
                    "muted"
                } else {
                    val anyActive = REMINDER_KINDS.any { kind ->
                        prefs[booleanPreferencesKey("rem_${prayer.name}_${kind}")] ?: true
                    }
                    if (anyActive) "sound" else "muted"
                }
            }
            val nextMode = when (currentMode) {
                "sound" -> "vibrate"
                "vibrate" -> "muted"
                else -> "sound"
            }
            prefs[stringPreferencesKey("alert_mode_${prayer.name}")] = nextMode
            val enabled = nextMode != "muted"
            for (pk in prayerKeysToToggle) {
                for (kind in REMINDER_KINDS) {
                    prefs[booleanPreferencesKey("rem_${pk}_${kind}")] = enabled
                }
            }
        }
    }

    suspend fun setPrayerAlertMode(prayer: Prayer, mode: String) {
        val prayerKeysToToggle = when {
            prayer == Prayer.DHUHR -> listOf("DHUHR", "JUMUAH")
            else -> listOf(prayer.name)
        }
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("alert_mode_${prayer.name}")] = mode
            val enabled = mode != "muted"
            for (pk in prayerKeysToToggle) {
                for (kind in REMINDER_KINDS) {
                    prefs[booleanPreferencesKey("rem_${pk}_${kind}")] = enabled
                }
            }
        }
    }
}
