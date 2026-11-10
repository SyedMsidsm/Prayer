package com.syedmsidsm.prayerreminder

import android.content.Context
import com.syedmsidsm.prayerreminder.data.SettingsRepository
import com.syedmsidsm.prayerreminder.data.TimetableRepository
import com.syedmsidsm.prayerreminder.domain.DayScheduleResolver
import com.syedmsidsm.prayerreminder.domain.NextPrayerResolver

/**
 * AppContainer provides manual dependency injection without external frameworks.
 */
class AppContainer(val context: Context) {
    val timetableRepository: TimetableRepository by lazy {
        TimetableRepository(context)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    val dayScheduleResolver: DayScheduleResolver by lazy {
        DayScheduleResolver()
    }

    val nextPrayerResolver: NextPrayerResolver by lazy {
        NextPrayerResolver()
    }
}
