package com.syedmsidsm.prayerreminder.reminders

import com.syedmsidsm.prayerreminder.domain.model.DaySchedule
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.ScheduleSource
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class ReminderSettingsSnapshot(
    val remBeforeAzanMin: Int = 10,
    val remJumuahBeforeAzanMin: Int = 30,
    val remAtAzan: Boolean = true,
    val remBeforeJamaatMin: Int = 5,
    val matrix: Map<Pair<String, String>, Boolean> = emptyMap(),
    val remBeforeAzanOffsets: Set<Int> = emptySet(),
    val remBeforeJamaatOffsets: Set<Int> = emptySet(),
    val remAfterPrayerOffsets: Set<Int> = emptySet(),
    val remJumuahBeforeAzanOffsets: Set<Int> = emptySet(),
    val remSunriseBeforeOffsets: Set<Int> = setOf(15),
    val remSunriseAt: Boolean = true
) {
    fun isEnabled(prayerKey: String, kind: ReminderKind): Boolean {
        if (prayerKey == "SUNRISE") {
            if (kind == ReminderKind.BEFORE_AZAN) return false // No before-sunrise (time left) reminders
            if (kind == ReminderKind.AT_AZAN) return remSunriseAt && (matrix[Pair("SUNRISE", "AT_AZAN")] ?: true)
            return false // Statically false for BEFORE_JAMAAT and AFTER_PRAYER!
        }
        val kindStr = when (kind) {
            ReminderKind.BEFORE_AZAN -> "BEFORE_AZAN"
            ReminderKind.AT_AZAN -> "AT_AZAN"
            ReminderKind.BEFORE_JAMAAT -> "BEFORE_JAMAAT"
            ReminderKind.AFTER_PRAYER -> "AFTER_PRAYER"
            else -> return true
        }
        return matrix[Pair(prayerKey, kindStr)] ?: true
    }
}

class ReminderPlanner {

    companion object {
        fun calculateCode(date: LocalDate, prayer: Prayer, kind: ReminderKind, offsetMinutes: Int = 0): Int {
            // Unique deterministic integer code: Day of year * 10000 + prayer ordinal * 100 + kind ordinal * 20 + offset
            return (date.dayOfYear * 10000) + (prayer.ordinal * 100) + (kind.ordinal * 20) + (offsetMinutes % 20)
        }

        fun calculateTableExpiringCode(date: LocalDate): Int {
            return 900000 + date.dayOfYear
        }

        fun calculateMidnightCode(date: LocalDate): Int {
            return 800000 + date.dayOfYear
        }

        const val TEST_REMINDER_CODE = 777777
    }

