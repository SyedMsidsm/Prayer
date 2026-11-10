package com.syedmsidsm.prayerreminder.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class DaySchedule(
    val date: LocalDate,
    val hijri: HijriDate?,
    val azan: Map<Prayer, LocalTime>,
    val jamaat: Map<Prayer, LocalTime>,
    val isFriday: Boolean,
    val source: ScheduleSource,
    val fixedTimeBeforeAzanWarnings: Set<Prayer> = emptySet()
)
