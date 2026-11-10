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
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class DayScheduleResolver(
    private val calculator: PrayerTimeCalculator = PrayerTimeCalculator()
) {

    /**
     * Resolves the schedule for a given date D against available timetables,
     * falling back to the calibrated offline astronomical calculator so the app
     * always has accurate times for any date.
     */
    fun resolve(
        date: LocalDate,
        timetables: List<Timetable>,
        jamaatRules: Map<String, JamaatRule> = emptyMap(),
        useCalculatedFallback: Boolean = true
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
                            hijri = null,
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

        // 4. Calibrated Offline Astronomical Calculator (always available for any date)
        if (useCalculatedFallback) {
            val calibration = calculator.calibrateFrom(timetables)
            val azanTimes = calculator.compute(date, calibration)
            val (jamaatTimes, warnings) = computeJamaatTimes(azanTimes, isFriday, jamaatRules)
            return DaySchedule(
                date = date,
                hijri = estimateHijriDate(date, timetables),
                azan = azanTimes,
                jamaat = jamaatTimes,
                isFriday = isFriday,
                source = ScheduleSource.CALCULATED,
                fixedTimeBeforeAzanWarnings = warnings
            )
        }

        // 5. No schedule available (only when fallback is explicitly disabled)
        return DaySchedule(
            date = date,
            hijri = null,
            azan = emptyMap(),
            jamaat = emptyMap(),
            isFriday = isFriday,
            source = ScheduleSource.NONE
        )
    }

    /**
     * Estimates the Hijri date for [date] by anchoring to the closest imported timetable
     * if within 30 days, or falling back to the Umm al-Qura calendar (adjusted -1 day for India/Bhatkal).
     */
    fun estimateHijriDate(date: LocalDate, timetables: List<Timetable> = emptyList()): HijriDate {
        for (table in timetables) {
            val firstDate = runCatching { LocalDate.parse(table.firstDate) }.getOrNull()
            if (firstDate != null) {
                val diff = ChronoUnit.DAYS.between(firstDate, date).toInt()
                if (diff in -30..-1) {
                    val prevMonth = if (table.hijriMonth == 1) 12 else table.hijriMonth - 1
                    val prevYear = if (table.hijriMonth == 1) table.hijriYear - 1 else table.hijriYear
                    val dayInPrev = (30 + diff).coerceIn(1, 30)
                    return HijriDate(
                        day = dayInPrev,
                        month = prevMonth,
                        monthName = HijriNames.getMonthName(prevMonth, "en"),
                        year = prevYear
                    )
                }
            }
        }
        return runCatching {
            // India moon sighting typically lags Umm al-Qura by 1 day
            val hijrah = HijrahDate.from(date.minusDays(1))
            val day = hijrah.get(ChronoField.DAY_OF_MONTH)
            val month = hijrah.get(ChronoField.MONTH_OF_YEAR)
            val year = hijrah.get(ChronoField.YEAR)
            HijriDate(
                day = day,
                month = month,
                monthName = HijriNames.getMonthName(month, "en"),
                year = year
            )
        }.getOrElse {
            HijriDate(1, 6, "Jumada al-Akhirah", 1448)
        }
    }

    private fun parseAzanTimes(times: Map<String, String>): Map<Prayer, LocalTime> {
        val result = mutableMapOf<Prayer, LocalTime>()
        times["fajr"]?.let { runCatching { LocalTime.parse(it) }.getOrNull()?.let { t -> result[Prayer.FAJR] = t } }
        times["sunrise"]?.let { runCatching { LocalTime.parse(it) }.getOrNull()?.let { t -> result[Prayer.SUNRISE] = t } }
        times["dhuhr"]?.let { runCatching { LocalTime.parse(it) }.getOrNull()?.let { t -> result[Prayer.DHUHR] = t } }
        times["asr"]?.let { runCatching { LocalTime.parse(it) }.getOrNull()?.let { t -> result[Prayer.ASR] = t } }
        times["maghrib"]?.let { runCatching { LocalTime.parse(it) }.getOrNull()?.let { t -> result[Prayer.MAGHRIB] = t } }
        times["isha"]?.let { runCatching { LocalTime.parse(it) }.getOrNull()?.let { t -> result[Prayer.ISHA] = t } }
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
