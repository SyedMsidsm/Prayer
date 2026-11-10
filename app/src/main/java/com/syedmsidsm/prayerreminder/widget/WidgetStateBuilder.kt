package com.syedmsidsm.prayerreminder.widget

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.syedmsidsm.prayerreminder.AppContainer
import com.syedmsidsm.prayerreminder.PrayerReminderApp
import com.syedmsidsm.prayerreminder.domain.HijriNames
import com.syedmsidsm.prayerreminder.domain.model.NextPrayerPhase
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.ScheduleSource
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class WidgetPrayerPhase {
    BEFORE_AZAN,
    BETWEEN_AZAN_AND_JAMAAT
}

data class WidgetPrayerRow(
    val prayer: Prayer,
    val prayerName: String,
    val azanFormatted: String,
    val jamaatFormatted: String,
    val azanShort: String = "",
    val isPast: Boolean,
    val isNext: Boolean
)

data class WidgetState(
    val hasTable: Boolean,
    val place: String,
    val isEstimated: Boolean,
    val hijriDate: String,
    val gregorianDate: String = "",
    val sunriseFormatted: String = "",
    val prayerName: String,
    val phase: WidgetPrayerPhase,
    val mainHeader: String,
    val mainTime: String,
    val secondaryTime: String,
    val countdownTargetEpochMillis: Long,
    val countdownText: String = "",
    val progressFraction: Float = 0.65f,
    val rows: List<WidgetPrayerRow>,
    val appLanguage: String,
    val voiceEnabled: Boolean = true,
    val nextPrayerKey: String = "FAJR"
) {
    val isRtl: Boolean get() = appLanguage == "ar" || appLanguage == "ur"
}

object WidgetStateBuilder {

