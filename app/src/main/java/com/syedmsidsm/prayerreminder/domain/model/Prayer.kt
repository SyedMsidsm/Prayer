package com.syedmsidsm.prayerreminder.domain.model

enum class Prayer {
    FAJR,
    SUNRISE, // Shown only, no jamaat, no reminders
    DHUHR,
    ASR,
    MAGHRIB,
    ISHA;

    val hasJamaat: Boolean
        get() = this != SUNRISE

    val hasReminders: Boolean
        get() = this != SUNRISE
}
