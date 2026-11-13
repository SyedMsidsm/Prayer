package com.syedmsidsm.prayerreminder.domain.model

enum class Prayer {
    FAJR,
    SUNRISE,
    DHUHR,
    ASR,
    MAGHRIB,
    ISHA;

    val hasJamaat: Boolean
        get() = this != SUNRISE

    val hasReminders: Boolean
        get() = true
}
