package com.syedmsidsm.prayerreminder.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.syedmsidsm.prayerreminder.PrayerReminderApp
import com.syedmsidsm.prayerreminder.audio.VoicePlaybackService
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.notifications.NotificationHelper
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

class ReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_CODE = "extra_code"
        const val EXTRA_KIND = "extra_kind"
        const val EXTRA_PRAYER = "extra_prayer"
        const val EXTRA_IS_JUMUAH = "extra_is_jumuah"
        const val EXTRA_MINUTES_OFFSET = "extra_minutes_offset"
        const val EXTRA_AZAN_TIME = "extra_azan_time"
        const val EXTRA_JAMAAT_TIME = "extra_jamaat_time"
        const val EXTRA_IS_ESTIMATED = "extra_is_estimated"
        const val EXTRA_TABLE_MONTH = "extra_table_month"
        const val EXTRA_DAYS_UNTIL_EXPIRY = "extra_days_until_expiry"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                withTimeoutOrNull(8000L) {
                    processAlarm(context, intent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processAlarm(context: Context, intent: Intent) {
        val code = intent.getIntExtra(EXTRA_CODE, -1)
        val kindStr = intent.getStringExtra(EXTRA_KIND) ?: return
        val kind = runCatching { ReminderKind.valueOf(kindStr) }.getOrNull() ?: return
        val prayerStr = intent.getStringExtra(EXTRA_PRAYER)
        val prayer = prayerStr?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }
        val isJumuah = intent.getBooleanExtra(EXTRA_IS_JUMUAH, false)
        val minutesOffset = intent.getIntExtra(EXTRA_MINUTES_OFFSET, 0)
        val azanTimeIso = intent.getStringExtra(EXTRA_AZAN_TIME)
        val jamaatTimeIso = intent.getStringExtra(EXTRA_JAMAAT_TIME)
        val isEstimated = intent.getBooleanExtra(EXTRA_IS_ESTIMATED, false)
        val tableMonth = intent.getStringExtra(EXTRA_TABLE_MONTH)
        val daysUntilExpiry = intent.getIntExtra(EXTRA_DAYS_UNTIL_EXPIRY, 0)

        val app = context.applicationContext as? PrayerReminderApp
        val container = app?.container ?: return
        val settingsRepo = container.settingsRepository
        val notificationHelper = NotificationHelper(context)
        val firedEventLog = FiredEventLog(context)

        val now = ZonedDateTime.now()
        val nowEpochSeconds = now.toEpochSecond()

        // Handle MIDNIGHT: silent refresh only
        if (kind == ReminderKind.MIDNIGHT) {
            firedEventLog.record(code, nowEpochSeconds)
            AppRefresher.refreshAll(context, "midnight")
            return
        }

        // Handle PHASE: silent refresh for widget and next prayer phase change
        if (kind == ReminderKind.PHASE) {
            firedEventLog.record(code, nowEpochSeconds)
            AppRefresher.refreshAll(context, "phase_change")
            return
        }

        // Handle TEST reminder
        if (kind == ReminderKind.TEST) {
            val event = ScheduledReminderEvent(
                code = code,
                fireAtEpochSeconds = nowEpochSeconds,
                kind = ReminderKind.TEST
            )
            notificationHelper.postReminderNotification(event, voiceEnabled = true)

            val voiceLang = settingsRepo.voiceLanguage.first()
            val voiceGender = settingsRepo.voiceGender.first()
            val voiceChime = settingsRepo.voiceChime.first()
            val voiceOnSilent = settingsRepo.voiceOnSilent.first()

            VoicePlaybackService.start(
                context = context,
                language = voiceLang,
                gender = voiceGender,
                kind = "before_azan",
                prayer = "asr",
                minutes = 10,
                chime = voiceChime,
                overrideSilent = voiceOnSilent
            )

            val timeStr = TimeFormatter.format12Hour(LocalTime.now(), "AM", "PM")
            settingsRepo.setLastTestResult("Test passed at $timeStr (voice played)")
            firedEventLog.record(code, nowEpochSeconds)
            return
        }

        // Re-check if this reminder is still enabled in settings
        val prayerKey = if (isJumuah) "JUMUAH" else (prayer?.name ?: "")
        val matrix = settingsRepo.reminderMatrix.first()
        val kindMatrixStr = when (kind) {
            ReminderKind.BEFORE_AZAN -> "BEFORE_AZAN"
            ReminderKind.AT_AZAN -> "AT_AZAN"
            ReminderKind.BEFORE_JAMAAT -> "BEFORE_JAMAAT"
            else -> ""
        }

        if (kindMatrixStr.isNotEmpty() && prayerKey.isNotEmpty()) {
            val isEnabled = matrix[Pair(prayerKey, kindMatrixStr)] ?: true
            if (!isEnabled) {
                // User disabled this reminder since it was planned -> skip
                firedEventLog.record(code, nowEpochSeconds)
                AppRefresher.refreshAll(context, "alarm_skipped")
                return
            }
        }

        val event = ScheduledReminderEvent(
            code = code,
            fireAtEpochSeconds = nowEpochSeconds,
            prayer = prayer,
            kind = kind,
            isJumuah = isJumuah,
            minutesOffset = minutesOffset,
            azanTimeIso = azanTimeIso,
            jamaatTimeIso = jamaatTimeIso,
            isEstimated = isEstimated,
            tableMonthName = tableMonth,
            daysUntilExpiry = daysUntilExpiry
        )

        val voiceEnabled = settingsRepo.voiceEnabled.first()
        val alertMode = if (prayer != null) {
            settingsRepo.prayerAlertModes.first()[prayer] ?: "sound"
        } else {
            "sound"
        }

        if (alertMode == "muted") {
            firedEventLog.record(code, nowEpochSeconds)
            AppRefresher.refreshAll(context, "alarm_muted")
            return
        }

        val playSoundForPrayer = voiceEnabled && alertMode == "sound"
        notificationHelper.postReminderNotification(event, voiceEnabled = playSoundForPrayer, date = LocalDate.now())

        // Play voice if enabled and alert mode is sound (skip for vibrate-only mode)
        if (playSoundForPrayer && prayer != null) {
            val voiceLang = settingsRepo.voiceLanguage.first()
            val voiceGender = settingsRepo.voiceGender.first()
            val voiceChime = settingsRepo.voiceChime.first()
            val voiceOnSilent = settingsRepo.voiceOnSilent.first()
            val atAzanType = settingsRepo.atAzanType.first()
            val azanReciter = settingsRepo.azanReciter.first()
            val azanLength = settingsRepo.azanLength.first()
            val playDua = settingsRepo.playDuaAfterAzan.first()

            val voiceKindStr = when (kind) {
                ReminderKind.BEFORE_AZAN -> if (isJumuah) "jumuah_before_azan" else "before_azan"
                ReminderKind.AT_AZAN -> "at_azan"
                ReminderKind.BEFORE_JAMAAT -> "before_jamaat"
                else -> ""
            }

            if (voiceKindStr.isNotEmpty()) {
                VoicePlaybackService.start(
                    context = context,
                    language = voiceLang,
                    gender = voiceGender,
                    kind = voiceKindStr,
                    prayer = prayer.name.lowercase(),
                    minutes = minutesOffset,
                    chime = voiceChime,
                    overrideSilent = voiceOnSilent,
                    atAzanType = atAzanType,
                    azanReciter = azanReciter,
                    azanLength = azanLength,
                    playDua = playDua
                )
            }
        }

        firedEventLog.record(code, nowEpochSeconds)
        AppRefresher.refreshAll(context, "alarm")
    }
}
