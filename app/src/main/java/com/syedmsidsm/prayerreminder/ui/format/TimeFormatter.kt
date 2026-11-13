package com.syedmsidsm.prayerreminder.ui.format

import java.time.Duration
import java.time.LocalTime
import java.util.Locale

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
     * Formats LocalTime into either 24h ("HH:mm") or 12h ("h:mm AM/PM").
     */
    fun formatTime(time: LocalTime, use24Hour: Boolean, amMarker: String, pmMarker: String): String {
        if (use24Hour) {
            return "%02d:%02d".format(Locale.US, time.hour, time.minute)
        }
        return format12Hour(time, amMarker, pmMarker)
    }

    /**
     * Returns Pair(mainTimeDigits, amPmSuffix). For 24h mode, suffix is "".
     */
    fun formatTimeParts(
        time: LocalTime,
        use24Hour: Boolean,
        amMarker: String,
        pmMarker: String
    ): Pair<String, String> {
        if (use24Hour) {
            return "%02d:%02d".format(Locale.US, time.hour, time.minute) to ""
        }
        val hour24 = time.hour
        val isPm = hour24 >= 12
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val digits = "$hour12:%02d".format(Locale.US, time.minute)
        return digits to (if (isPm) pmMarker else amMarker)
    }

    /**
     * Formats duration as "-HH:MM:SS".
     */
    fun formatCountdownHms(duration: Duration): String {
        val totalSeconds = duration.seconds.coerceAtLeast(0L)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "-%02d:%02d:%02d".format(Locale.US, hours, minutes, seconds)
    }

    /**
     * Formats duration briefly as "2h 15m" or "18m".
     */
    fun formatShortCountdown(duration: Duration): String {
        val totalMinutes = (duration.seconds.coerceAtLeast(0L) + 59) / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
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
