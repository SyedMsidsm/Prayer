package com.syedmsidsm.prayerreminder.domain

import com.syedmsidsm.prayerreminder.domain.model.DaySchedule
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.NextPrayerPhase
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.ScheduleSource
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

class DomainResolverTest {

    private lateinit var sampleTimetable: Timetable
    private val dayScheduleResolver = DayScheduleResolver()
    private val nextPrayerResolver = NextPrayerResolver()

    @Before
    fun setUp() {
        val file = File("src/main/assets/default_timetable.json").takeIf { it.exists() }
            ?: File("app/src/main/assets/default_timetable.json")
        val jsonString = file.readText()
        val json = Json { ignoreUnknownKeys = true }
        sampleTimetable = json.decodeFromString(jsonString)
    }

    @Test
    fun testTimetableSerializationAndGoldenData() {
        assertEquals("Bhatkal", sampleTimetable.place)
        assertEquals(10, sampleTimetable.hijriMonth)
        assertEquals("Shawwal", sampleTimetable.hijriMonthName)
        assertEquals(1447, sampleTimetable.hijriYear)
        assertEquals(30, sampleTimetable.days.size)

        // Verify Tuesday 24 March 2026 (Day 5)
        val day5 = sampleTimetable.days.first { it.date == "2026-03-24" }
        assertEquals(5, day5.hijriDay)
        assertEquals("Tue", day5.weekday)
        assertEquals("05:18", day5.times["fajr"])
        assertEquals("06:33", day5.times["sunrise"])
        assertEquals("12:44", day5.times["dhuhr"])
        assertEquals("15:59", day5.times["asr"])
        assertEquals("18:46", day5.times["maghrib"])
        assertEquals("19:50", day5.times["isha"])
    }

