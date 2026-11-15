package com.syedmsidsm.prayerreminder.widget

import android.content.Context

object WidgetUpdater {

    fun updateAll(context: Context) {
        try {
            com.syedmsidsm.prayerreminder.widgets.WidgetUpdater.updateAll(context)
        } catch (_: Exception) {}
    }
}
