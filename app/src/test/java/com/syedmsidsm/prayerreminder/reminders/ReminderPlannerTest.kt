package com.syedmsidsm.prayerreminder.reminders

import com.syedmsidsm.prayerreminder.domain.DayScheduleResolver
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderPlannerTest {

    private lateinit var sampleTimetable: Timetable
    private val dayScheduleResolver = DayScheduleResolver()
    private val reminderPlanner = ReminderPlanner()
    private val zoneId = ZoneId.of("Asia/Kolkata")

    @Before
    fun setUp() {
        val file = File("src/main/assets/default_timetable.json").takeIf { it.exists() }
            ?: File("app/src/main/assets/default_timetable.json")
        val json = Json { ignoreUnknownKeys = true }
        sampleTimetable = json.decodeFromString(file.readText())
    }

    @Test
    fun testPlanStandardPrayerReminders() {
        val date = LocalDate.of(2026, 3, 24) // Day 5: Fajr 5:18, Dhuhr 12:44, Asr 15:59, Maghrib 18:46, Isha 19:50
        val tomorrow = date.plusDays(1)
        val defaultRules = mapOf<String, JamaatRule>()

        val todaySchedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable), defaultRules)
        val tomorrowSchedule = dayScheduleResolver.resolve(tomorrow, listOf(sampleTimetable), defaultRules)

        // Set simulated current time to 14:00 on 24 Mar 2026
        val now = date.atTime(14, 0).atZone(zoneId)

        val settings = ReminderSettingsSnapshot(
            remBeforeAzanMin = 10,
            remJumuahBeforeAzanMin = 30,
            remAtAzan = true,
            remBeforeJamaatMin = 5
        )

        val events = reminderPlanner.plan(
            now = now,
            today = todaySchedule,
            tomorrow = tomorrowSchedule,
            settings = settings,
            latestTimetable = sampleTimetable
        )

        // Events before 14:00 (Fajr, Dhuhr) should be dropped because they are in the past
        val pastEvents = events.filter { it.fireAtEpochSeconds <= now.toEpochSecond() }
        assertTrue(pastEvents.isEmpty())

        // Check today's Asr events (Azan 15:59, Jamaat 16:14)
        val todayAsrEvents = events.filter { it.prayer == Prayer.ASR && it.fireAtEpochSeconds < date.plusDays(1).atStartOfDay(zoneId).toEpochSecond() }
        assertEquals(4, todayAsrEvents.size)

        val beforeAzan = todayAsrEvents.first { it.kind == ReminderKind.BEFORE_AZAN }
        assertEquals(date.atTime(15, 49).atZone(zoneId).toEpochSecond(), beforeAzan.fireAtEpochSeconds)
        assertEquals(10, beforeAzan.minutesOffset)

        val atAzan = todayAsrEvents.first { it.kind == ReminderKind.AT_AZAN }
        assertEquals(date.atTime(15, 59).atZone(zoneId).toEpochSecond(), atAzan.fireAtEpochSeconds)

        val beforeJamaat = todayAsrEvents.first { it.kind == ReminderKind.BEFORE_JAMAAT }
        // Jamaat 16:14 - 5 min = 16:09 (> azan 15:59)
        assertEquals(date.atTime(16, 9).atZone(zoneId).toEpochSecond(), beforeJamaat.fireAtEpochSeconds)
        assertEquals(5, beforeJamaat.minutesOffset)

        val phaseEvent = todayAsrEvents.first { it.kind == ReminderKind.PHASE }
        assertEquals(date.atTime(16, 14).atZone(zoneId).toEpochSecond(), phaseEvent.fireAtEpochSeconds)
    }

    @Test
    fun testBeforeJamaatSkippedWhenNotAfterAzanTime() {
        val date = LocalDate.of(2026, 3, 24)
        val tomorrow = date.plusDays(1)
        val defaultRules = mapOf<String, JamaatRule>()

        val todaySchedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable), defaultRules)
        val tomorrowSchedule = dayScheduleResolver.resolve(tomorrow, listOf(sampleTimetable), defaultRules)

        val now = date.atTime(14, 0).atZone(zoneId)

        // If remBeforeJamaatMin is 15 minutes, and jamaat is 15 min after azan (16:14 - 15 = 15:59)
        // Fire time is NOT > azan time (15:59 is not > 15:59), so it must be skipped per Section 10.1!
        val settings = ReminderSettingsSnapshot(
            remBeforeAzanMin = 10,
            remAtAzan = true,
            remBeforeJamaatMin = 15
        )

        val events = reminderPlanner.plan(
            now = now,
            today = todaySchedule,
            tomorrow = tomorrowSchedule,
            settings = settings,
            latestTimetable = sampleTimetable
        )

        val asrBeforeJamaat = events.firstOrNull { it.prayer == Prayer.ASR && it.kind == ReminderKind.BEFORE_JAMAAT }
        assertNull(asrBeforeJamaat)
    }

    @Test
    fun testFridayJumuahUsesSpecialBeforeAzanMinutes() {
        val date = LocalDate.of(2026, 3, 27) // Friday (Day 8 in Shawwal 1447)
        val tomorrow = date.plusDays(1)
        val defaultRules = mapOf<String, JamaatRule>()

        val todaySchedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable), defaultRules)
        val tomorrowSchedule = dayScheduleResolver.resolve(tomorrow, listOf(sampleTimetable), defaultRules)

        val now = date.atTime(10, 0).atZone(zoneId)

        val settings = ReminderSettingsSnapshot(
            remBeforeAzanMin = 10,
            remJumuahBeforeAzanMin = 30, // 30 min before Jumu'ah azan
            remAtAzan = true,
            remBeforeJamaatMin = 5
        )

        val events = reminderPlanner.plan(
            now = now,
            today = todaySchedule,
            tomorrow = tomorrowSchedule,
            settings = settings,
            latestTimetable = sampleTimetable
        )

        // Friday Dhuhr is Jumu'ah
        val jumuahBeforeAzan = events.firstOrNull { it.prayer == Prayer.DHUHR && it.kind == ReminderKind.BEFORE_AZAN }
        assertNotNull(jumuahBeforeAzan)
        assertTrue(jumuahBeforeAzan!!.isJumuah)
        assertEquals(30, jumuahBeforeAzan.minutesOffset)

        // On Friday 2026-03-27, Dhuhr azan is 12:43 -> 30 min before is 12:13
        val expectedEpoch = date.atTime(12, 13).atZone(zoneId).toEpochSecond()
        assertEquals(expectedEpoch, jumuahBeforeAzan.fireAtEpochSeconds)
    }

    @Test
    fun testTableExpiringNotificationScheduledInLast3Days() {
        // Table ends 2026-04-18
        val date = LocalDate.of(2026, 4, 16) // 2 days before expiry
        val tomorrow = date.plusDays(1)
        val defaultRules = mapOf<String, JamaatRule>()

        val todaySchedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable), defaultRules)
        val tomorrowSchedule = dayScheduleResolver.resolve(tomorrow, listOf(sampleTimetable), defaultRules)

        val now = date.atTime(8, 0).atZone(zoneId) // 8:00 AM, before 9:00 AM

        val settings = ReminderSettingsSnapshot()

        val events = reminderPlanner.plan(
            now = now,
            today = todaySchedule,
            tomorrow = tomorrowSchedule,
            settings = settings,
            latestTimetable = sampleTimetable
        )

        val expiringEvents = events.filter { it.kind == ReminderKind.TABLE_EXPIRING }
        assertFalse(expiringEvents.isEmpty())
        val firstExpiring = expiringEvents.first()
        assertEquals(date.atTime(9, 0).atZone(zoneId).toEpochSecond(), firstExpiring.fireAtEpochSeconds)
        assertEquals(2, firstExpiring.daysUntilExpiry)
    }

    @Test
    fun testMidnightRefreshScheduled() {
        val date = LocalDate.of(2026, 3, 24)
        val tomorrow = date.plusDays(1)
        val defaultRules = mapOf<String, JamaatRule>()

        val todaySchedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable), defaultRules)
        val tomorrowSchedule = dayScheduleResolver.resolve(tomorrow, listOf(sampleTimetable), defaultRules)

        val now = date.atTime(14, 0).atZone(zoneId)

        val events = reminderPlanner.plan(
            now = now,
            today = todaySchedule,
            tomorrow = tomorrowSchedule,
            settings = ReminderSettingsSnapshot(),
            latestTimetable = sampleTimetable
        )

        val midnight = events.firstOrNull { it.kind == ReminderKind.MIDNIGHT }
        assertNotNull(midnight)
        assertEquals(tomorrow.atTime(0, 0, 30).atZone(zoneId).toEpochSecond(), midnight!!.fireAtEpochSeconds)
    }

    @Test
    fun testMatrixDisablingDropsReminder() {
        val date = LocalDate.of(2026, 3, 24)
        val tomorrow = date.plusDays(1)
        val defaultRules = mapOf<String, JamaatRule>()

        val todaySchedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable), defaultRules)
        val tomorrowSchedule = dayScheduleResolver.resolve(tomorrow, listOf(sampleTimetable), defaultRules)

        val now = date.atTime(14, 0).atZone(zoneId)

        // Disable BEFORE_AZAN for ASR in matrix
        val matrix = mapOf(
            Pair("ASR", "BEFORE_AZAN") to false,
            Pair("ASR", "AT_AZAN") to true,
            Pair("ASR", "BEFORE_JAMAAT") to true
        )

        val settings = ReminderSettingsSnapshot(
            remBeforeAzanMin = 10,
            matrix = matrix
        )

        val events = reminderPlanner.plan(
            now = now,
            today = todaySchedule,
            tomorrow = tomorrowSchedule,
            settings = settings,
            latestTimetable = sampleTimetable
        )

        val asrBeforeAzan = events.firstOrNull { it.prayer == Prayer.ASR && it.kind == ReminderKind.BEFORE_AZAN }
        assertNull(asrBeforeAzan)

        val asrAtAzan = events.firstOrNull { it.prayer == Prayer.ASR && it.kind == ReminderKind.AT_AZAN }
        assertNotNull(asrAtAzan)
    }
}
