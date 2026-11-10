package com.syedmsidsm.prayerreminder.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.reminders.ReminderKind
import com.syedmsidsm.prayerreminder.reminders.ReminderPlanner
import com.syedmsidsm.prayerreminder.reminders.ScheduledReminderEvent
import com.syedmsidsm.prayerreminder.ui.MainActivity
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import java.time.LocalDate
import java.time.LocalTime

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_VOICE = "reminders_voice"
        const val CHANNEL_SOUND = "reminders_sound"
        const val CHANNEL_GENERAL = "general"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. Channel Voice: High importance, silent sound (app plays speech), vibration on
            val voiceChannel = NotificationChannel(
                CHANNEL_VOICE,
                context.getString(R.string.channel_voice),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Spoken prayer reminders"
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            // 2. Channel Sound: High importance, default notification sound, vibration on
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val soundChannel = NotificationChannel(
                CHANNEL_SOUND,
                context.getString(R.string.channel_sound),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Standard chime prayer reminders"
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .build()
                )
                enableVibration(true)
            }

            // 3. Channel General: Default importance, default sound
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                context.getString(R.string.channel_general),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General timetable alerts"
            }

            notificationManager.createNotificationChannels(listOf(voiceChannel, soundChannel, generalChannel))
        }
    }

    fun postReminderNotification(
        event: ScheduledReminderEvent,
        voiceEnabled: Boolean,
        date: LocalDate = LocalDate.now()
    ) {
        val channelId = if (voiceEnabled) CHANNEL_VOICE else CHANNEL_SOUND

        val contentIntent = PendingIntent.getActivity(
            context,
            event.code,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val amMarker = context.getString(R.string.am_marker)
        val pmMarker = context.getString(R.string.pm_marker)

        val prayerName = if (event.isJumuah) {
            context.getString(R.string.prayer_jumuah)
        } else {
            event.prayer?.let { getPrayerName(it) } ?: ""
        }

        val azanTime = event.azanTimeIso?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
        val jamaatTime = event.jamaatTimeIso?.let { runCatching { LocalTime.parse(it) }.getOrNull() }

        val azanFormatted = azanTime?.let { TimeFormatter.format12Hour(it, amMarker, pmMarker) } ?: ""
        val jamaatFormatted = jamaatTime?.let { TimeFormatter.format12Hour(it, amMarker, pmMarker) } ?: ""

        val (title, text) = when (event.kind) {
            ReminderKind.BEFORE_AZAN -> {
                val minStr = context.resources.getQuantityString(R.plurals.minutes, event.minutesOffset, event.minutesOffset)
                val t = context.getString(R.string.notif_before_azan, prayerName, minStr)
                var body = context.getString(R.string.notif_azan_jamaat_text, azanFormatted, jamaatFormatted)
                if (event.isEstimated) body += " " + context.getString(R.string.estimated_suffix)
                Pair(t, body)
            }
            ReminderKind.AT_AZAN -> {
                val t = context.getString(R.string.notif_at_azan, prayerName, azanFormatted)
                var body = context.getString(R.string.notif_jamaat_text, jamaatFormatted)
                if (event.isEstimated) body += " " + context.getString(R.string.estimated_suffix)
                Pair(t, body)
            }
            ReminderKind.BEFORE_JAMAAT -> {
                val minStr = context.resources.getQuantityString(R.plurals.minutes, event.minutesOffset, event.minutesOffset)
                val t = context.getString(R.string.notif_before_jamaat, prayerName, minStr)
                var body = context.getString(R.string.notif_jamaat_text, jamaatFormatted)
                if (event.isEstimated) body += " " + context.getString(R.string.estimated_suffix)
                Pair(t, body)
            }
            ReminderKind.TABLE_EXPIRING -> {
                val t = context.getString(R.string.notif_new_timetable_title)
                val daysStr = context.resources.getQuantityString(R.plurals.minutes, event.daysUntilExpiry, event.daysUntilExpiry)
                val body = context.getString(R.string.notif_new_timetable_text, event.tableMonthName ?: "", "$event.daysUntilExpiry days")
                Pair(t, body)
            }
            ReminderKind.TEST -> {
                Pair(context.getString(R.string.app_name), context.getString(R.string.notif_test))
            }
            ReminderKind.MIDNIGHT -> return
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_prayer)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        try {
            NotificationManagerCompat.from(context).notify(event.code, builder.build())
        } catch (_: SecurityException) {
            // Notifications permission not granted
        }

        // Handle notification replacements (Spec 10.3):
        // AT_AZAN cancels BEFORE_AZAN; BEFORE_JAMAAT cancels AT_AZAN
        event.prayer?.let { p ->
            when (event.kind) {
                ReminderKind.AT_AZAN -> {
                    val beforeAzanCode = ReminderPlanner.calculateCode(date, p, ReminderKind.BEFORE_AZAN)
                    cancel(beforeAzanCode)
                }
                ReminderKind.BEFORE_JAMAAT -> {
                    val atAzanCode = ReminderPlanner.calculateCode(date, p, ReminderKind.AT_AZAN)
                    cancel(atAzanCode)
                }
                else -> {}
            }
        }
    }

    fun buildForegroundServiceNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_VOICE)
            .setSmallIcon(R.drawable.ic_stat_prayer)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notif_playing))
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun cancel(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    private fun getPrayerName(prayer: Prayer): String {
        return when (prayer) {
            Prayer.FAJR -> context.getString(R.string.prayer_fajr)
            Prayer.SUNRISE -> context.getString(R.string.prayer_sunrise)
            Prayer.DHUHR -> context.getString(R.string.prayer_dhuhr)
            Prayer.ASR -> context.getString(R.string.prayer_asr)
            Prayer.MAGHRIB -> context.getString(R.string.prayer_maghrib)
            Prayer.ISHA -> context.getString(R.string.prayer_isha)
        }
    }
}
