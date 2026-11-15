package com.syedmsidsm.prayerreminder.widgets

import android.content.Context
import com.syedmsidsm.prayerreminder.AppContainer
import com.syedmsidsm.prayerreminder.PrayerReminderApp
import com.syedmsidsm.prayerreminder.domain.model.HijriDate
import com.syedmsidsm.prayerreminder.domain.model.Prayer as DomainPrayer
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime

enum class Prayer { FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA }

/** adhan = call to prayer, namaz = congregation time (null when there is none, e.g. sunrise). */
data class Slot(val prayer: Prayer, val adhan: LocalTime, val namaz: LocalTime?)

data class DaySchedule(val date: LocalDate, val slots: List<Slot>) {
    fun slot(p: Prayer): Slot = slots.first { it.prayer == p }
    val fivePrayers: List<Slot> get() = slots.filter { it.prayer != Prayer.SUNRISE }
}

object PrayerDataSource {
    private fun resolveDomainSchedule(
        context: Context,
        date: LocalDate
    ): Pair<com.syedmsidsm.prayerreminder.domain.model.DaySchedule, AppContainer> {
        val appContext = context.applicationContext
        val app = appContext as? PrayerReminderApp
        val container = app?.container ?: AppContainer(appContext)

        var timetables = container.timetableRepository.timetables.value
        if (timetables.isEmpty()) {
            runBlocking {
                container.timetableRepository.initialize()
            }
            timetables = container.timetableRepository.timetables.value
        }

        val rules = runBlocking {
            container.settingsRepository.jamaatRules.firstOrNull()
        } ?: emptyMap()

        val resolved = container.dayScheduleResolver.resolve(
            date = date,
            timetables = timetables,
            jamaatRules = rules,
            useCalculatedFallback = true
        )
        return resolved to container
    }

    fun scheduleFor(context: Context, date: LocalDate): DaySchedule {
        val (resolved, _) = resolveDomainSchedule(context, date)

        fun adhanOf(p: DomainPrayer, defH: Int, defM: Int): LocalTime =
            resolved.azan[p] ?: LocalTime.of(defH, defM)

        fun namazOf(p: DomainPrayer, defH: Int, defM: Int): LocalTime =
            resolved.jamaat[p] ?: adhanOf(p, defH, defM).plusMinutes(15)

        return DaySchedule(
            date = date,
            slots = listOf(
                Slot(Prayer.FAJR, adhanOf(DomainPrayer.FAJR, 5, 6), namazOf(DomainPrayer.FAJR, 5, 21)),
                Slot(Prayer.SUNRISE, adhanOf(DomainPrayer.SUNRISE, 6, 17), null),
                Slot(Prayer.DHUHR, adhanOf(DomainPrayer.DHUHR, 12, 10), namazOf(DomainPrayer.DHUHR, 12, 25)),
                Slot(Prayer.ASR, adhanOf(DomainPrayer.ASR, 15, 32), namazOf(DomainPrayer.ASR, 15, 45)),
                Slot(Prayer.MAGHRIB, adhanOf(DomainPrayer.MAGHRIB, 17, 56), namazOf(DomainPrayer.MAGHRIB, 17, 58)),
                Slot(Prayer.ISHA, adhanOf(DomainPrayer.ISHA, 19, 12), namazOf(DomainPrayer.ISHA, 19, 27))
            )
        )
    }

    fun hijriFor(context: Context, date: LocalDate): HijriDate {
        val (resolved, container) = resolveDomainSchedule(context, date)
        return resolved.hijri
            ?: container.dayScheduleResolver.estimateHijriDate(
                date,
                container.timetableRepository.timetables.value
            )
    }
}
