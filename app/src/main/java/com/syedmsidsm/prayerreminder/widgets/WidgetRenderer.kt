package com.syedmsidsm.prayerreminder.widgets

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.ui.MainActivity
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale

enum class WidgetSize { SMALL, MEDIUM, LARGE }

object WidgetRenderer {
    private val DARK = 0xFF1A1F3C.toInt()
    private val DARK_SOFT = 0xFF3A3F5C.toInt()
    private val WHITE = 0xFFFFFFFF.toInt()
    private val WHITE_SOFT = 0xFFD9E2F2.toInt()
    private const val DASH = "\u2014"

    // root, name, adhan label, adhan, namaz label, namaz
    private val CELLS = arrayOf(
        intArrayOf(R.id.m1, R.id.m1_name, R.id.m1_al, R.id.m1_adhan, R.id.m1_nl, R.id.m1_namaz),
        intArrayOf(R.id.m2, R.id.m2_name, R.id.m2_al, R.id.m2_adhan, R.id.m2_nl, R.id.m2_namaz),
        intArrayOf(R.id.m3, R.id.m3_name, R.id.m3_al, R.id.m3_adhan, R.id.m3_nl, R.id.m3_namaz),
        intArrayOf(R.id.m4, R.id.m4_name, R.id.m4_al, R.id.m4_adhan, R.id.m4_nl, R.id.m4_namaz),
        intArrayOf(R.id.m5, R.id.m5_name, R.id.m5_al, R.id.m5_adhan, R.id.m5_nl, R.id.m5_namaz)
    )

    // root, name, adhan, namaz
    private val ROWS = arrayOf(
        intArrayOf(R.id.r1, R.id.r1_name, R.id.r1_adhan, R.id.r1_namaz),
        intArrayOf(R.id.r2, R.id.r2_name, R.id.r2_adhan, R.id.r2_namaz),
        intArrayOf(R.id.r3, R.id.r3_name, R.id.r3_adhan, R.id.r3_namaz),
        intArrayOf(R.id.r4, R.id.r4_name, R.id.r4_adhan, R.id.r4_namaz),
        intArrayOf(R.id.r5, R.id.r5_name, R.id.r5_adhan, R.id.r5_namaz),
        intArrayOf(R.id.r6, R.id.r6_name, R.id.r6_adhan, R.id.r6_namaz)
    )

