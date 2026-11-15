package com.syedmsidsm.prayerreminder.widgets

import java.time.LocalTime

enum class SkyPhase { DAWN, DAY, DUSK, NIGHT }

/** Same idea as the app's main page: the sky follows the real prayer/sun times. */
object Sky {
    private fun dawnEnd(s: DaySchedule) = s.slot(Prayer.SUNRISE).adhan.plusMinutes(30)
    private fun duskStart(s: DaySchedule) = s.slot(Prayer.MAGHRIB).adhan.minusMinutes(45)

    fun phaseAt(s: DaySchedule, t: LocalTime): SkyPhase {
        val fajr = s.slot(Prayer.FAJR).adhan
        val isha = s.slot(Prayer.ISHA).adhan
        return when {
            t >= fajr && t < dawnEnd(s) -> SkyPhase.DAWN
            t >= dawnEnd(s) && t < duskStart(s) -> SkyPhase.DAY
            t >= duskStart(s) && t < isha -> SkyPhase.DUSK
            else -> SkyPhase.NIGHT
        }
    }

    /** Moments when the sky changes (used to schedule widget refreshes). */
    fun boundaries(s: DaySchedule): List<LocalTime> =
        listOf(s.slot(Prayer.FAJR).adhan, dawnEnd(s), duskStart(s), s.slot(Prayer.ISHA).adhan)
}
