package com.syedmsidsm.prayerreminder.domain

import com.syedmsidsm.prayerreminder.domain.model.DaySchedule
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.ScheduleSource
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

data class CityPreset(
    val id: String,
    val name: String,
    val country: String,
    val displayName: String,
    val lat: Double,
    val lng: Double,
    val zoneId: String
)

object CityPresets {
    val ALL: List<CityPreset> = listOf(
        CityPreset("bhatkal", "Bhatkal", "IN", "Bhatkal, IN", 13.9853, 74.5553, "Asia/Kolkata"),
        CityPreset("mangaluru", "Mangaluru", "IN", "Mangaluru, IN", 12.9141, 74.8560, "Asia/Kolkata"),
        CityPreset("bengaluru", "Bengaluru", "IN", "Bengaluru, IN", 12.9716, 77.5946, "Asia/Kolkata"),
        CityPreset("makkah", "Makkah", "SA", "Makkah, SA", 21.4225, 39.8262, "Asia/Riyadh"),
        CityPreset("madinah", "Madinah", "SA", "Madinah, SA", 24.4672, 39.6112, "Asia/Riyadh"),
        CityPreset("london", "London", "UK", "London, UK", 51.5074, -0.1278, "Europe/London")
    )

    fun findById(id: String): CityPreset = ALL.firstOrNull { it.id == id } ?: ALL.first()

    /**
     * When the active city is Bhatkal with default Standard/MWL (or using a printed Bhatkal table
     * without custom Hanafi/method overrides), preserves the exact Bhatkal masjid schedule.
     * Otherwise computes offline astronomical times for the chosen city, calculation method, and Asr juristic.
     */
    fun adjustScheduleForCityAndMethod(
        baseSchedule: DaySchedule,
        date: LocalDate,
        cityId: String,
        calcMethod: String,
        asrMethod: String,
        jamaatRules: Map<String, JamaatRule>
    ): DaySchedule {
        if (cityId == "bhatkal" && calcMethod == "MWL" && asrMethod == "Standard") {
            return baseSchedule
        }

        val azanTimes: Map<Prayer, LocalTime> = if (cityId == "bhatkal" && baseSchedule.source == ScheduleSource.TABLE) {
            // Apply method/juristic adjustments cleanly on top of the imported Bhatkal sheet
            val computedStdMwl = computeAstronomical(date, findById("bhatkal"), "MWL", "Standard")
            val computedCustom = computeAstronomical(date, findById("bhatkal"), calcMethod, asrMethod)
            baseSchedule.azan.mapValues { (prayer, time) ->
                val baseRef = computedStdMwl[prayer]
                val custRef = computedCustom[prayer]
                if (baseRef != null && custRef != null) {
                    val deltaMin = (custRef.toSecondOfDay() - baseRef.toSecondOfDay()) / 60L
                    time.plusMinutes(deltaMin)
                } else {
                    time
                }
            }
        } else {
            val preset = findById(cityId)
            val raw = computeAstronomical(date, preset, calcMethod, asrMethod)
            if (cityId == "bhatkal") {
                raw.mapValues { (prayer, time) ->
                    val offset = PrayerTimeCalculator.Calibration.BHATKAL.offsets[prayer] ?: 0
                    time.plusMinutes(offset.toLong())
                }
            } else {
                raw
            }
        }

        val jamaatTimes = mutableMapOf<Prayer, LocalTime>()
        val warnings = mutableSetOf<Prayer>()
        for (prayer in Prayer.entries) {
            if (!prayer.hasJamaat) continue
            val azan = azanTimes[prayer] ?: continue
            val ruleKey = if (prayer == Prayer.DHUHR && baseSchedule.isFriday) "JUMUAH" else prayer.name
            val rule = jamaatRules[ruleKey] ?: JamaatRule.AfterAzan(15)
            when (rule) {
                is JamaatRule.AfterAzan -> {
                    val mins = rule.minutes.coerceIn(0, 60)
                    jamaatTimes[prayer] = azan.plusMinutes(mins.toLong())
                }
                is JamaatRule.Fixed -> {
                    if (!rule.time.isAfter(azan)) {
                        warnings.add(prayer)
                        jamaatTimes[prayer] = azan.plusMinutes(15)
                    } else {
                        jamaatTimes[prayer] = rule.time
                    }
                }
            }
        }

        return baseSchedule.copy(
            azan = azanTimes,
            jamaat = jamaatTimes,
            fixedTimeBeforeAzanWarnings = warnings
        )
    }

    fun computeAstronomical(
        date: LocalDate,
        city: CityPreset,
        calcMethod: String,
        asrMethod: String
    ): Map<Prayer, LocalTime> {
        val zone = runCatching { ZoneId.of(city.zoneId) }.getOrDefault(ZoneId.of("Asia/Kolkata"))
        val tz = zone.rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0

        var y = date.year
        var m = date.monthValue
        if (m <= 2) { y -= 1; m += 12 }
        val a = y / 100
        val b = 2 - a + a / 4
        val jd = (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + date.dayOfMonth + b - 1524.5 - city.lng / (15.0 * 24.0)

        val d = jd + 0.5 - 2451545.0
        val g = Math.toRadians((357.529 + 0.98560028 * d).mod(360.0))
        val q = (280.459 + 0.98564736 * d).mod(360.0)
        val l = Math.toRadians((q + 1.915 * sin(g) + 0.020 * sin(2 * g)).mod(360.0))
        val e = Math.toRadians(23.439 - 0.00000036 * d)
        val ra = Math.toDegrees(atan2(cos(e) * sin(l), cos(l))) / 15.0
        val decl = asin(sin(e) * sin(l))
        var eqt = q / 15.0 - ra.mod(24.0)
        eqt = (eqt + 12.0).mod(24.0) - 12.0

        val noon = 12.0 + tz - city.lng / 15.0 - eqt
        val phi = Math.toRadians(city.lat)

        fun hourAngle(altitudeDeg: Double): Double {
            val c = (sin(Math.toRadians(altitudeDeg)) - sin(phi) * sin(decl)) / (cos(phi) * cos(decl))
            return Math.toDegrees(acos(c.coerceIn(-1.0, 1.0))) / 15.0
        }

        val fajrAngle = when (calcMethod) {
            "ISNA" -> 15.0
            "UmmAlQura" -> 18.5
            "Karachi" -> 18.0
            else -> 18.0 // MWL
        }
        val ishaAngle = when (calcMethod) {
            "ISNA" -> 15.0
            "Karachi" -> 18.0
            else -> 17.0 // MWL
        }
        val shadowFactor = if (asrMethod == "Hanafi") 2.0 else 1.0
        val asrAltitude = Math.toDegrees(atan(1.0 / (shadowFactor + tan(abs(phi - decl)))))

        val maghribHours = noon + hourAngle(-0.833)
        val ishaHours = if (calcMethod == "UmmAlQura") maghribHours + 1.5 else noon + hourAngle(-ishaAngle)

        val hoursMap = mapOf(
            Prayer.FAJR to (noon - hourAngle(-fajrAngle)),
            Prayer.SUNRISE to (noon - hourAngle(-0.833)),
            Prayer.DHUHR to noon,
            Prayer.ASR to (noon + hourAngle(asrAltitude)),
            Prayer.MAGHRIB to maghribHours,
            Prayer.ISHA to ishaHours
        )

        return hoursMap.mapValues { (_, hours) ->
            val minutes = (hours * 60.0).roundToInt()
            LocalTime.of(0, 0).plusMinutes(minutes.toLong().mod(24L * 60L))
        }
    }
}
