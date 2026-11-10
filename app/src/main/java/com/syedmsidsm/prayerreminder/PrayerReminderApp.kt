package com.syedmsidsm.prayerreminder

import android.app.Application
import com.syedmsidsm.prayerreminder.notifications.NotificationHelper
import com.syedmsidsm.prayerreminder.reminders.AppRefresher
import com.syedmsidsm.prayerreminder.reminders.DailyMaintenanceWorker

class PrayerReminderApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        NotificationHelper(this).createNotificationChannels()
        DailyMaintenanceWorker.enqueue(this)
        AppRefresher.refreshAll(this, "startup")
    }
}
