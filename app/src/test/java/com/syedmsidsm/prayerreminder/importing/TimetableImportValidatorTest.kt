package com.syedmsidsm.prayerreminder.importing

import com.syedmsidsm.prayerreminder.domain.TimetableParser
import com.syedmsidsm.prayerreminder.domain.TimetableValidator
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class TimetableImportValidatorTest {

    private lateinit var goldenTimetable: Timetable
    private val validator = TimetableValidator()
    private val parser = TimetableParser()

    @Before
    fun setUp() {
        val file = File("src/main/assets/default_timetable.json").takeIf { it.exists() }
            ?: File("app/src/main/assets/default_timetable.json")
        val json = Json { ignoreUnknownKeys = true }
        goldenTimetable = json.decodeFromString(file.readText())
    }

    @Test
    fun testGoldenTimetableHasZeroValidationFlags() {
        val flags = validator.validate(goldenTimetable)
        assertEquals(0, flags.size)
    }

    @Test
    fun testDeliberatelyBadValueGetsFlaggedAndFixed() {
        // Spec 6.4 & Phase 2 acceptance: Asr on Tue 24 Mar read as 3:39 (15:39) instead of 3:59 (15:59)
        // Neighbours are 16:00 (23 Mar) and 15:59 (25 Mar)
        val modifiedDays = goldenTimetable.days.toMutableList()
        val dayIndex = modifiedDays.indexOfFirst { it.date == "2026-03-24" }
        assertTrue(dayIndex >= 0)

        val badTimes = modifiedDays[dayIndex].times.toMutableMap()
        badTimes["asr"] = "15:39"
        modifiedDays[dayIndex] = modifiedDays[dayIndex].copy(times = badTimes)
        val badTimetable = goldenTimetable.copy(days = modifiedDays)

        // Validate bad timetable
        val flags = validator.validate(badTimetable)
        assertTrue(flags.isNotEmpty())

        val asrFlag = flags.firstOrNull { it.dayIndex == dayIndex && it.field == "asr" }
        assertNotNull(asrFlag)
        assertEquals("15:39", asrFlag?.currentValue)
        assertEquals("15:59", asrFlag?.suggestion) // Interpolated between 16:00 and 15:59

        // Fix the flag with the suggestion
        badTimes["asr"] = asrFlag!!.suggestion!!
        modifiedDays[dayIndex] = modifiedDays[dayIndex].copy(times = badTimes)
        val fixedTimetable = goldenTimetable.copy(days = modifiedDays)

        val fixedFlags = validator.validate(fixedTimetable)
        assertEquals(0, fixedFlags.size)
    }

    @Test
    fun testTimeCleaningAndAmPmAttribution() {
        // Fajr & Sunrise -> AM
        assertEquals("05:18", parser.cleanAndFormatTime("5:18", "fajr"))
        assertEquals("06:33", parser.cleanAndFormatTime("6:33", "sunrise"))

        // Dhuhr -> midday/afternoon
        assertEquals("12:44", parser.cleanAndFormatTime("12:44", "dhuhr"))
        assertEquals("13:15", parser.cleanAndFormatTime("1:15", "dhuhr"))

        // Asr, Maghrib, Isha -> PM
        assertEquals("15:59", parser.cleanAndFormatTime("3:59", "asr"))
        assertEquals("18:46", parser.cleanAndFormatTime("6:46", "maghrib"))
        assertEquals("19:50", parser.cleanAndFormatTime("7:50", "isha"))

        // OCR Noise repairs (O -> 0, semicolon -> colon, etc.)
        assertEquals("05:20", parser.cleanAndFormatTime("O5.20", "fajr"))
        assertEquals("16:01", parser.cleanAndFormatTime("4;01", "asr"))
        assertEquals("19:50", parser.cleanAndFormatTime("7:5O", "isha"))
    }

    @Test
    fun testWrongWeekdayGetsFlagged() {
        val modifiedDays = goldenTimetable.days.toMutableList()
        // Change 24 Mar (Tuesday) to "Mon"
        modifiedDays[4] = modifiedDays[4].copy(weekday = "Mon")
        val table = goldenTimetable.copy(days = modifiedDays)

        val flags = validator.validate(table)
        val weekdayFlag = flags.firstOrNull { it.dayIndex == 4 && it.field == "weekday" }
        assertNotNull(weekdayFlag)
        assertEquals("Tue", weekdayFlag?.suggestion)
    }
}