    fun render(
        base: Context,
        size: WidgetSize,
        heightDp: Int = 0,
        now: LocalDateTime = LocalDateTime.now()
    ): RemoteViews {
        val ctx = WidgetLocale.wrap(base)
        val loc: Locale = ctx.resources.configuration.locales[0]
        val next = NextPrayer.compute(ctx, now)
        val layout = when (size) {
            WidgetSize.SMALL -> R.layout.widget_small
            WidgetSize.MEDIUM -> R.layout.widget_medium
            WidgetSize.LARGE -> R.layout.widget_large
        }
        val v = RemoteViews(base.packageName, layout)
        v.setInt(
            R.id.root, "setLayoutDirection",
            if (WidgetLocale.isRtl(loc)) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        )

        val openAppIntent = PendingIntent.getActivity(
            base,
            size.ordinal,
            Intent(base, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        v.setOnClickPendingIntent(R.id.root, openAppIntent)

        applySky(v, Sky.phaseAt(next.today, now.toLocalTime()))
        when (size) {
            WidgetSize.SMALL -> small(v, ctx, loc, next, now, heightDp)
            WidgetSize.MEDIUM -> medium(v, ctx, loc, next, now)
            WidgetSize.LARGE -> large(v, ctx, loc, next, now, heightDp)
        }
        return v
    }

    private fun applySky(v: RemoteViews, p: SkyPhase) {
        v.setImageViewResource(
            R.id.sky_bg, when (p) {
                SkyPhase.DAWN -> R.drawable.sky_dawn
                SkyPhase.DAY -> R.drawable.sky_day
                SkyPhase.DUSK -> R.drawable.sky_dusk
                SkyPhase.NIGHT -> R.drawable.sky_night
            }
        )
        v.setImageViewResource(
            R.id.sky_body, when (p) {
                SkyPhase.DAY -> R.drawable.ic_sun_day
                SkyPhase.DAWN, SkyPhase.DUSK -> R.drawable.ic_sun_warm
                SkyPhase.NIGHT -> R.drawable.ic_moon
            }
        )
        v.setViewVisibility(
            R.id.sky_stars,
            if (p == SkyPhase.NIGHT || p == SkyPhase.DUSK) View.VISIBLE else View.INVISIBLE
        )
    }

    private fun nameRes(p: Prayer) = when (p) {
        Prayer.FAJR -> R.string.w_prayer_fajr
        Prayer.SUNRISE -> R.string.w_prayer_sunrise
        Prayer.DHUHR -> R.string.w_prayer_dhuhr
        Prayer.ASR -> R.string.w_prayer_asr
        Prayer.MAGHRIB -> R.string.w_prayer_maghrib
        Prayer.ISHA -> R.string.w_prayer_isha
    }

    /** Live countdown that ticks by itself (no battery-hungry refresh every second). */
    private fun countdown(v: RemoteViews, id: Int, next: NextInfo, now: LocalDateTime) {
        val ms = Duration.between(now, next.at).toMillis().coerceAtLeast(0L)
        v.setChronometerCountDown(id, true)
        v.setChronometer(id, SystemClock.elapsedRealtime() + ms, "%s", true)
    }

    private fun small(v: RemoteViews, ctx: Context, loc: Locale, next: NextInfo, now: LocalDateTime, heightDp: Int) {
        val date = now.toLocalDate()
        v.setTextViewText(R.id.next_label, ctx.getString(R.string.next_label, ctx.getString(nameRes(next.slot.prayer))))
        countdown(v, R.id.countdown, next, now)
        v.setTextViewText(R.id.until_label, ctx.getString(R.string.until_adhan))
        // Very short widget? Drop the caption instead of letting anything get clipped.
        v.setViewVisibility(R.id.until_label, if (heightDp in 1..124) View.GONE else View.VISIBLE)
        v.setTextViewText(R.id.adhan_label, ctx.getString(R.string.adhan))
        v.setTextViewText(R.id.namaz_label, ctx.getString(R.string.namaz))
        v.setTextViewText(R.id.adhan_time, DateText.time(ctx, next.slot.adhan, loc, true))
        v.setTextViewText(R.id.namaz_time, next.slot.namaz?.let { DateText.time(ctx, it, loc, true) } ?: DASH)
        v.setTextViewText(R.id.date_en, DateText.gregorian(date, loc, false))
        v.setTextViewText(R.id.date_hijri, DateText.hijri(ctx, date, loc, true))
    }

    private fun medium(v: RemoteViews, ctx: Context, loc: Locale, next: NextInfo, now: LocalDateTime) {
        val date = now.toLocalDate()
        v.setTextViewText(R.id.date_en, DateText.gregorian(date, loc, true))
        v.setTextViewText(R.id.date_hijri, DateText.hijri(ctx, date, loc, false))
        v.setTextViewText(R.id.headline, ctx.getString(R.string.headline_in, ctx.getString(nameRes(next.slot.prayer))))
        countdown(v, R.id.countdown, next, now)
        next.today.fivePrayers.forEachIndexed { i, slot ->
            val c = CELLS[i]
            val hi = slot.prayer == next.slot.prayer
            v.setInt(c[0], "setBackgroundResource", if (hi) R.drawable.bg_next else R.drawable.bg_glass_cell)
            v.setTextViewText(c[1], ctx.getString(nameRes(slot.prayer)))
            v.setTextViewText(c[2], ctx.getString(R.string.adhan))
            v.setTextViewText(c[3], DateText.time(ctx, slot.adhan, loc, false))
            v.setTextViewText(c[4], ctx.getString(R.string.namaz))
            v.setTextViewText(c[5], slot.namaz?.let { DateText.time(ctx, it, loc, false) } ?: DASH)
            v.setTextColor(c[1], if (hi) DARK else WHITE)
            v.setTextColor(c[2], if (hi) DARK_SOFT else WHITE_SOFT)
            v.setTextColor(c[3], if (hi) DARK else WHITE)
            v.setTextColor(c[4], if (hi) DARK_SOFT else WHITE_SOFT)
            v.setTextColor(c[5], if (hi) DARK else WHITE)
        }
    }

    private fun large(v: RemoteViews, ctx: Context, loc: Locale, next: NextInfo, now: LocalDateTime, heightDp: Int) {
        val date = now.toLocalDate()
        v.setTextViewText(R.id.date_en, DateText.gregorian(date, loc, true))
        v.setTextViewText(R.id.date_hijri, DateText.hijri(ctx, date, loc, false))
        v.setTextViewText(R.id.next_title, ctx.getString(R.string.next_prayer_title))
        v.setTextViewText(R.id.next_name, ctx.getString(nameRes(next.slot.prayer)))
        v.setTextViewText(R.id.in_label, ctx.getString(R.string.in_label))
        countdown(v, R.id.countdown, next, now)

        val total = Duration.between(next.previousAt, next.at).toMillis().coerceAtLeast(1L)
        val elapsed = Duration.between(next.previousAt, now).toMillis().coerceIn(0L, total)
        v.setProgressBar(R.id.progress, 1000, (elapsed * 1000 / total).toInt(), false)
        v.setViewVisibility(R.id.progress, if (heightDp in 1..239) View.GONE else View.VISIBLE)

        v.setTextViewText(R.id.h_prayer, ctx.getString(R.string.col_prayer))
        v.setTextViewText(R.id.h_adhan, ctx.getString(R.string.adhan))
        v.setTextViewText(R.id.h_namaz, ctx.getString(R.string.namaz))
        next.today.slots.forEachIndexed { i, slot ->
            val r = ROWS[i]
            val hi = slot.prayer == next.slot.prayer
            v.setInt(r[0], "setBackgroundResource", if (hi) R.drawable.bg_next_row else R.drawable.bg_row)
            v.setTextViewText(r[1], ctx.getString(nameRes(slot.prayer)))
            v.setTextViewText(r[2], DateText.time(ctx, slot.adhan, loc, true))
            v.setTextViewText(r[3], slot.namaz?.let { DateText.time(ctx, it, loc, true) } ?: DASH)
            for (k in 1..3) v.setTextColor(r[k], if (hi) DARK else WHITE)
        }
    }
}
