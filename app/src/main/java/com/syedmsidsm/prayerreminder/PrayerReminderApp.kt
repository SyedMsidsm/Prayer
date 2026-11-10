package com.syedmsidsm.prayerreminder

import android.app.Application

class PrayerReminderApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
