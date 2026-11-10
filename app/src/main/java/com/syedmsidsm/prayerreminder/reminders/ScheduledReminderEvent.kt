package com.syedmsidsm.prayerreminder.reminders

import com.syedmsidsm.prayerreminder.domain.model.Prayer
import kotlinx.serialization.Serializable

enum class ReminderKind {
    BEFORE_AZAN,
    AT_AZAN,
    BEFORE_JAMAAT,
    TABLE_EXPIRING,
    MIDNIGHT,
    TEST
}

@Serializable
data class ScheduledReminderEvent(
    val code: Int,
    val fireAtEpochSeconds: Long,
    val prayer: Prayer? = null,
    val kind: ReminderKind,
    val isJumuah: Boolean = false,
    val minutesOffset: Int = 0,
    val azanTimeIso: String? = null,
    val jamaatTimeIso: String? = null,
    val isEstimated: Boolean = false,
    val tableMonthName: String? = null,
    val daysUntilExpiry: Int = 0
)