    fun build(context: Context): WidgetState {
        val app = context.applicationContext as? PrayerReminderApp
        val container = app?.container ?: AppContainer(context.applicationContext)

        val locales = AppCompatDelegate.getApplicationLocales()
        val lang = if (!locales.isEmpty) locales.get(0)?.language ?: "en" else "en"

        var timetables = container.timetableRepository.timetables.value
        if (timetables.isEmpty()) {
            runBlocking {
                container.timetableRepository.initialize()
            }
            timetables = container.timetableRepository.timetables.value
        }

        val zoneId = ZoneId.of("Asia/Kolkata")
        val now = ZonedDateTime.now(zoneId)
        val today = now.toLocalDate()
        val nowTime = now.toLocalTime()

        val activeTimetable = container.timetableRepository.getTimetableFor(today)
            ?: timetables.firstOrNull()

        val rules = runBlocking {
            container.settingsRepository.jamaatRules.firstOrNull()
        } ?: emptyMap()
        val voiceEnabled = runBlocking {
            container.settingsRepository.voiceEnabled.firstOrNull()
        } ?: true

        val dayResolver = container.dayScheduleResolver
        val todaySchedule = dayResolver.resolve(today, timetables, rules)
        val tomorrowSchedule = dayResolver.resolve(today.plusDays(1), timetables, rules)
        val isEstimated = todaySchedule.source.isEstimated

        if (todaySchedule.source == ScheduleSource.NONE || todaySchedule.azan.isEmpty()) {
            return WidgetState(
                hasTable = false,
                place = getLocalizedPlace(activeTimetable?.place ?: "Bhatkal", lang),
                isEstimated = false,
                hijriDate = "",
                prayerName = "",
                phase = WidgetPrayerPhase.BEFORE_AZAN,
                mainHeader = "",
                mainTime = "",
                secondaryTime = "",
                countdownTargetEpochMillis = 0L,
                rows = emptyList(),
                appLanguage = lang
            )
        }

        val nextPrayerResolver = container.nextPrayerResolver
        val nextPrayer = nextPrayerResolver.resolve(nowTime, todaySchedule, tomorrowSchedule)
            ?: return WidgetState(
                hasTable = false,
                place = getLocalizedPlace(activeTimetable?.place ?: "Bhatkal", lang),
                isEstimated = false,
                hijriDate = "",
                prayerName = "",
                phase = WidgetPrayerPhase.BEFORE_AZAN,
                mainHeader = "",
                mainTime = "",
                secondaryTime = "",
                countdownTargetEpochMillis = 0L,
                rows = emptyList(),
                appLanguage = lang
            )

        val phase = if (nextPrayer.phase == NextPrayerPhase.BETWEEN_AZAN_AND_JAMAAT) {
            WidgetPrayerPhase.BETWEEN_AZAN_AND_JAMAAT
        } else {
            WidgetPrayerPhase.BEFORE_AZAN
        }

        val countdownTargetEpochMillis = if (nextPrayer.isTomorrow) {
            today.plusDays(1).atTime(nextPrayer.targetTime).atZone(zoneId).toInstant().toEpochMilli()
        } else {
            today.atTime(nextPrayer.targetTime).atZone(zoneId).toInstant().toEpochMilli()
        }

        val totalMinutesRemaining = nextPrayer.remainingDuration.toMinutes().coerceAtLeast(1)
        val remHours = totalMinutesRemaining / 60
        val remMins = totalMinutesRemaining % 60
        val countdownText = when (lang) {
            "ar" -> if (remHours > 0) "بعد ${remHours}س ${remMins}د" else "بعد ${remMins}د"
            "ur" -> if (remHours > 0) "${remHours} گھنٹے ${remMins} منٹ میں" else "${remMins} منٹ میں"
            else -> if (remHours > 0) "in ${remHours}h ${remMins}m" else "in ${remMins}m"
        }

        // Calculate progress fraction since previous prayer
        val progressFraction = computeProgressFraction(nowTime, todaySchedule.azan, nextPrayer.remainingDuration, nextPrayer.isTomorrow)

        val localizedPrayerName = getLocalizedPrayerName(nextPrayer.prayer, nextPrayer.isJumuah, lang)

        val amMarker = if (lang == "ar") "ص" else "AM"
        val pmMarker = if (lang == "ar") "م" else "PM"
        val azanFormatted = TimeFormatter.format12Hour(nextPrayer.azanTime, amMarker, pmMarker)
        val jamaatFormatted = TimeFormatter.format12Hour(nextPrayer.jamaatTime, amMarker, pmMarker)

        val mainHeader: String
        val mainTime: String
        val secondaryTime: String

        if (phase == WidgetPrayerPhase.BEFORE_AZAN) {
            mainHeader = when (lang) {
                "ar" -> "التالية · $localizedPrayerName"
                "ur" -> "اگلی · $localizedPrayerName"
                else -> "NEXT · ${localizedPrayerName.uppercase(Locale.ROOT)}"
            }
            mainTime = azanFormatted
            secondaryTime = when (lang) {
                "ar" -> "الإقامة $jamaatFormatted"
                "ur" -> "جماعت $jamaatFormatted"
                else -> "Jamaat $jamaatFormatted"
            }
        } else {
            mainHeader = when (lang) {
                "ar" -> "الإقامة · $localizedPrayerName"
                "ur" -> "جماعت · $localizedPrayerName"
                else -> "JAMAAT · ${localizedPrayerName.uppercase(Locale.ROOT)}"
            }
            mainTime = jamaatFormatted
            secondaryTime = when (lang) {
                "ar" -> "الأذان ✓ $azanFormatted"
                "ur" -> "اذان ✓ $azanFormatted"
                else -> "Azan ✓ $azanFormatted"
            }
        }

        val prayers = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
        val scheduleForRows = if (nextPrayer.isTomorrow) tomorrowSchedule else todaySchedule
        val rows = prayers.map { p ->
            val isFri = (p == Prayer.DHUHR && scheduleForRows.isFriday)
            val pName = getLocalizedPrayerName(p, isFri, lang)
            val azan = scheduleForRows.azan[p] ?: todaySchedule.azan[p] ?: LocalTime.of(0, 0)
            val jamaat = scheduleForRows.jamaat[p] ?: todaySchedule.jamaat[p] ?: azan.plusMinutes(15)
            val aStr = TimeFormatter.format12Hour(azan, amMarker, pmMarker)
            val jStr = TimeFormatter.format12Hour(jamaat, amMarker, pmMarker)
            val aShort = aStr.replace(" AM", "").replace(" PM", "").replace(" ص", "").replace(" م", "")

            val isNext = (nextPrayer.prayer == p)
            val isPast = if (nextPrayer.isTomorrow) {
                false
            } else {
                nowTime.isAfter(jamaat)
            }

            WidgetPrayerRow(
                prayer = p,
                prayerName = pName,
                azanFormatted = aStr,
                jamaatFormatted = jStr,
                azanShort = aShort,
                isPast = isPast,
                isNext = isNext
            )
        }

        val sunriseTime = scheduleForRows.azan[Prayer.SUNRISE] ?: todaySchedule.azan[Prayer.SUNRISE]
        val sunriseFormatted = if (sunriseTime != null) {
            val sTime = TimeFormatter.format12Hour(sunriseTime, amMarker, pmMarker)
            val sLabel = getLocalizedPrayerName(Prayer.SUNRISE, false, lang)
            "$sLabel $sTime"
        } else ""

        val rawPlace = getLocalizedPlace(activeTimetable?.place ?: "Bhatkal", lang)
        val place = if (isEstimated) {
            when (lang) {
                "ar" -> "$rawPlace تقريبي"
                "ur" -> "$rawPlace تخمینہ"
                else -> "$rawPlace est."
            }
        } else {
            rawPlace
        }

        val hijriObj = todaySchedule.hijri ?: dayResolver.estimateHijriDate(today, timetables)
        val monthName = HijriNames.getMonthName(hijriObj.month, lang)
        val hijriDate = "${hijriObj.day} $monthName ${hijriObj.year}".trim()

        val locale = Locale.forLanguageTag(lang)
        val gregorianDate = today.format(DateTimeFormatter.ofPattern("EEE d MMM", locale))

        return WidgetState(
            hasTable = true,
            place = place,
            isEstimated = isEstimated,
            hijriDate = hijriDate,
            gregorianDate = gregorianDate,
            sunriseFormatted = sunriseFormatted,
            prayerName = localizedPrayerName,
            phase = phase,
            mainHeader = mainHeader,
            mainTime = mainTime,
            secondaryTime = secondaryTime,
            countdownTargetEpochMillis = countdownTargetEpochMillis,
            countdownText = countdownText,
            progressFraction = progressFraction,
            rows = rows,
            appLanguage = lang,
            voiceEnabled = voiceEnabled,
            nextPrayerKey = nextPrayer.prayer.name
        )
    }

