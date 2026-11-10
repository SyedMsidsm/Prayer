package com.syedmsidsm.prayerreminder.domain.model

import java.time.Duration
import java.time.LocalTime

enum class NextPrayerPhase {
    BEFORE_AZAN,
    BETWEEN_AZAN_AND_JAMAAT
}

data class NextPrayer(
    val prayer: Prayer,
    val isJumuah: Boolean,
    val phase: NextPrayerPhase,
    val isTomorrow: Boolean,
    val azanTime: LocalTime,
    val jamaatTime: LocalTime,
    val targetTime: LocalTime,
    val remainingDuration: Duration
)
