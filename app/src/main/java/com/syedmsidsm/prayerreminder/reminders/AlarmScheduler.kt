package com.syedmsidsm.prayerreminder.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.syedmsidsm.prayerreminder.ui.MainActivity

class AlarmScheduler(
    private val context: Context,
    private val missedReminderDetector: MissedReminderDetector
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAll(events: List<ScheduledReminderEvent>) {
        // 1. Cancel previously scheduled alarms
        val previousEvents = missedReminderDetector.getScheduledEvents()
        for (prev in previousEvents) {
            cancelAlarm(prev.code)
        }

        // 2. Schedule each new event
        for (event in events) {
            scheduleEvent(event)
        }

        // 3. Save new scheduled events list
        missedReminderDetector.saveScheduledEvents(events)
    }

    fun scheduleEvent(event: ScheduledReminderEvent) {
        val fireAtMillis = event.fireAtEpochSeconds * 1000L
        val pendingIntent = createAlarmPendingIntent(event)

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        when (event.kind) {
            ReminderKind.BEFORE_AZAN,
            ReminderKind.AT_AZAN,
            ReminderKind.BEFORE_JAMAAT,
            ReminderKind.AFTER_PRAYER,
            ReminderKind.TEST -> {
                if (canScheduleExact) {
                    val showIntent = PendingIntent.getActivity(
                        context,
                        event.code,
                        Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(fireAtMillis, showIntent)
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAtMillis, pendingIntent)
                }
            }
            ReminderKind.TABLE_EXPIRING,
            ReminderKind.MIDNIGHT,
            ReminderKind.PHASE -> {
                if (canScheduleExact) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAtMillis, pendingIntent)
                }
            }
        }
    }

    fun cancelAlarm(code: Int) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    private fun createAlarmPendingIntent(event: ScheduledReminderEvent): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_CODE, event.code)
            putExtra(ReminderAlarmReceiver.EXTRA_KIND, event.kind.name)
            putExtra(ReminderAlarmReceiver.EXTRA_PRAYER, event.prayer?.name)
            putExtra(ReminderAlarmReceiver.EXTRA_IS_JUMUAH, event.isJumuah)
            putExtra(ReminderAlarmReceiver.EXTRA_MINUTES_OFFSET, event.minutesOffset)
            putExtra(ReminderAlarmReceiver.EXTRA_AZAN_TIME, event.azanTimeIso)
            putExtra(ReminderAlarmReceiver.EXTRA_JAMAAT_TIME, event.jamaatTimeIso)
            putExtra(ReminderAlarmReceiver.EXTRA_IS_ESTIMATED, event.isEstimated)
            putExtra(ReminderAlarmReceiver.EXTRA_TABLE_MONTH, event.tableMonthName)
            putExtra(ReminderAlarmReceiver.EXTRA_DAYS_UNTIL_EXPIRY, event.daysUntilExpiry)
        }
        return PendingIntent.getBroadcast(
            context,
            event.code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
