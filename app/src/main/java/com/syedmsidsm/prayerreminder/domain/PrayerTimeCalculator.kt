package com.syedmsidsm.prayerreminder.domain

import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

/**
 * Fully offline astronomical prayer-time calculator.
 *
 * Used as the always-available fallback when no imported timetable covers a date, so the
 * app NEVER shows stale times or stops reminders when a monthly sheet expires.
 *
 * Accuracy: verified against the Bhatkal masjid sheets (Mar–Apr and Nov–Dec 2026). After applying
 * the per-prayer [Calibration] offsets the result matches the printed sheet within ±1 minute.
 */
class PrayerTimeCalculator(
    val latitude: Double = BHATKAL_LAT,
    val longitude: Double = BHATKAL_LON,
    val zoneId: ZoneId = ZoneId.of("Asia/Kolkata"),
    private val fajrAngle: Double = 18.0,
    private val ishaAngle: Double = 18.0
) {

    /** Minutes added to each raw astronomical time to match the local masjid committee sheet. */
    data class Calibration(val offsets: Map<Prayer, Int>) {
        companion object {
            /** Derived from the Bhatkal 2026 sheets (Shawwal 1447, Jumada al-Akhirah 1448). */
            val BHATKAL = Calibration(
                mapOf(
                    Prayer.FAJR to -5,
                    Prayer.SUNRISE to -1,
                    Prayer.DHUHR to 6,
                    Prayer.ASR to 2,
                    Prayer.MAGHRIB to 3,
                    Prayer.ISHA to -4
                )
            )
            val NONE = Calibration(emptyMap())
        }
    }

    /** Raw astronomical times (no calibration), as fractional local hours. */
    fun rawHours(date: LocalDate): Map<Prayer, Double> {
        val tz = zoneId.rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0
        val jd = julianDay(date) - longitude / (15.0 * 24.0)
        val (decl, eqt) = sunPosition(jd + 0.5)
        val noon = 12.0 + tz - longitude / 15.0 - eqt
        val phi = Math.toRadians(latitude)

        fun hourAngle(altitudeDeg: Double): Double {
            val c = (sin(Math.toRadians(altitudeDeg)) - sin(phi) * sin(decl)) / (cos(phi) * cos(decl))
            return Math.toDegrees(acos(c.coerceIn(-1.0, 1.0))) / 15.0
        }

        // Shafi'i Asr: shadow length = object length + noon shadow
        val asrAltitude = Math.toDegrees(atan(1.0 / (1.0 + tan(abs(phi - decl)))))

        return mapOf(
            Prayer.FAJR to noon - hourAngle(-fajrAngle),
            Prayer.SUNRISE to noon - hourAngle(-0.833),
            Prayer.DHUHR to noon,
            Prayer.ASR to noon + hourAngle(asrAltitude),
            Prayer.MAGHRIB to noon + hourAngle(-0.833),
            Prayer.ISHA to noon + hourAngle(-ishaAngle)
        )
    }

    /** Calibrated local times for [date]. */
    fun compute(date: LocalDate, calibration: Calibration = Calibration.BHATKAL): Map<Prayer, LocalTime> {
        return rawHours(date).mapValues { (prayer, hours) ->
            val minutes = (hours * 60.0).roundToInt() + (calibration.offsets[prayer] ?: 0)
            LocalTime.of(0, 0).plusMinutes(minutes.toLong().mod(24L * 60L))
        }
    }

    /**
     * Learns per-prayer offsets from imported timetables (median of table − raw calc), so the
     * fallback keeps matching the user's own masjid even if it differs from Bhatkal's committee.
     * Falls back to [Calibration.BHATKAL] when there is not enough data.
     */
    fun calibrateFrom(timetables: List<Timetable>): Calibration {
        val diffs = mutableMapOf<Prayer, MutableList<Int>>()
        for (table in timetables) {
            for (day in table.days) {
                val date = runCatching { LocalDate.parse(day.date) }.getOrNull() ?: continue
                val raw = rawHours(date)
                for (prayer in Prayer.entries) {
                    val printed = day.times[prayer.name.lowercase()]
                        ?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: continue
                    val calc = (raw.getValue(prayer) * 60.0).roundToInt()
                    val diff = printed.toSecondOfDay() / 60 - calc
                    if (abs(diff) <= 30) diffs.getOrPut(prayer) { mutableListOf() }.add(diff)
                }
            }
        }
        if (diffs.values.sumOf { it.size } < 30) return Calibration.BHATKAL
        val offsets = Calibration.BHATKAL.offsets.toMutableMap()
        for ((prayer, list) in diffs) {
            if (list.size >= 5) offsets[prayer] = list.sorted()[list.size / 2]
        }
        return Calibration(offsets)
    }

    private fun julianDay(date: LocalDate): Double {
        var y = date.year
        var m = date.monthValue
        if (m <= 2) { y -= 1; m += 12 }
        val a = y / 100
        val b = 2 - a + a / 4
        return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + date.dayOfMonth + b - 1524.5
    }

    /** Returns (declination in radians, equation of time in hours). */
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = Math.toRadians((357.529 + 0.98560028 * d).mod(360.0))
        val q = (280.459 + 0.98564736 * d).mod(360.0)
        val l = Math.toRadians((q + 1.915 * sin(g) + 0.020 * sin(2 * g)).mod(360.0))
        val e = Math.toRadians(23.439 - 0.00000036 * d)
        val ra = Math.toDegrees(atan2(cos(e) * sin(l), cos(l))) / 15.0
        val decl = asin(sin(e) * sin(l))
        var eqt = q / 15.0 - ra.mod(24.0)
        eqt = (eqt + 12.0).mod(24.0) - 12.0
        return decl to eqt
    }

    companion object {
        const val BHATKAL_LAT = 13.9853
        const val BHATKAL_LON = 74.5553
    }
}
