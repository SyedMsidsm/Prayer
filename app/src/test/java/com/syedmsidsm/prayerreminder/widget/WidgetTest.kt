package com.syedmsidsm.prayerreminder.widget

import com.syedmsidsm.prayerreminder.domain.DayScheduleResolver
import com.syedmsidsm.prayerreminder.domain.NextPrayerResolver
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.NextPrayerPhase
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class WidgetTest {

    private lateinit var sampleTimetable: Timetable
    private val dayScheduleResolver = DayScheduleResolver()
    private val nextPrayerResolver = NextPrayerResolver()
    private val zoneId = ZoneId.of("Asia/Kolkata")

    @Before
    fun setUp() {
        val file = File("src/main/assets/default_timetable.json").takeIf { it.exists() }
            ?: File("app/src/main/assets/default_timetable.json")
        val json = Json { ignoreUnknownKeys = true }
        sampleTimetable = json.decodeFromString(file.readText())
    }

    @Test
    fun testPrayerNameLocalization() {
        assertEquals("Fajr", WidgetStateBuilder.getLocalizedPrayerName(Prayer.FAJR, false, "en"))
        assertEquals("الفجر", WidgetStateBuilder.getLocalizedPrayerName(Prayer.FAJR, false, "ar"))
        assertEquals("فجر", WidgetStateBuilder.getLocalizedPrayerName(Prayer.FAJR, false, "ur"))

        assertEquals("Asr", WidgetStateBuilder.getLocalizedPrayerName(Prayer.ASR, false, "en"))
        assertEquals("العصر", WidgetStateBuilder.getLocalizedPrayerName(Prayer.ASR, false, "ar"))
        assertEquals("عصر", WidgetStateBuilder.getLocalizedPrayerName(Prayer.ASR, false, "ur"))

        // Friday Dhuhr is Jumu'ah
        assertEquals("Jumu'ah", WidgetStateBuilder.getLocalizedPrayerName(Prayer.DHUHR, true, "en"))
        assertEquals("الجمعة", WidgetStateBuilder.getLocalizedPrayerName(Prayer.DHUHR, true, "ar"))
        assertEquals("جمعہ", WidgetStateBuilder.getLocalizedPrayerName(Prayer.DHUHR, true, "ur"))
    }

    @Test
    fun testPlaceLocalization() {
        assertEquals("Bhatkal", WidgetStateBuilder.getLocalizedPlace("Bhatkal", "en"))
        assertEquals("بهتكل", WidgetStateBuilder.getLocalizedPlace("Bhatkal", "ar"))
        assertEquals("بھٹکل", WidgetStateBuilder.getLocalizedPlace("Bhatkal", "ur"))

        assertEquals("Mangalore", WidgetStateBuilder.getLocalizedPlace("Mangalore", "en"))
    }

    @Test
    fun testMonthNameLocalization() {
        assertEquals("Shawwal", WidgetStateBuilder.getLocalizedMonthName("Shawwal", "en"))
        assertEquals("شوال", WidgetStateBuilder.getLocalizedMonthName("Shawwal", "ar"))
        assertEquals("شوال", WidgetStateBuilder.getLocalizedMonthName("Shawwal", "ur"))

        assertEquals("Ramadan", WidgetStateBuilder.getLocalizedMonthName("Ramadan", "en"))
        assertEquals("رمضان", WidgetStateBuilder.getLocalizedMonthName("Ramadan", "ar"))
    }

    @Test
    fun testRtlLanguageDetection() {
        val stateEn = WidgetState(
            hasTable = true,
            place = "Bhatkal",
            isEstimated = false,
            hijriDate = "5 Shawwal 1447",
            prayerName = "Asr",
            phase = WidgetPrayerPhase.BEFORE_AZAN,
            mainHeader = "Next · Asr",
            mainTime = "3:59 PM",
            secondaryTime = "Jamaat 4:14 PM",
            countdownTargetEpochMillis = 0L,
            rows = emptyList(),
            appLanguage = "en"
        )
        assertFalse(stateEn.isRtl)

        val stateAr = stateEn.copy(appLanguage = "ar")
        assertTrue(stateAr.isRtl)

        val stateUr = stateEn.copy(appLanguage = "ur")
        assertTrue(stateUr.isRtl)
    }

    @Test
    fun testWidgetBeforeAzanPhaseCalculation() {
        val date = LocalDate.of(2026, 3, 24) // Day 5: Asr Azan 15:59, Jamaat 16:14
        val tomorrow = date.plusDays(1)
        val defaultRules = mapOf<String, JamaatRule>()

        val todaySchedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable), defaultRules)
        val tomorrowSchedule = dayScheduleResolver.resolve(tomorrow, listOf(sampleTimetable), defaultRules)

        // 14:00 is before Asr azan (15:59)
        val nowTime = LocalTime.of(14, 0)
        val next = nextPrayerResolver.resolve(nowTime, todaySchedule, tomorrowSchedule)

        assertNotNull(next)
        assertEquals(Prayer.ASR, next!!.prayer)
        assertEquals(NextPrayerPhase.BEFORE_AZAN, next.phase)
        assertEquals(LocalTime.of(15, 59), next.azanTime)
        assertEquals(LocalTime.of(16, 14), next.jamaatTime)

        val aStr = TimeFormatter.format12Hour(next.azanTime, "AM", "PM")
        val jStr = TimeFormatter.format12Hour(next.jamaatTime, "AM", "PM")
        assertEquals("3:59 PM", aStr)
        assertEquals("4:14 PM", jStr)
    }

    @Test
    fun testWidgetBetweenAzanAndJamaatPhaseCalculation() {
        val date = LocalDate.of(2026, 3, 24) // Day 5: Asr Azan 15:59, Jamaat 16:14
        val tomorrow = date.plusDays(1)
        val defaultRules = mapOf<String, JamaatRule>()

        val todaySchedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable), defaultRules)
        val tomorrowSchedule = dayScheduleResolver.resolve(tomorrow, listOf(sampleTimetable), defaultRules)

        // 16:05 is between Asr azan (15:59) and jamaat (16:14)
        val nowTime = LocalTime.of(16, 5)
        val next = nextPrayerResolver.resolve(nowTime, todaySchedule, tomorrowSchedule)

        assertNotNull(next)
        assertEquals(Prayer.ASR, next!!.prayer)
        assertEquals(NextPrayerPhase.BETWEEN_AZAN_AND_JAMAAT, next.phase)
        assertEquals(LocalTime.of(16, 14), next.targetTime)
    }

    @Test
    fun testWidgetTableRowsPastAndNextHighlight() {
        val date = LocalDate.of(2026, 3, 24)
        val tomorrow = date.plusDays(1)
        val defaultRules = mapOf<String, JamaatRule>()

        val todaySchedule = dayScheduleResolver.resolve(date, listOf(sampleTimetable), defaultRules)
        val tomorrowSchedule = dayScheduleResolver.resolve(tomorrow, listOf(sampleTimetable), defaultRules)

        val nowTime = LocalTime.of(14, 0) // Before Asr azan (15:59), after Dhuhr jamaat (12:59)
        val next = nextPrayerResolver.resolve(nowTime, todaySchedule, tomorrowSchedule)

        val prayers = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
        val rows = prayers.map { p ->
            val azan = todaySchedule.azan[p]!!
            val jamaat = todaySchedule.jamaat[p]!!
            val isNext = (!next!!.isTomorrow && next.prayer == p)
            val isPast = nowTime.isAfter(jamaat)
            WidgetPrayerRow(
                prayer = p,
                prayerName = WidgetStateBuilder.getLocalizedPrayerName(p, false, "en"),
                azanFormatted = TimeFormatter.format12Hour(azan, "AM", "PM"),
                jamaatFormatted = TimeFormatter.format12Hour(jamaat, "AM", "PM"),
                isPast = isPast,
                isNext = isNext
            )
        }

        assertEquals(5, rows.size)

        // Fajr (5:33 jamaat) and Dhuhr (12:59 jamaat) should be past at 14:00
        val fajrRow = rows.first { it.prayer == Prayer.FAJR }
        assertTrue(fajrRow.isPast)
        assertFalse(fajrRow.isNext)

        val dhuhrRow = rows.first { it.prayer == Prayer.DHUHR }
        assertTrue(dhuhrRow.isPast)
        assertFalse(dhuhrRow.isNext)

        // Asr should be Next!
        val asrRow = rows.first { it.prayer == Prayer.ASR }
        assertFalse(asrRow.isPast)
        assertTrue(asrRow.isNext)

        // Maghrib and Isha should be upcoming (neither past nor next)
        val maghribRow = rows.first { it.prayer == Prayer.MAGHRIB }
        assertFalse(maghribRow.isPast)
        assertFalse(maghribRow.isNext)

        val ishaRow = rows.first { it.prayer == Prayer.ISHA }
        assertFalse(ishaRow.isPast)
        assertFalse(ishaRow.isNext)
    }
}
