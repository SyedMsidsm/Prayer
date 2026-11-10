package com.syedmsidsm.prayerreminder.reminders

import android.content.Context
import android.content.SharedPreferences

class FiredEventLog(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fired_events_log", Context.MODE_PRIVATE)

    fun record(code: Int, timestampSeconds: Long) {
        prefs.edit().putLong(code.toString(), timestampSeconds).apply()
        cleanupOldEntries(timestampSeconds)
    }

    fun hasFired(code: Int): Boolean {
        return prefs.contains(code.toString())
    }

    fun getFiredTimestamp(code: Int): Long? {
        val ts = prefs.getLong(code.toString(), -1L)
        return if (ts != -1L) ts else null
    }

    private fun cleanupOldEntries(currentTimestampSeconds: Long) {
        val sevenDaysAgo = currentTimestampSeconds - (7 * 86400)
        val all = prefs.all
        val editor = prefs.edit()
        var changed = false
        for ((key, value) in all) {
            val ts = (value as? Long) ?: continue
            if (ts < sevenDaysAgo) {
                editor.remove(key)
                changed = true
            }
        }
        if (changed) {
            editor.apply()
        }
    }
}
