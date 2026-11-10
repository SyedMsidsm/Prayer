package com.syedmsidsm.prayerreminder.widget

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.syedmsidsm.prayerreminder.AppContainer
import com.syedmsidsm.prayerreminder.PrayerReminderApp
import com.syedmsidsm.prayerreminder.domain.DayScheduleResolver
import com.syedmsidsm.prayerreminder.domain.NextPrayerResolver
import com.syedmsidsm.prayerreminder.domain.model.NextPrayerPhase
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.ScheduleSource
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

enum class WidgetPrayerPhase {
    BEFORE_AZAN,
    BETWEEN_AZAN_AND_JAMAAT
}

data class WidgetPrayerRow(
    val prayer: Prayer,
    val prayerName: String,
    val azanFormatted: String,
    val jamaatFormatted: String,
    val isPast: Boolean,
    val isNext: Boolean
)

data class WidgetState(
    val hasTable: Boolean,
    val place: String,
    val isEstimated: Boolean,
    val hijriDate: String,
    val prayerName: String,
    val phase: WidgetPrayerPhase,
    val mainHeader: String,
    val mainTime: String,
    val secondaryTime: String,
    val countdownTargetEpochMillis: Long,
    val rows: List<WidgetPrayerRow>,
    val appLanguage: String
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
        val activeTimetable = timetables.firstOrNull()

        if (activeTimetable == null) {
            return WidgetState(
                hasTable = false,
                place = getLocalizedPlace("Bhatkal", lang),
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

        val zoneId = ZoneId.of("Asia/Kolkata")
        val now = ZonedDateTime.now(zoneId)
        val today = now.toLocalDate()
        val nowTime = now.toLocalTime()

        val rules = runBlocking {
            container.settingsRepository.jamaatRules.firstOrNull()
        } ?: emptyMap()

        val dayResolver = container.dayScheduleResolver
        var todaySchedule = dayResolver.resolve(today, timetables, rules)
        var isEstimated = todaySchedule.source.isEstimated

        // If today is outside the active timetable, fall back gracefully to the timetable's days
        // so the widget is NEVER blank or empty!
        var effectiveDate = today
        if (todaySchedule.source == ScheduleSource.NONE || todaySchedule.azan.isEmpty()) {
            val days = activeTimetable.days
            if (days.isNotEmpty()) {
                val dayIdx = (today.dayOfMonth - 1).coerceIn(0, days.size - 1)
                val targetDay = days[dayIdx]
                val targetDate = runCatching { LocalDate.parse(targetDay.date) }.getOrNull() ?: today
                effectiveDate = targetDate
                todaySchedule = dayResolver.resolve(targetDate, listOf(activeTimetable), rules)
                isEstimated = true
            }
        }

        if (todaySchedule.source == ScheduleSource.NONE || todaySchedule.azan.isEmpty()) {
            return WidgetState(
                hasTable = false,
                place = getLocalizedPlace(activeTimetable.place, lang),
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

        val tomorrowSchedule = if (effectiveDate != today) {
            val days = activeTimetable.days
            if (days.isNotEmpty()) {
                val nextDayIdx = today.dayOfMonth.coerceIn(0, days.size - 1)
                val nextDay = days[nextDayIdx]
                val nextDate = runCatching { LocalDate.parse(nextDay.date) }.getOrNull() ?: effectiveDate.plusDays(1)
                dayResolver.resolve(nextDate, listOf(activeTimetable), rules)
            } else {
                dayResolver.resolve(effectiveDate.plusDays(1), timetables, rules)
            }
        } else {
            dayResolver.resolve(today.plusDays(1), timetables, rules)
        }

        val nextPrayerResolver = container.nextPrayerResolver
        val nextPrayer = nextPrayerResolver.resolve(nowTime, todaySchedule, tomorrowSchedule)

        if (nextPrayer == null) {
            return WidgetState(
                hasTable = false,
                place = getLocalizedPlace(activeTimetable.place, lang),
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
                else -> "Next · $localizedPrayerName"
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
                else -> "Jamaat · $localizedPrayerName"
            }
            mainTime = jamaatFormatted
            secondaryTime = when (lang) {
                "ar" -> "الأذان ✓ $azanFormatted"
                "ur" -> "اذان ✓ $azanFormatted"
                else -> "Azan ✓ $azanFormatted"
            }
        }

        val prayers = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
        val rows = prayers.map { p ->
            val isFri = (p == Prayer.DHUHR && todaySchedule.isFriday)
            val pName = getLocalizedPrayerName(p, isFri, lang)
            val azan = todaySchedule.azan[p] ?: LocalTime.of(0, 0)
            val jamaat = todaySchedule.jamaat[p] ?: azan.plusMinutes(15)
            val aStr = TimeFormatter.format12Hour(azan, amMarker, pmMarker)
            val jStr = TimeFormatter.format12Hour(jamaat, amMarker, pmMarker)

            val isNext = (!nextPrayer.isTomorrow && nextPrayer.prayer == p)
            val isPast = if (nextPrayer.isTomorrow) {
                true
            } else {
                nowTime.isAfter(jamaat)
            }

            WidgetPrayerRow(
                prayer = p,
                prayerName = pName,
                azanFormatted = aStr,
                jamaatFormatted = jStr,
                isPast = isPast,
                isNext = isNext
            )
        }

        val rawPlace = getLocalizedPlace(activeTimetable.place, lang)
        val place = if (isEstimated) {
            when (lang) {
                "ar" -> "$rawPlace تقريبي"
                "ur" -> "$rawPlace تخمینہ"
                else -> "$rawPlace est."
            }
        } else {
            rawPlace
        }

        val hijriDay = todaySchedule.hijri?.day?.toString() ?: ""
        val monthName = getLocalizedMonthName(activeTimetable.hijriMonthName, lang)
        val hijriDate = "$hijriDay $monthName ${activeTimetable.hijriYear}".trim()

        return WidgetState(
            hasTable = true,
            place = place,
            isEstimated = isEstimated,
            hijriDate = hijriDate,
            prayerName = localizedPrayerName,
            phase = phase,
            mainHeader = mainHeader,
            mainTime = mainTime,
            secondaryTime = secondaryTime,
            countdownTargetEpochMillis = countdownTargetEpochMillis,
            rows = rows,
            appLanguage = lang
        )
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
                Prayer.SUNRISE -> "طلوع آفتاب"
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
            lower.contains("jumada") && (lower.contains("1") || lower.contains("awwal")) -> if (lang == "ar") "جمادى الأولى" else if (lang == "ur") "جمادی الاولیٰ" else "Jumada al-Awwal"
            lower.contains("jumada") -> if (lang == "ar") "جمادى الآخرة" else if (lang == "ur") "جمادی الثانی" else "Jumada al-Thani"
            lower.contains("rajab") -> if (lang == "ar") "رجب" else if (lang == "ur") "رجب" else "Rajab"
            lower.contains("sha") && lower.contains("ban") -> if (lang == "ar") "شعبان" else if (lang == "ur") "شعبان" else "Sha'ban"
            lower.contains("dhu") && lower.contains("qa") -> if (lang == "ar") "ذو القعدة" else if (lang == "ur") "ذوالقعدہ" else "Dhu al-Qi'dah"
            lower.contains("dhu") && lower.contains("hij") -> if (lang == "ar") "ذو الحجة" else if (lang == "ur") "ذوالحجہ" else "Dhu al-Hijjah"
            else -> name
        }
    }
}
