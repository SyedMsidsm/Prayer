package com.syedmsidsm.prayerreminder.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.syedmsidsm.prayerreminder.domain.DayScheduleResolver
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

class SettingsRepositoryTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var sampleTimetable: Timetable
    private val dayScheduleResolver = DayScheduleResolver()

    @Before
    fun setUp() {
        val tempFile = File.createTempFile("test_settings", ".preferences_pb")
        tempFile.deleteOnExit()

        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.Unconfined + Job()),
            produceFile = { tempFile }
        )
        settingsRepository = SettingsRepository(dataStore = dataStore)

        val file = File("src/main/assets/default_timetable.json").takeIf { it.exists() }
            ?: File("app/src/main/assets/default_timetable.json")
        val json = Json { ignoreUnknownKeys = true }
        sampleTimetable = json.decodeFromString(file.readText())
    }

    @Test
    fun testDefaultSettingsMatchSpec() = runBlocking {
        assertEquals(10, settingsRepository.remBeforeAzanMin.first())
        assertEquals(30, settingsRepository.remJumuahBeforeAzanMin.first())
        assertTrue(settingsRepository.remAtAzan.first())
        assertEquals(5, settingsRepository.remBeforeJamaatMin.first())
        assertTrue(settingsRepository.voiceEnabled.first())
        assertEquals("ur", settingsRepository.voiceLanguage.first())
        assertEquals("male", settingsRepository.voiceGender.first())
        assertTrue(settingsRepository.voiceChime.first())
        assertFalse(settingsRepository.voiceOnSilent.first())
        assertEquals("normal", settingsRepository.fontScale.first())

        settingsRepository.setFontScale("xl")
        assertEquals("xl", settingsRepository.fontScale.first())

        val rules = settingsRepository.jamaatRules.first()
        for (key in SettingsRepository.PRAYER_KEYS) {
            val rule = rules[key]
            assertTrue(rule is JamaatRule.AfterAzan)
            assertEquals(15, (rule as JamaatRule.AfterAzan).minutes)
        }

        for (prayer in Prayer.entries) {
            if (prayer.hasReminders) {
                assertTrue(settingsRepository.isPrayerReminderEnabled(prayer).first())
            }
        }
    }

    @Test
    fun testSetJamaatRuleAndResetAll() = runBlocking {
        // Change Asr to 25 minutes after azan
        settingsRepository.setJamaatRule("ASR", JamaatRule.AfterAzan(25))
        var rules = settingsRepository.jamaatRules.first()
        val asrRule = rules["ASR"] as JamaatRule.AfterAzan
        assertEquals(25, asrRule.minutes)

        // Change Isha to fixed 8:15 PM (20:15)
        settingsRepository.setJamaatRule("ISHA", JamaatRule.Fixed(LocalTime.of(20, 15)))
        rules = settingsRepository.jamaatRules.first()
        val ishaRule = rules["ISHA"] as JamaatRule.Fixed
        assertEquals(LocalTime.of(20, 15), ishaRule.time)

        // Reset all to 15 minutes
        settingsRepository.resetAllJamaatTo15Minutes()
        rules = settingsRepository.jamaatRules.first()
        for (key in SettingsRepository.PRAYER_KEYS) {
            val rule = rules[key] as JamaatRule.AfterAzan
            assertEquals(15, rule.minutes)
        }
    }

    @Test
    fun testPrayerBellToggleHomeMatrix() = runBlocking {
        // Toggle ASR reminder off
        assertTrue(settingsRepository.isPrayerReminderEnabled(Prayer.ASR).first())
        settingsRepository.togglePrayerReminder(Prayer.ASR)
        assertFalse(settingsRepository.isPrayerReminderEnabled(Prayer.ASR).first())

        val matrix = settingsRepository.reminderMatrix.first()
        assertFalse(matrix[Pair("ASR", "BEFORE_AZAN")]!!)
        assertFalse(matrix[Pair("ASR", "AT_AZAN")]!!)
        assertFalse(matrix[Pair("ASR", "BEFORE_JAMAAT")]!!)

        // Toggle ASR back on
        settingsRepository.togglePrayerReminder(Prayer.ASR)
        assertTrue(settingsRepository.isPrayerReminderEnabled(Prayer.ASR).first())
        val restoredMatrix = settingsRepository.reminderMatrix.first()
        assertTrue(restoredMatrix[Pair("ASR", "BEFORE_AZAN")]!!)
        assertTrue(restoredMatrix[Pair("ASR", "AT_AZAN")]!!)
        assertTrue(restoredMatrix[Pair("ASR", "BEFORE_JAMAAT")]!!)

        // Toggling Dhuhr also toggles Jumu'ah in sync
        settingsRepository.togglePrayerReminder(Prayer.DHUHR)
        val dhuhrOffMatrix = settingsRepository.reminderMatrix.first()
        assertFalse(dhuhrOffMatrix[Pair("DHUHR", "BEFORE_AZAN")]!!)
        assertFalse(dhuhrOffMatrix[Pair("JUMUAH", "BEFORE_AZAN")]!!)
    }

    @Test
    fun testIntervalAndVoiceSettings() = runBlocking {
        settingsRepository.setRemBeforeAzanMin(20)
        assertEquals(20, settingsRepository.remBeforeAzanMin.first())

        settingsRepository.setRemBeforeJamaatMin(10)
        assertEquals(10, settingsRepository.remBeforeJamaatMin.first())

        settingsRepository.setVoiceLanguage("ar")
        assertEquals("ar", settingsRepository.voiceLanguage.first())

        settingsRepository.setVoiceGender("female")
        assertEquals("female", settingsRepository.voiceGender.first())

        settingsRepository.setVoiceChime(false)
        assertFalse(settingsRepository.voiceChime.first())

        settingsRepository.setVoiceOnSilent(true)
        assertTrue(settingsRepository.voiceOnSilent.first())
    }

    @Test
    fun testChangingRulesUpdatesScheduleImmediately() = runBlocking {
        val testDate = LocalDate.of(2026, 3, 24) // 24 Mar 2026 (Day 5, Asr Azan 15:59)
        val initialRules = settingsRepository.jamaatRules.first()

        // 1. Initial schedule with default 15 min: Asr jamaat is 16:14
        val initialSchedule = dayScheduleResolver.resolve(testDate, listOf(sampleTimetable), initialRules)
        assertEquals(LocalTime.of(15, 59), initialSchedule.azan[Prayer.ASR])
        assertEquals(LocalTime.of(16, 14), initialSchedule.jamaat[Prayer.ASR])
        assertTrue(initialSchedule.fixedTimeBeforeAzanWarnings.isEmpty())

        // 2. Change Asr rule to 25 minutes after azan
        settingsRepository.setJamaatRule("ASR", JamaatRule.AfterAzan(25))
        val updatedRules = settingsRepository.jamaatRules.first()
        val updatedSchedule = dayScheduleResolver.resolve(testDate, listOf(sampleTimetable), updatedRules)
        assertEquals(LocalTime.of(16, 24), updatedSchedule.jamaat[Prayer.ASR])

        // 3. Change Isha rule to fixed 8:15 PM (20:15) (Isha azan is 19:56)
        settingsRepository.setJamaatRule("ISHA", JamaatRule.Fixed(LocalTime.of(20, 15)))
        val fixedRules = settingsRepository.jamaatRules.first()
        val fixedSchedule = dayScheduleResolver.resolve(testDate, listOf(sampleTimetable), fixedRules)
        assertEquals(LocalTime.of(20, 15), fixedSchedule.jamaat[Prayer.ISHA])
        assertFalse(fixedSchedule.fixedTimeBeforeAzanWarnings.contains(Prayer.ISHA))

        // 4. Sanity check: Set fixed Isha to 19:30 (before Isha azan 19:56)
        settingsRepository.setJamaatRule("ISHA", JamaatRule.Fixed(LocalTime.of(19, 30)))
        val sanityRules = settingsRepository.jamaatRules.first()
        val sanitySchedule = dayScheduleResolver.resolve(testDate, listOf(sampleTimetable), sanityRules)
        // Falls back to azan + 15 min (19:50 + 15 min = 20:05) and records warning
        assertEquals(LocalTime.of(20, 5), sanitySchedule.jamaat[Prayer.ISHA])
        assertTrue(sanitySchedule.fixedTimeBeforeAzanWarnings.contains(Prayer.ISHA))
    }
}
