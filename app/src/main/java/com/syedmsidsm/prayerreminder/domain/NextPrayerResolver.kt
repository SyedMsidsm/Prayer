package com.syedmsidsm.prayerreminder.domain

import com.syedmsidsm.prayerreminder.domain.model.DaySchedule
import com.syedmsidsm.prayerreminder.domain.model.NextPrayer
import com.syedmsidsm.prayerreminder.domain.model.NextPrayerPhase
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.ScheduleSource
import java.time.Duration
import java.time.LocalTime

class NextPrayerResolver {

    /**
     * Resolves the current "Next" or "Now" prayer state.
     */
    fun resolve(
        nowTime: LocalTime,
        todaySchedule: DaySchedule,
        tomorrowSchedule: DaySchedule? = null
    ): NextPrayer? {
        if (todaySchedule.source == ScheduleSource.NONE || todaySchedule.azan.isEmpty()) {
            return null
        }

        val prayerSequence = listOf(
            Prayer.FAJR,
            Prayer.DHUHR,
            Prayer.ASR,
            Prayer.MAGHRIB,
            Prayer.ISHA
        )

        for (prayer in prayerSequence) {
            val azan = todaySchedule.azan[prayer] ?: continue
            val jamaat = todaySchedule.jamaat[prayer] ?: azan.plusMinutes(15)
            val isJumuah = prayer == Prayer.DHUHR && todaySchedule.isFriday

            if (nowTime.isBefore(azan)) {
                return NextPrayer(
                    prayer = prayer,
                    isJumuah = isJumuah,
                    phase = NextPrayerPhase.BEFORE_AZAN,
                    isTomorrow = false,
                    azanTime = azan,
                    jamaatTime = jamaat,
                    targetTime = azan,
                    remainingDuration = Duration.between(nowTime, azan)
                )
            } else if (nowTime.isBefore(jamaat)) {
                return NextPrayer(
                    prayer = prayer,
                    isJumuah = isJumuah,
                    phase = NextPrayerPhase.BETWEEN_AZAN_AND_JAMAAT,
                    isTomorrow = false,
                    azanTime = azan,
                    jamaatTime = jamaat,
                    targetTime = jamaat,
                    remainingDuration = Duration.between(nowTime, jamaat)
                )
            }
        }

        // Past today's Isha jamaat -> rollover to Tomorrow's Fajr
        val tomorrowFajrAzan = tomorrowSchedule?.azan?.get(Prayer.FAJR)
            ?: todaySchedule.azan[Prayer.FAJR] // fallback if tomorrow schedule is missing
        val tomorrowFajrJamaat = tomorrowSchedule?.jamaat?.get(Prayer.FAJR)
            ?: todaySchedule.jamaat[Prayer.FAJR]
            ?: tomorrowFajrAzan?.plusMinutes(15)

        if (tomorrowFajrAzan != null && tomorrowFajrJamaat != null) {
            val untilMidnight = Duration.between(nowTime, LocalTime.MAX).plusSeconds(1)
            val fromMidnightToFajr = Duration.between(LocalTime.MIN, tomorrowFajrAzan)
            val totalRemaining = untilMidnight.plus(fromMidnightToFajr)

            return NextPrayer(
                prayer = Prayer.FAJR,
                isJumuah = false,
                phase = NextPrayerPhase.BEFORE_AZAN,
                isTomorrow = true,
                azanTime = tomorrowFajrAzan,
                jamaatTime = tomorrowFajrJamaat,
                targetTime = tomorrowFajrAzan,
                remainingDuration = totalRemaining
            )
        }

        return null
    }
}
