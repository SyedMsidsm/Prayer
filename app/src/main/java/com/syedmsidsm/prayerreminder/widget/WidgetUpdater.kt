package com.syedmsidsm.prayerreminder.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdater {

    fun updateAll(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                SmallPrayerWidget().updateAll(context)
            } catch (_: Exception) {}
            try {
                WidePrayerWidget().updateAll(context)
            } catch (_: Exception) {}
            try {
                StripPrayerWidget().updateAll(context)
            } catch (_: Exception) {}
        }
    }
}
