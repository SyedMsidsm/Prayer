package com.syedmsidsm.prayerreminder.reminders

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MissedReminderDetector(
    private val context: Context,
    private val firedEventLog: FiredEventLog
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("scheduled_events_store", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun saveScheduledEvents(events: List<ScheduledReminderEvent>) {
        val encoded = json.encodeToString(events)
        prefs.edit().putString("scheduled_events_json", encoded).apply()
    }

    fun getScheduledEvents(): List<ScheduledReminderEvent> {
        val str = prefs.getString("scheduled_events_json", null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<ScheduledReminderEvent>>(str) }.getOrDefault(emptyList())
    }

    /**
     * Checks for any scheduled events that should have fired at least 2 minutes ago
     * but were not recorded in the fired event log.
     * Updates and returns the rolling 7-day missed count.
     */
    fun checkMissedReminders(nowEpochSeconds: Long): Int {
        val scheduled = getScheduledEvents()
        val missedCutoff = nowEpochSeconds - 120 // 2 minutes ago
        val sevenDaysAgo = nowEpochSeconds - (7 * 86400)

        // Stored missed codes with their fire timestamps
        val missedPrefs = context.getSharedPreferences("missed_reminders_log", Context.MODE_PRIVATE)
        val editor = missedPrefs.edit()

        for (event in scheduled) {
            // Only consider prayer reminders (not MIDNIGHT or TEST)
            if (event.prayer != null) {
                if (event.fireAtEpochSeconds in (sevenDaysAgo + 1)..missedCutoff) {
                    if (!firedEventLog.hasFired(event.code)) {
                        editor.putLong(event.code.toString(), event.fireAtEpochSeconds)
                    }
                }
            }
        }

        // Clean up entries older than 7 days
        val allMissed = missedPrefs.all
        var activeMissedCount = 0
        for ((key, value) in allMissed) {
            val ts = (value as? Long) ?: continue
            if (ts < sevenDaysAgo) {
                editor.remove(key)
            } else {
                activeMissedCount++
            }
        }
        editor.apply()

        // Store active count
        context.getSharedPreferences("reliability_stats", Context.MODE_PRIVATE)
            .edit()
            .putInt("missed_count_7d", activeMissedCount)
            .apply()

        return activeMissedCount
    }

    fun getMissedCount7d(): Int {
        return context.getSharedPreferences("reliability_stats", Context.MODE_PRIVATE)
            .getInt("missed_count_7d", 0)
    }
}
