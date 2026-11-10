package com.syedmsidsm.prayerreminder.domain

import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.domain.model.TimetableDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToInt

data class ValidationFlag(
    val dayIndex: Int,
    val date: String,
    val field: String, // "fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha", "weekday", "hijriDay", "date"
    val currentValue: String,
    val suggestion: String?,
    val reason: String
)

class TimetableValidator {

    companion object {
        val PRAYER_FIELDS = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")

        val PLAUSIBLE_RANGES = mapOf(
            "fajr" to (LocalTime.of(3, 30)..LocalTime.of(6, 30)),
            "sunrise" to (LocalTime.of(5, 0)..LocalTime.of(7, 30)),
            "dhuhr" to (LocalTime.of(11, 30)..LocalTime.of(13, 30)),
            "asr" to (LocalTime.of(14, 30)..LocalTime.of(17, 30)),
            "maghrib" to (LocalTime.of(17, 0)..LocalTime.of(20, 0)),
            "isha" to (LocalTime.of(18, 0)..LocalTime.of(21, 30))
        )
    }

    fun validate(timetable: Timetable): List<ValidationFlag> {
        val flags = mutableListOf<ValidationFlag>()
        val days = timetable.days

        // Rule 1: Table length (must be 29 or 30 days)
        if (days.size !in 29..30) {
            flags.add(
                ValidationFlag(
                    dayIndex = 0,
                    date = timetable.firstDate,
                    field = "length",
                    currentValue = "${days.size} days",
                    suggestion = "29 or 30 days",
                    reason = "An Islamic month must have 29 or 30 days."
                )
            )
        }

        // Validate each day individually
        for (i in days.indices) {
            val day = days[i]

            // Check Gregorian date and weekday
            val parsedDate = runCatching { LocalDate.parse(day.date) }.getOrNull()
            if (parsedDate != null) {
                val expectedDayName = getAbbreviatedDay(parsedDate.dayOfWeek)
                if (!day.weekday.equals(expectedDayName, ignoreCase = true)) {
                    flags.add(
                        ValidationFlag(
                            dayIndex = i,
                            date = day.date,
                            field = "weekday",
                            currentValue = day.weekday,
                            suggestion = expectedDayName,
                            reason = "Weekday does not match date ${day.date} ($expectedDayName)."
                        )
                    )
                }

                // Check date continuity
                if (i > 0) {
                    val prevDate = runCatching { LocalDate.parse(days[i - 1].date) }.getOrNull()
                    if (prevDate != null && ChronoUnit.DAYS.between(prevDate, parsedDate) != 1L) {
                        flags.add(
                            ValidationFlag(
                                dayIndex = i,
                                date = day.date,
                                field = "date",
                                currentValue = day.date,
                                suggestion = prevDate.plusDays(1).toString(),
                                reason = "Date is not consecutive with previous day."
                            )
                        )
                    }
                }
            }

            // Check Hijri sequence
            val expectedHijri = i + 1
            if (day.hijriDay != expectedHijri) {
                flags.add(
                    ValidationFlag(
                        dayIndex = i,
                        date = day.date,
                        field = "hijriDay",
                        currentValue = day.hijriDay.toString(),
                        suggestion = expectedHijri.toString(),
                        reason = "Islamic day must be consecutive ($expectedHijri)."
                    )
                )
            }

            // Check time cells
            val parsedTimes = mutableMapOf<String, LocalTime?>()
            for (field in PRAYER_FIELDS) {
                val timeStr = day.times[field]
                if (timeStr.isNullOrBlank()) {
                    flags.add(
                        ValidationFlag(
                            dayIndex = i,
                            date = day.date,
                            field = field,
                            currentValue = "",
                            suggestion = calculateInterpolatedSuggestion(days, i, field),
                            reason = "Missing time for ${field.replaceFirstChar { it.uppercase() }}."
                        )
                    )
                    parsedTimes[field] = null
                } else {
                    val parsed = runCatching { LocalTime.parse(timeStr) }.getOrNull()
                    if (parsed == null) {
                        flags.add(
                            ValidationFlag(
                                dayIndex = i,
                                date = day.date,
                                field = field,
                                currentValue = timeStr,
                                suggestion = calculateInterpolatedSuggestion(days, i, field),
                                reason = "Unparseable time: '$timeStr'."
                            )
                        )
                        parsedTimes[field] = null
                    } else {
                        parsedTimes[field] = parsed

                        // Check plausible range
                        val range = PLAUSIBLE_RANGES[field]
                        if (range != null && (parsed.isBefore(range.start) || parsed.isAfter(range.endInclusive))) {
                            flags.add(
                                ValidationFlag(
                                    dayIndex = i,
                                    date = day.date,
                                    field = field,
                                    currentValue = timeStr,
                                    suggestion = calculateInterpolatedSuggestion(days, i, field),
                                    reason = "$timeStr is outside plausible range for ${field.replaceFirstChar { it.uppercase() }}."
                                )
                            )
                        }
                    }
                }
            }

            // Check chronological order within day (Fajr < Sunrise < Dhuhr < Asr < Maghrib < Isha)
            for (p in 0 until PRAYER_FIELDS.size - 1) {
                val f1 = PRAYER_FIELDS[p]
                val f2 = PRAYER_FIELDS[p + 1]
                val t1 = parsedTimes[f1]
                val t2 = parsedTimes[f2]
                if (t1 != null && t2 != null && !t1.isBefore(t2)) {
                    flags.add(
                        ValidationFlag(
                            dayIndex = i,
                            date = day.date,
                            field = f2,
                            currentValue = day.times[f2] ?: "",
                            suggestion = calculateInterpolatedSuggestion(days, i, f2),
                            reason = "${f2.replaceFirstChar { it.uppercase() }} must be after ${f1.replaceFirstChar { it.uppercase() }}."
                        )
                    )
                }
            }

            // Check smooth day-to-day transition (flag if differs > 3 min from median of neighbours)
            for (field in PRAYER_FIELDS) {
                val currentTime = parsedTimes[field] ?: continue
                val neighbourTimes = mutableListOf<LocalTime>()
                for (offset in listOf(-2, -1, 1, 2)) {
                    val nIndex = i + offset
                    if (nIndex in days.indices) {
                        val nTime = runCatching { LocalTime.parse(days[nIndex].times[field]) }.getOrNull()
                        if (nTime != null) neighbourTimes.add(nTime)
                    }
                }

                if (neighbourTimes.size >= 2) {
                    val medianMinutes = computeMedianMinutes(neighbourTimes)
                    val currentMinutes = currentTime.toSecondOfDay() / 60
                    val diff = abs(currentMinutes - medianMinutes)
                    if (diff > 3) {
                        val beforeStr = if (i > 0) days[i - 1].times[field] ?: "" else ""
                        val afterStr = if (i < days.size - 1) days[i + 1].times[field] ?: "" else ""
                        val suggestion = calculateInterpolatedSuggestion(days, i, field)

                        flags.add(
                            ValidationFlag(
                                dayIndex = i,
                                date = day.date,
                                field = field,
                                currentValue = day.times[field] ?: "",
                                suggestion = suggestion,
                                reason = "The day before is $beforeStr and the day after is $afterStr. Differs by $diff minutes from neighbouring days."
                            )
                        )
                    }
                }
            }
        }

        // Deduplicate flags for same dayIndex and field
        return flags.distinctBy { Pair(it.dayIndex, it.field) }
    }

