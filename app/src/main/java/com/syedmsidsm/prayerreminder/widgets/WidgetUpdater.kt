package com.syedmsidsm.prayerreminder.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.SizeF
import android.widget.RemoteViews
import java.time.LocalDateTime
import java.time.ZoneId

object WidgetUpdater {
    private val PROVIDERS = listOf(
        SmallWidgetProvider::class.java to WidgetSize.SMALL,
        MediumWidgetProvider::class.java to WidgetSize.MEDIUM,
        LargeWidgetProvider::class.java to WidgetSize.LARGE
    )

    /** Call this from your app whenever language, location or prayer settings change. */
    fun updateAll(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        for ((cls, size) in PROVIDERS) {
            for (id in mgr.getAppWidgetIds(ComponentName(context, cls))) {
                val h = mgr.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
                mgr.updateAppWidget(id, WidgetRenderer.render(context, size, h))
            }
        }

        val adaptive = ComponentName(context, AdaptiveWidgetProvider::class.java)
        for (id in mgr.getAppWidgetIds(adaptive)) {
            val o = mgr.getAppWidgetOptions(id)
            val w = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
            val h = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
            val views = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+: the launcher swaps layouts instantly while the user drags.
                RemoteViews(
                    mapOf(
                        SizeF(110f, 110f) to WidgetRenderer.render(context, WidgetSize.SMALL, 110),
                        SizeF(250f, 110f) to WidgetRenderer.render(context, WidgetSize.MEDIUM, 110),
                        SizeF(250f, 250f) to WidgetRenderer.render(context, WidgetSize.LARGE, 250)
                    )
                )
            } else {
                val size = when {
                    h >= 230 -> WidgetSize.LARGE
                    w >= 230 -> WidgetSize.MEDIUM
                    else -> WidgetSize.SMALL
                }
                WidgetRenderer.render(context, size, h)
            }
            mgr.updateAppWidget(id, views)
        }

        WidgetScheduler.scheduleNext(context)
    }

    fun hasAnyWidget(context: Context): Boolean {
        val mgr = AppWidgetManager.getInstance(context)
        val hasFixed = PROVIDERS.any { (cls, _) ->
            mgr.getAppWidgetIds(ComponentName(context, cls)).isNotEmpty()
        }
        val hasAdaptive = mgr.getAppWidgetIds(ComponentName(context, AdaptiveWidgetProvider::class.java)).isNotEmpty()
        return hasFixed || hasAdaptive
    }
}

/** Wakes the widgets exactly when something visible changes (adhan, namaz, sky phase, midnight). */
object WidgetScheduler {
    const val ACTION_REFRESH = "com.syedmsidsm.prayerreminder.widgets.ACTION_REFRESH"

    private fun pending(ctx: Context): PendingIntent = PendingIntent.getBroadcast(
        ctx, 0,
        Intent(ctx, WidgetUpdateReceiver::class.java).setAction(ACTION_REFRESH),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun scheduleNext(ctx: Context) {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now()
        val nowMs = now.atZone(zone).toInstant().toEpochMilli()
        val times = mutableListOf<LocalDateTime>()
        for (offset in 0L..1L) {
            val d = now.toLocalDate().plusDays(offset)
            val s = PrayerDataSource.scheduleFor(ctx, d)
            s.slots.forEach {
                times += LocalDateTime.of(d, it.adhan)
                it.namaz?.let { n -> times += LocalDateTime.of(d, n) }
            }
            Sky.boundaries(s).forEach { times += LocalDateTime.of(d, it) }
        }
        times += now.toLocalDate().plusDays(1).atTime(0, 1)
        val nextMs = times.map { it.atZone(zone).toInstant().toEpochMilli() }
            .filter { it > nowMs + 1000 }.minOrNull() ?: return

        val am = ctx.getSystemService(AlarmManager::class.java)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMs + 500, pending(ctx))
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMs + 500, pending(ctx))
    }

    fun cancel(ctx: Context) {
        if (WidgetUpdater.hasAnyWidget(ctx)) return
        ctx.getSystemService(AlarmManager::class.java).cancel(pending(ctx))
    }
}

/** Handles our alarm + reboot, clock change, timezone change, language change, app update. */
class WidgetUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WidgetUpdater.updateAll(context)
    }
}
