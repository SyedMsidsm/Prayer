package com.syedmsidsm.prayerreminder.widgets

import android.content.Context
import java.time.LocalDateTime

data class NextInfo(
    val slot: Slot,
    val at: LocalDateTime,
    val previousAt: LocalDateTime,
    val today: DaySchedule
)

object NextPrayer {
    fun compute(context: Context, now: LocalDateTime): NextInfo {
        val today = PrayerDataSource.scheduleFor(context, now.toLocalDate())
        val five = today.fivePrayers
        val upcoming = five.firstOrNull { LocalDateTime.of(today.date, it.adhan).isAfter(now) }
        if (upcoming != null) {
            val i = five.indexOf(upcoming)
            val prev = if (i > 0) {
                LocalDateTime.of(today.date, five[i - 1].adhan)
            } else {
                val y = PrayerDataSource.scheduleFor(context, today.date.minusDays(1))
                LocalDateTime.of(y.date, y.slot(Prayer.ISHA).adhan)
            }
            return NextInfo(upcoming, LocalDateTime.of(today.date, upcoming.adhan), prev, today)
        }
        val tomorrow = PrayerDataSource.scheduleFor(context, today.date.plusDays(1))
        val fajr = tomorrow.slot(Prayer.FAJR)
        return NextInfo(
            fajr,
            LocalDateTime.of(tomorrow.date, fajr.adhan),
            LocalDateTime.of(today.date, five.last().adhan),
            today
        )
    }
}
