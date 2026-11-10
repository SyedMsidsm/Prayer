package com.syedmsidsm.prayerreminder.ui.format

import java.time.Duration
import java.time.LocalTime

object TimeFormatter {

    /**
     * Formats 24-hour LocalTime into 12-hour format with localized AM/PM marker.
     * Always uses Western digits (0-9) as specified in Spec Section 14.1.
     */
    fun format12Hour(time: LocalTime, amMarker: String, pmMarker: String): String {
        val hour24 = time.hour
        val minute = time.minute
        val isPm = hour24 >= 12
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val minuteStr = minute.toString().padStart(2, '0')
        val marker = if (isPm) pmMarker else amMarker
        return "$hour12:$minuteStr $marker"
    }

    /**
     * Formats remaining duration into localized countdown text:
     * e.g. "1 hour 24 minutes" or "15 minutes"
     */
    fun formatDuration(
        duration: Duration,
        formatHours: (Long) -> String,
        formatMinutes: (Long) -> String,
        durationJoin: (String, String) -> String
    ): String {
        val totalMinutes = duration.toMinutes().coerceAtLeast(0)
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60

        return when {
            hours > 0 && mins > 0 -> {
                durationJoin(formatHours(hours), formatMinutes(mins))
            }
            hours > 0 -> formatHours(hours)
            else -> formatMinutes(mins.coerceAtLeast(1))
        }
    }
}
