package com.syedmsidsm.prayerreminder.domain.model

enum class ScheduleSource {
    TABLE,
    ESTIMATED_CARRY_FORWARD,
    ESTIMATED_LAST_YEAR,
    NONE;

    val isEstimated: Boolean
        get() = this == ESTIMATED_CARRY_FORWARD || this == ESTIMATED_LAST_YEAR
}
