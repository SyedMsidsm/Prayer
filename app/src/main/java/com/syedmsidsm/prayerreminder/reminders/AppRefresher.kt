package com.syedmsidsm.prayerreminder.reminders

import android.content.Context
import com.syedmsidsm.prayerreminder.PrayerReminderApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime

object AppRefresher {

    fun refreshAll(context: Context, reason: String) {
        val app = context.applicationContext as? PrayerReminderApp ?: return
        val container = app.container

        CoroutineScope(Dispatchers.Default).launch {
            val timetableRepo = container.timetableRepository
            val settingsRepo = container.settingsRepository
            val dayResolver = container.dayScheduleResolver

            val timetables = timetableRepo.timetables.value
            val rules = settingsRepo.jamaatRules.first()

            val now = ZonedDateTime.now()
            val todayDate = now.toLocalDate()
            val tomorrowDate = todayDate.plusDays(1)

            val todaySchedule = dayResolver.resolve(todayDate, timetables, rules)
            val tomorrowSchedule = dayResolver.resolve(tomorrowDate, timetables, rules)

            val settingsSnapshot = ReminderSettingsSnapshot(
                remBeforeAzanMin = settingsRepo.remBeforeAzanMin.first(),
                remJumuahBeforeAzanMin = settingsRepo.remJumuahBeforeAzanMin.first(),
                remAtAzan = settingsRepo.remAtAzan.first(),
                remBeforeJamaatMin = settingsRepo.remBeforeJamaatMin.first(),
                matrix = settingsRepo.reminderMatrix.first()
            )

            val latestTimetable = timetables.firstOrNull()

            val planner = ReminderPlanner()
            val events = planner.plan(
                now = now,
                today = todaySchedule,
                tomorrow = tomorrowSchedule,
                settings = settingsSnapshot,
                latestTimetable = latestTimetable
            )

            val firedLog = FiredEventLog(context)
            val missedDetector = MissedReminderDetector(context, firedLog)
            val alarmScheduler = AlarmScheduler(context, missedDetector)

            alarmScheduler.scheduleAll(events)

            // Check missed reminders
            missedDetector.checkMissedReminders(now.toEpochSecond())
        }
    }
}