    private fun computeProgressFraction(
        nowTime: LocalTime,
        azanMap: Map<Prayer, LocalTime>,
        remaining: Duration,
        isTomorrow: Boolean
    ): Float {
        val remMin = remaining.toMinutes().toFloat().coerceAtLeast(1f)
        val totalWindowMin = if (isTomorrow) {
            val isha = azanMap[Prayer.ISHA] ?: LocalTime.of(19, 15)
            val fajr = azanMap[Prayer.FAJR] ?: LocalTime.of(5, 15)
            val nightSpan = Duration.between(isha, LocalTime.MAX).toMinutes() + Duration.between(LocalTime.MIN, fajr).toMinutes()
            nightSpan.toFloat().coerceAtLeast(180f)
        } else {
            210f
        }
        return (1f - (remMin / totalWindowMin)).coerceIn(0.12f, 0.92f)
    }

    fun getLocalizedPrayerName(prayer: Prayer, isJumuah: Boolean, lang: String): String {
        return when (lang) {
            "ar" -> if (isJumuah) "الجمعة" else when (prayer) {
                Prayer.FAJR -> "الفجر"
                Prayer.SUNRISE -> "الشروق"
                Prayer.DHUHR -> "الظهر"
                Prayer.ASR -> "العصر"
                Prayer.MAGHRIB -> "المغرب"
                Prayer.ISHA -> "العشاء"
            }
            "ur" -> if (isJumuah) "جمعہ" else when (prayer) {
                Prayer.FAJR -> "فجر"
                Prayer.SUNRISE -> "طلوع"
                Prayer.DHUHR -> "ظہر"
                Prayer.ASR -> "عصر"
                Prayer.MAGHRIB -> "مغرب"
                Prayer.ISHA -> "عشاء"
            }
            else -> if (isJumuah) "Jumu'ah" else when (prayer) {
                Prayer.FAJR -> "Fajr"
                Prayer.SUNRISE -> "Sunrise"
                Prayer.DHUHR -> "Dhuhr"
                Prayer.ASR -> "Asr"
                Prayer.MAGHRIB -> "Maghrib"
                Prayer.ISHA -> "Isha"
            }
        }
    }

    fun getLocalizedPlace(place: String, lang: String): String {
        return if (place.equals("Bhatkal", ignoreCase = true)) {
            when (lang) {
                "ar" -> "بهتكل"
                "ur" -> "بھٹکل"
                else -> "Bhatkal"
            }
        } else {
            place
        }
    }

    fun getLocalizedMonthName(name: String, lang: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("shawwal") -> if (lang == "ar") "شوال" else if (lang == "ur") "شوال" else "Shawwal"
            lower.contains("ramadan") -> if (lang == "ar") "رمضان" else if (lang == "ur") "رمضان" else "Ramadan"
            lower.contains("muharram") -> if (lang == "ar") "محرم" else if (lang == "ur") "محرم" else "Muharram"
            lower.contains("safar") -> if (lang == "ar") "صفر" else if (lang == "ur") "صفر" else "Safar"
            lower.contains("rabi") && (lower.contains("1") || lower.contains("awwal")) -> if (lang == "ar") "ربيع الأول" else if (lang == "ur") "ربیع الاول" else "Rabi' al-Awwal"
            lower.contains("rabi") -> if (lang == "ar") "ربيع الآخر" else if (lang == "ur") "ربیع الثانی" else "Rabi' al-Thani"
            lower.contains("jumada") && (lower.contains("1") || lower.contains("awwal") || lower.contains("ula")) -> if (lang == "ar") "جمادى الأولى" else if (lang == "ur") "جمادی الاولیٰ" else "Jumada al-Ula"
            lower.contains("jumada") -> if (lang == "ar") "جمادى الآخرة" else if (lang == "ur") "جمادی الثانی" else "Jumada al-Akhirah"
            lower.contains("rajab") -> if (lang == "ar") "رجب" else if (lang == "ur") "رجب" else "Rajab"
            lower.contains("sha") && lower.contains("ban") -> if (lang == "ar") "شعبان" else if (lang == "ur") "شعبان" else "Sha'ban"
            lower.contains("dhu") && lower.contains("qa") -> if (lang == "ar") "ذو القعدة" else if (lang == "ur") "ذوالقعدہ" else "Dhu al-Qi'dah"
            lower.contains("dhu") && lower.contains("hij") -> if (lang == "ar") "ذو الحجة" else if (lang == "ur") "ذوالحجہ" else "Dhu al-Hijjah"
            else -> name
        }
    }
}