    @Test
    fun testDayScheduleResolverFor24March2026() {
        val date = LocalDate.of(2026, 3, 24)
        val schedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable))

        assertEquals(ScheduleSource.TABLE, schedule.source)
        assertFalse(schedule.isFriday)
        assertNotNull(schedule.hijri)
        assertEquals(5, schedule.hijri?.day)
        assertEquals(10, schedule.hijri?.month)
        assertEquals(1447, schedule.hijri?.year)

        // Azan times
        assertEquals(LocalTime.of(5, 18), schedule.azan[Prayer.FAJR])
        assertEquals(LocalTime.of(6, 33), schedule.azan[Prayer.SUNRISE])
        assertEquals(LocalTime.of(12, 44), schedule.azan[Prayer.DHUHR])
        assertEquals(LocalTime.of(15, 59), schedule.azan[Prayer.ASR])
        assertEquals(LocalTime.of(18, 46), schedule.azan[Prayer.MAGHRIB])
        assertEquals(LocalTime.of(19, 50), schedule.azan[Prayer.ISHA])

        // Default jamaat (+15 min)
        assertEquals(LocalTime.of(5, 33), schedule.jamaat[Prayer.FAJR])
        assertEquals(LocalTime.of(12, 59), schedule.jamaat[Prayer.DHUHR])
        assertEquals(LocalTime.of(16, 14), schedule.jamaat[Prayer.ASR])
        assertEquals(LocalTime.of(19, 1), schedule.jamaat[Prayer.MAGHRIB])
        assertEquals(LocalTime.of(20, 5), schedule.jamaat[Prayer.ISHA])
        assertNull(schedule.jamaat[Prayer.SUNRISE]) // Sunrise has no jamaat
    }

    @Test
    fun testFridayJumuahResolution() {
        val friday = LocalDate.of(2026, 3, 20)
        val schedule = dayScheduleResolver.resolve(friday, listOf(sampleTimetable))
        assertTrue(schedule.isFriday)
    }

    @Test
    fun testEstimatedCarryForward() {
        // Table ends on 2026-04-18, test 2026-04-20 (2 days after)
        val estimatedDate = LocalDate.of(2026, 4, 20)
        val schedule = dayScheduleResolver.resolve(estimatedDate, listOf(sampleTimetable))

        assertEquals(ScheduleSource.ESTIMATED_CARRY_FORWARD, schedule.source)
        assertTrue(schedule.source.isEstimated)
        assertNull(schedule.hijri) // Hijri unknown on estimated days
        // Carries forward last day's times (day 30, 2026-04-18)
        val lastDay = sampleTimetable.days.last()
        assertEquals(LocalTime.parse(lastDay.times["fajr"]), schedule.azan[Prayer.FAJR])
    }

    @Test
    fun testFixedJamaatRuleAndSanityWarning() {
        val date = LocalDate.of(2026, 3, 24)

        // Custom rule: Isha fixed at 20:15 (after 19:50 azan) -> valid
        // Asr fixed at 15:45 (before 15:59 azan) -> invalid, must fallback to 15:59 + 15 min = 16:14 and warn
        val rules = mapOf(
            "ISHA" to JamaatRule.Fixed(LocalTime.of(20, 15)),
            "ASR" to JamaatRule.Fixed(LocalTime.of(15, 45))
        )
        val schedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable), rules)

        assertEquals(LocalTime.of(20, 15), schedule.jamaat[Prayer.ISHA])
        assertEquals(LocalTime.of(16, 14), schedule.jamaat[Prayer.ASR])
        assertTrue(schedule.fixedTimeBeforeAzanWarnings.contains(Prayer.ASR))
        assertFalse(schedule.fixedTimeBeforeAzanWarnings.contains(Prayer.ISHA))
    }

    @Test
    fun testNextPrayerBeforeAzan() {
        val date = LocalDate.of(2026, 3, 24)
        val schedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable))

        // Time: 14:35 (before Asr at 15:59)
        val now = LocalTime.of(14, 35)
        val next = nextPrayerResolver.resolve(now, schedule)

        assertNotNull(next)
        assertEquals(Prayer.ASR, next?.prayer)
        assertEquals(NextPrayerPhase.BEFORE_AZAN, next?.phase)
        assertFalse(next!!.isTomorrow)
        assertEquals(LocalTime.of(15, 59), next.targetTime)
        assertEquals(84, next.remainingDuration.toMinutes()) // 1 hr 24 min
    }

    @Test
    fun testNextPrayerBetweenAzanAndJamaat() {
        val date = LocalDate.of(2026, 3, 24)
        val schedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable))

        // Time: 16:05 (between Asr azan 15:59 and Asr jamaat 16:14)
        val now = LocalTime.of(16, 5)
        val next = nextPrayerResolver.resolve(now, schedule)

        assertNotNull(next)
        assertEquals(Prayer.ASR, next?.prayer)
        assertEquals(NextPrayerPhase.BETWEEN_AZAN_AND_JAMAAT, next?.phase)
        assertFalse(next!!.isTomorrow)
        assertEquals(LocalTime.of(16, 14), next.targetTime)
        assertEquals(9, next.remainingDuration.toMinutes())
    }

    @Test
    fun testNextPrayerRolloverToTomorrowFajr() {
        val date = LocalDate.of(2026, 3, 24)
        val todaySchedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable))
        val tomorrowSchedule = dayScheduleResolver.resolve(date.plusDays(1), listOf(sampleTimetable))

        // Time: 21:00 (after Isha jamaat at 20:05)
        val now = LocalTime.of(21, 0)
        val next = nextPrayerResolver.resolve(now, todaySchedule, tomorrowSchedule)

        assertNotNull(next)
        assertEquals(Prayer.FAJR, next?.prayer)
        assertTrue(next!!.isTomorrow)
        assertEquals(NextPrayerPhase.BEFORE_AZAN, next.phase)
        // Tomorrow 25 March Fajr azan is 05:17
        assertEquals(LocalTime.of(5, 17), next.targetTime)
        // 3 hours to midnight + 5 hours 17 min = 8 hours 17 minutes = 497 minutes
        assertEquals(497, next.remainingDuration.toMinutes())
    }
}
