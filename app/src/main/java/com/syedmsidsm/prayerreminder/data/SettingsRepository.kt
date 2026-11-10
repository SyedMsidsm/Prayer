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

class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")

        val KEY_REM_BEFORE_AZAN_MIN = intPreferencesKey("rem_before_azan_min")
        val KEY_REM_JUMUAH_BEFORE_AZAN_MIN = intPreferencesKey("rem_jumuah_before_azan_min")
        val KEY_REM_AT_AZAN = booleanPreferencesKey("rem_at_azan")
        val KEY_REM_BEFORE_JAMAAT_MIN = intPreferencesKey("rem_before_jamaat_min")

        val KEY_VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val KEY_VOICE_LANGUAGE = stringPreferencesKey("voice_language")
        val KEY_VOICE_GENDER = stringPreferencesKey("voice_gender")
        val KEY_VOICE_CHIME = booleanPreferencesKey("voice_chime")
        val KEY_VOICE_ON_SILENT = booleanPreferencesKey("voice_on_silent")
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_LANGUAGE] ?: ""
    }

    suspend fun setAppLanguage(languageCode: String) {
        context.dataStore.edit { it[KEY_APP_LANGUAGE] = languageCode }
    }

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    val voiceEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_ENABLED] ?: true
    }

    val voiceLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_LANGUAGE] ?: "ur"
    }

    val voiceGender: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_GENDER] ?: "male"
    }

    val remBeforeAzanMin: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_REM_BEFORE_AZAN_MIN] ?: 10
    }

    val remBeforeJamaatMin: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_REM_BEFORE_JAMAAT_MIN] ?: 5
    }

    /**
     * Reads all Jamaat rules for FAJR, DHUHR, ASR, MAGHRIB, ISHA, JUMUAH
     */
    val jamaatRules: Flow<Map<String, JamaatRule>> = context.dataStore.data.map { prefs ->
        val keys = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA", "JUMUAH")
        val map = mutableMapOf<String, JamaatRule>()
        for (key in keys) {
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
        context.dataStore.edit { prefs ->
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
        val keys = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA", "JUMUAH")
        context.dataStore.edit { prefs ->
            for (key in keys) {
                prefs[stringPreferencesKey("jamaat_${key}_mode")] = "after"
                prefs[intPreferencesKey("jamaat_${key}_minutes")] = 15
            }
        }
    }

    /**
     * Checks whether reminders for a specific prayer are enabled
     */
    fun isPrayerReminderEnabled(prayer: Prayer): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[booleanPreferencesKey("rem_${prayer.name}_master")] ?: true
    }

    suspend fun togglePrayerReminder(prayer: Prayer) {
        context.dataStore.edit { prefs ->
            val key = booleanPreferencesKey("rem_${prayer.name}_master")
            val current = prefs[key] ?: true
            prefs[key] = !current
        }
    }
}