    /**
     * Plans reminders for the window now -> now + 36h per Spec Section 10.1.
     */
    fun plan(
        now: ZonedDateTime,
        today: DaySchedule,
        tomorrow: DaySchedule,
        settings: ReminderSettingsSnapshot,
        latestTimetable: Timetable? = null
    ): List<ScheduledReminderEvent> {
        val events = mutableListOf<ScheduledReminderEvent>()
        val horizon = now.plusHours(36)
        val cutoff = now.plusSeconds(5)

        val days = listOf(today, tomorrow)

        for (day in days) {
            if (day.source == ScheduleSource.NONE) continue

            val prayers = listOf(Prayer.FAJR, Prayer.SUNRISE, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
            for (prayer in prayers) {
                val isJumuah = day.isFriday && prayer == Prayer.DHUHR
                val isSunrise = prayer == Prayer.SUNRISE
                val prayerKey = if (isJumuah) "JUMUAH" else prayer.name
                val azan = day.azan[prayer] ?: continue
                val jamaat = day.jamaat[prayer]

                val beforeAzanMin = if (isJumuah) settings.remJumuahBeforeAzanMin else settings.remBeforeAzanMin
                val azanOffsets = when {
                    isJumuah -> {
                        if (settings.remJumuahBeforeAzanOffsets.isNotEmpty()) settings.remJumuahBeforeAzanOffsets
                        else setOf(beforeAzanMin).filter { it > 0 }.toSet()
                    }
                    isSunrise -> {
                        emptySet() // No before-sunrise (time left) reminders
                    }
                    else -> {
                        if (settings.remBeforeAzanOffsets.isNotEmpty()) settings.remBeforeAzanOffsets
                        else setOf(beforeAzanMin).filter { it > 0 }.toSet()
                    }
                }

                // 1. BEFORE_AZAN / BEFORE_SUNRISE (Support multiple reminder intervals)
                for (offset in azanOffsets) {
                    if (offset > 0 && settings.isEnabled(prayerKey, ReminderKind.BEFORE_AZAN)) {
                        val fireTime = day.date.atTime(azan).minusMinutes(offset.toLong()).atZone(now.zone)
                        if (fireTime.isAfter(cutoff) && !fireTime.isAfter(horizon)) {
                            events.add(
                                ScheduledReminderEvent(
                                    code = calculateCode(day.date, prayer, ReminderKind.BEFORE_AZAN, offset),
                                    fireAtEpochSeconds = fireTime.toEpochSecond(),
                                    prayer = prayer,
                                    kind = ReminderKind.BEFORE_AZAN,
                                    isJumuah = isJumuah,
                                    minutesOffset = offset,
                                    azanTimeIso = azan.toString(),
                                    jamaatTimeIso = jamaat?.toString(),
                                    isEstimated = day.source.isEstimated
                                )
                            )
                        }
                    }
                }

                // 2. AT_AZAN / AT_SUNRISE
                val atTimeAllowed = if (isSunrise) settings.remSunriseAt else settings.remAtAzan
                if (atTimeAllowed && settings.isEnabled(prayerKey, ReminderKind.AT_AZAN)) {
                    val fireTime = day.date.atTime(azan).atZone(now.zone)
                    if (fireTime.isAfter(cutoff) && !fireTime.isAfter(horizon)) {
                        events.add(
                            ScheduledReminderEvent(
                                code = calculateCode(day.date, prayer, ReminderKind.AT_AZAN, 0),
                                fireAtEpochSeconds = fireTime.toEpochSecond(),
                                prayer = prayer,
                                kind = ReminderKind.AT_AZAN,
                                isJumuah = isJumuah,
                                minutesOffset = 0,
                                azanTimeIso = azan.toString(),
                                jamaatTimeIso = jamaat?.toString(),
                                isEstimated = day.source.isEstimated
                            )
                        )
                    }
                }

                // NEVER schedule BEFORE_JAMAAT, AFTER_PRAYER, or PHASE for Sunrise!
                if (isSunrise) continue

                // 3. BEFORE_JAMAAT (Support multiple reminder intervals)
                val jamaatOffsets = if (settings.remBeforeJamaatOffsets.isNotEmpty()) {
                    settings.remBeforeJamaatOffsets
                } else {
                    setOf(settings.remBeforeJamaatMin).filter { it > 0 }.toSet()
                }

                for (offset in jamaatOffsets) {
                    if (jamaat != null && offset > 0 && settings.isEnabled(prayerKey, ReminderKind.BEFORE_JAMAAT)) {
                        val fireTime = day.date.atTime(jamaat).minusMinutes(offset.toLong()).atZone(now.zone)
                        val fireLocalTime = fireTime.toLocalTime()
                        if (fireLocalTime.isAfter(azan) && fireTime.isAfter(cutoff) && !fireTime.isAfter(horizon)) {
                            events.add(
                                ScheduledReminderEvent(
                                    code = calculateCode(day.date, prayer, ReminderKind.BEFORE_JAMAAT, offset),
                                    fireAtEpochSeconds = fireTime.toEpochSecond(),
                                    prayer = prayer,
                                    kind = ReminderKind.BEFORE_JAMAAT,
                                    isJumuah = isJumuah,
                                    minutesOffset = offset,
                                    azanTimeIso = azan.toString(),
                                    jamaatTimeIso = jamaat.toString(),
                                    isEstimated = day.source.isEstimated
                                )
                            )
                        }
                    }
                }

                // 4. AFTER_PRAYER (Support multiple reminder intervals after prayer / jamaat)
                for (offset in settings.remAfterPrayerOffsets) {
                    if (offset > 0 && settings.isEnabled(prayerKey, ReminderKind.AFTER_PRAYER)) {
                        val refTime = jamaat ?: azan
                        val fireTime = day.date.atTime(refTime).plusMinutes(offset.toLong()).atZone(now.zone)
                        if (fireTime.isAfter(cutoff) && !fireTime.isAfter(horizon)) {
                            events.add(
                                ScheduledReminderEvent(
                                    code = calculateCode(day.date, prayer, ReminderKind.AFTER_PRAYER, offset),
                                    fireAtEpochSeconds = fireTime.toEpochSecond(),
                                    prayer = prayer,
                                    kind = ReminderKind.AFTER_PRAYER,
                                    isJumuah = isJumuah,
                                    minutesOffset = offset,
                                    azanTimeIso = azan.toString(),
                                    jamaatTimeIso = jamaat?.toString(),
                                    isEstimated = day.source.isEstimated
                                )
                            )
                        }
                    }
                }

                // 5. PHASE change at jamaat time (silent refresh for widgets and next prayer)
                if (jamaat != null) {
                    val fireTime = day.date.atTime(jamaat).atZone(now.zone)
                    if (fireTime.isAfter(cutoff) && !fireTime.isAfter(horizon)) {
                        events.add(
                            ScheduledReminderEvent(
                                code = calculateCode(day.date, prayer, ReminderKind.PHASE),
                                fireAtEpochSeconds = fireTime.toEpochSecond(),
                                prayer = prayer,
                                kind = ReminderKind.PHASE,
                                isJumuah = isJumuah,
                                azanTimeIso = azan.toString(),
                                jamaatTimeIso = jamaat.toString(),
                                isEstimated = day.source.isEstimated
                            )
                        )
                    }
                }
            }
        }

        // 4. TABLE_EXPIRING at 9:00 AM on each of the last 3 days
        if (latestTimetable != null) {
            val lastDate = runCatching { LocalDate.parse(latestTimetable.lastDate) }.getOrNull()
            if (lastDate != null) {
                val last3Days = listOf(lastDate.minusDays(2), lastDate.minusDays(1), lastDate)
                for (d in last3Days) {
                    val fireTime = d.atTime(9, 0).atZone(now.zone)
                    if (fireTime.isAfter(cutoff) && !fireTime.isAfter(horizon)) {
                        val daysLeft = ChronoUnit.DAYS.between(d, lastDate).toInt()
                        events.add(
                            ScheduledReminderEvent(
                                code = calculateTableExpiringCode(d),
                                fireAtEpochSeconds = fireTime.toEpochSecond(),
                                prayer = null,
                                kind = ReminderKind.TABLE_EXPIRING,
                                tableMonthName = latestTimetable.hijriMonthName,
                                daysUntilExpiry = daysLeft
                            )
                        )
                    }
                }
            }
        }

        // 5. MIDNIGHT (00:00:30 tomorrow morning)
        val midnight = today.date.plusDays(1).atTime(0, 0, 30).atZone(now.zone)
        if (midnight.isAfter(cutoff) && !midnight.isAfter(horizon)) {
            events.add(
                ScheduledReminderEvent(
                    code = calculateMidnightCode(today.date.plusDays(1)),
                    fireAtEpochSeconds = midnight.toEpochSecond(),
                    prayer = null,
                    kind = ReminderKind.MIDNIGHT
                )
            )
        }

        // 6. If two events for the same prayer are < 60 s apart, keep only the later one
        val filtered = filterCloseEvents(events)

        return filtered.sortedBy { it.fireAtEpochSeconds }
    }

    private fun filterCloseEvents(events: List<ScheduledReminderEvent>): List<ScheduledReminderEvent> {
        val result = mutableListOf<ScheduledReminderEvent>()
        val prayerEvents = events.filter { it.prayer != null }.groupBy { it.prayer }
        val nonPrayerEvents = events.filter { it.prayer == null }

        for ((_, list) in prayerEvents) {
            val sorted = list.sortedBy { it.fireAtEpochSeconds }
            val prayerResult = mutableListOf<ScheduledReminderEvent>()
            for (ev in sorted) {
                val last = prayerResult.lastOrNull()
                if (last != null && abs(ev.fireAtEpochSeconds - last.fireAtEpochSeconds) < 60) {
                    // Keep the later one
                    prayerResult.removeAt(prayerResult.size - 1)
                    prayerResult.add(ev)
                } else {
                    prayerResult.add(ev)
                }
            }
            result.addAll(prayerResult)
        }

        result.addAll(nonPrayerEvents)
        return result
    }
}
