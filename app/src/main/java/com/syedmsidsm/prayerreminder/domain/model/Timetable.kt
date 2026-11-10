package com.syedmsidsm.prayerreminder.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TimetableDay(
    val date: String,                 // ISO "2026-03-24"
    val weekday: String,              // "Tue" as printed
    val hijriDay: Int,                // 5
    val times: Map<String, String>    // "fajr" -> "05:18" ... 24-hour local time
)

@Serializable
data class Timetable(
    val schemaVersion: Int = 1,
    val kind: String = "prayerreminder.timetable",
    val place: String,                // "Bhatkal"
    val hijriMonth: Int,              // 1..12 (10 = Shawwal)
    val hijriMonthName: String,       // "Shawwal"
    val hijriYear: Int,               // 1447
    val firstDate: String,            // ISO "2026-03-20"
    val lastDate: String,             // ISO "2026-04-18"
    val timeFormat: String? = null,
    val timeZone: String = "Asia/Kolkata",
    val source: String,               // "image" | "family-file" | "manual"
    val verified: Boolean = false,
    val importedAt: Long = System.currentTimeMillis(),
    val days: List<TimetableDay>
)
