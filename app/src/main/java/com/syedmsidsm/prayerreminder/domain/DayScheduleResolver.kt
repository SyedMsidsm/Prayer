package com.syedmsidsm.prayerreminder.domain

import com.syedmsidsm.prayerreminder.domain.model.DaySchedule
import com.syedmsidsm.prayerreminder.domain.model.HijriDate
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.ScheduleSource
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.domain.model.TimetableDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class DayScheduleResolver {

    /**
     * Resolves the schedule for a given date D against available timetables.
     */
    fun resolve(
        date: LocalDate,
        timetables: List<Timetable>,
        jamaatRules: Map<String, JamaatRule> = emptyMap()
    ): DaySchedule {
        val isFriday = date.dayOfWeek == DayOfWeek.FRIDAY
        val dateIso = date.toString()

        // 1. Check if an active/saved table contains date D
        for (table in timetables) {
            val matchingDay = table.days.firstOrNull { it.date == dateIso }
            if (matchingDay != null) {
                val azanTimes = parseAzanTimes(matchingDay.times)
                val hijriDate = HijriDate(
                    day = matchingDay.hijriDay,
                    month = table.hijriMonth,
                    monthName = table.hijriMonthName,
                    year = table.hijriYear
                )
                val (jamaatTimes, warnings) = computeJamaatTimes(azanTimes, isFriday, jamaatRules)
                return DaySchedule(
                    date = date,
                    hijri = hijriDate,
                    azan = azanTimes,
                    jamaat = jamaatTimes,
                    isFriday = isFriday,
                    source = ScheduleSource.TABLE,
                    fixedTimeBeforeAzanWarnings = warnings
                )
            }
        }

        // 2. Check if a table ended <= 7 days before D -> ESTIMATED_CARRY_FORWARD
        for (table in timetables) {
            val lastDate = runCatching { LocalDate.parse(table.lastDate) }.getOrNull()
            if (lastDate != null && date.isAfter(lastDate)) {
                val daysDiff = ChronoUnit.DAYS.between(lastDate, date)
                if (daysDiff in 1..7) {
                    val lastDay = table.days.lastOrNull()
                    if (lastDay != null) {
                        val azanTimes = parseAzanTimes(lastDay.times)
                        val (jamaatTimes, warnings) = computeJamaatTimes(azanTimes, isFriday, jamaatRules)
                        return DaySchedule(
                            date = date,
                            hijri = null, // Hijri date unknown on estimated days
                            azan = azanTimes,
                            jamaat = jamaatTimes,
                            isFriday = isFriday,
                            source = ScheduleSource.ESTIMATED_CARRY_FORWARD,
                            fixedTimeBeforeAzanWarnings = warnings
                        )
                    }
                }
            }
        }

        // 3. Check within +-3 days of D minus 1 year (or 354 days) -> ESTIMATED_LAST_YEAR
        val candidateDates = listOf(
            date.minusYears(1),
            date.minusDays(354)
        )
        for (targetDate in candidateDates) {
            var closestDay: TimetableDay? = null
            var minDiff = Long.MAX_VALUE
            for (table in timetables) {
                for (day in table.days) {
                    val d = runCatching { LocalDate.parse(day.date) }.getOrNull() ?: continue
                    val diff = abs(ChronoUnit.DAYS.between(d, targetDate))
                    if (diff <= 3 && diff < minDiff) {
                        minDiff = diff
                        closestDay = day
                    }
                }
            }
            if (closestDay != null) {
                val azanTimes = parseAzanTimes(closestDay.times)
                val (jamaatTimes, warnings) = computeJamaatTimes(azanTimes, isFriday, jamaatRules)
                return DaySchedule(
                    date = date,
                    hijri = null,
                    azan = azanTimes,
                    jamaat = jamaatTimes,
                    isFriday = isFriday,
                    source = ScheduleSource.ESTIMATED_LAST_YEAR,
                    fixedTimeBeforeAzanWarnings = warnings
                )
            }
        }

        // 4. No schedule available
        return DaySchedule(
            date = date,
            hijri = null,
            azan = emptyMap(),
            jamaat = emptyMap(),
            isFriday = isFriday,
            source = ScheduleSource.NONE
        )
    }

    private fun parseAzanTimes(times: Map<String, String>): Map<Prayer, LocalTime> {
        val result = mutableMapOf<Prayer, LocalTime>()
        times["fajr"]?.let { result[Prayer.FAJR] = LocalTime.parse(it) }
        times["sunrise"]?.let { result[Prayer.SUNRISE] = LocalTime.parse(it) }
        times["dhuhr"]?.let { result[Prayer.DHUHR] = LocalTime.parse(it) }
        times["asr"]?.let { result[Prayer.ASR] = LocalTime.parse(it) }
        times["maghrib"]?.let { result[Prayer.MAGHRIB] = LocalTime.parse(it) }
        times["isha"]?.let { result[Prayer.ISHA] = LocalTime.parse(it) }
        return result
    }

    private fun computeJamaatTimes(
        azanTimes: Map<Prayer, LocalTime>,
        isFriday: Boolean,
        jamaatRules: Map<String, JamaatRule>
    ): Pair<Map<Prayer, LocalTime>, Set<Prayer>> {
        val jamaatTimes = mutableMapOf<Prayer, LocalTime>()
        val warnings = mutableSetOf<Prayer>()

        for (prayer in Prayer.entries) {
            if (!prayer.hasJamaat) continue
            val azan = azanTimes[prayer] ?: continue

            val ruleKey = if (prayer == Prayer.DHUHR && isFriday) "JUMUAH" else prayer.name
            val rule = jamaatRules[ruleKey] ?: JamaatRule.AfterAzan(15)

            when (rule) {
                is JamaatRule.AfterAzan -> {
                    val minutes = rule.minutes.coerceIn(1, 60)
                    jamaatTimes[prayer] = azan.plusMinutes(minutes.toLong())
                }
                is JamaatRule.Fixed -> {
                    // Sanity check: if fixed time is <= azan time, fallback to azan + 15 min and warn
                    if (!rule.time.isAfter(azan)) {
                        warnings.add(prayer)
                        jamaatTimes[prayer] = azan.plusMinutes(15)
                    } else {
                        jamaatTimes[prayer] = rule.time
                    }
                }
            }
        }

        return Pair(jamaatTimes, warnings)
    }
}