    private fun calculateInterpolatedSuggestion(days: List<TimetableDay>, dayIndex: Int, field: String): String? {
        val prev = (dayIndex - 1 downTo 0)
            .mapNotNull { runCatching { LocalTime.parse(days[it].times[field]) }.getOrNull() }
            .firstOrNull()
        val next = (dayIndex + 1 until days.size)
            .mapNotNull { runCatching { LocalTime.parse(days[it].times[field]) }.getOrNull() }
            .firstOrNull()

        return when {
            prev != null && next != null -> {
                val avgSec = (prev.toSecondOfDay().toLong() + next.toSecondOfDay().toLong()) / 2
                val totalMin = (avgSec / 60).toInt()
                val hour = totalMin / 60
                val min = totalMin % 60
                "%02d:%02d".format(hour, min)
            }
            prev != null -> "%02d:%02d".format(prev.hour, prev.minute)
            next != null -> "%02d:%02d".format(next.hour, next.minute)
            else -> null
        }
    }

    private fun computeMedianMinutes(times: List<LocalTime>): Int {
        val minutesList = times.map { it.toSecondOfDay() / 60 }.sorted()
        val mid = minutesList.size / 2
        return if (minutesList.size % 2 == 1) {
            minutesList[mid]
        } else {
            (minutesList[mid - 1] + minutesList[mid]) / 2
        }
    }

    private fun getAbbreviatedDay(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "Mon"
            DayOfWeek.TUESDAY -> "Tue"
            DayOfWeek.WEDNESDAY -> "Wed"
            DayOfWeek.THURSDAY -> "Thu"
            DayOfWeek.FRIDAY -> "Fri"
            DayOfWeek.SATURDAY -> "Sat"
            DayOfWeek.SUNDAY -> "Sun"
        }
    }
}
