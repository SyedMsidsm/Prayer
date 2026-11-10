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
        val PRAYER_KEYS = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA", "JUMUAH")
        val REMINDER_KINDS = listOf("BEFORE_AZAN", "AT_AZAN", "BEFORE_JAMAAT")

        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

        val KEY_REM_BEFORE_AZAN_MIN = intPreferencesKey("rem_before_azan_min")
        val KEY_REM_JUMUAH_BEFORE_AZAN_MIN = intPreferencesKey("rem_jumuah_before_azan_min")
        val KEY_REM_AT_AZAN = booleanPreferencesKey("rem_at_azan")
        val KEY_REM_BEFORE_JAMAAT_MIN = intPreferencesKey("rem_before_jamaat_min")

        val KEY_VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val KEY_VOICE_LANGUAGE = stringPreferencesKey("voice_language")
        val KEY_VOICE_GENDER = stringPreferencesKey("voice_gender")
        val KEY_VOICE_CHIME = booleanPreferencesKey("voice_chime")
        val KEY_VOICE_ON_SILENT = booleanPreferencesKey("voice_on_silent")

        val KEY_OEM_STEP_CONFIRMED = booleanPreferencesKey("oem_step_confirmed")
        val KEY_LAST_TEST_RESULT = stringPreferencesKey("last_test_result")
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

    suspend fun setRemBeforeAzanMin(minutes: Int) {
        dataStore.edit { it[KEY_REM_BEFORE_AZAN_MIN] = minutes }
    }

    val remJumuahBeforeAzanMin: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_REM_JUMUAH_BEFORE_AZAN_MIN] ?: 30
    }

    suspend fun setRemJumuahBeforeAzanMin(minutes: Int) {
        dataStore.edit { it[KEY_REM_JUMUAH_BEFORE_AZAN_MIN] = minutes }
    }

    val remAtAzan: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_REM_AT_AZAN] ?: true
    }

    suspend fun setRemAtAzan(enabled: Boolean) {
        dataStore.edit { it[KEY_REM_AT_AZAN] = enabled }
    }

    val remBeforeJamaatMin: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_REM_BEFORE_JAMAAT_MIN] ?: 5
    }

    suspend fun setRemBeforeJamaatMin(minutes: Int) {
        dataStore.edit { it[KEY_REM_BEFORE_JAMAAT_MIN] = minutes }
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
        }
    }
}
