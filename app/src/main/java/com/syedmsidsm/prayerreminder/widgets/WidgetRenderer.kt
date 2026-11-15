package com.syedmsidsm.prayerreminder.widgets

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.SystemClock
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.ui.MainActivity
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

enum class WidgetSize { SMALL, MEDIUM, LARGE }

object WidgetRenderer {
    private val HIGHLIGHT_TEXT = 0xFF2A1442.toInt()
    private val WHITE = 0xFFFFFFFF.toInt()
    private val WHITE_95 = 0xF2FFFFFF.toInt()
    private const val DASH = "\u2014"

    // root, name, adhan, namaz
    private val CELLS = arrayOf(
        intArrayOf(R.id.m1, R.id.m1_name, R.id.m1_adhan, R.id.m1_namaz),
        intArrayOf(R.id.m2, R.id.m2_name, R.id.m2_adhan, R.id.m2_namaz),
        intArrayOf(R.id.m3, R.id.m3_name, R.id.m3_adhan, R.id.m3_namaz),
        intArrayOf(R.id.m4, R.id.m4_name, R.id.m4_adhan, R.id.m4_namaz),
        intArrayOf(R.id.m5, R.id.m5_name, R.id.m5_adhan, R.id.m5_namaz)
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
        now: LocalDateTime = LocalDateTime.now(),
        widthDp: Int = 0
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

        v.setInt(R.id.root, "setBackgroundResource", R.drawable.sky_dusk)
        v.setImageViewResource(R.id.sky_body, R.drawable.ic_sun_warm)
        v.setViewVisibility(R.id.sky_stars, View.GONE)

        when (size) {
            WidgetSize.SMALL -> small(v, ctx, loc, next, now, widthDp, heightDp)
            WidgetSize.MEDIUM -> medium(v, ctx, loc, next, now, widthDp, heightDp)
            WidgetSize.LARGE -> large(v, ctx, loc, next, now, widthDp, heightDp)
        }
        return v
    }

    private fun nameRes(p: Prayer) = when (p) {
        Prayer.FAJR -> R.string.w_prayer_fajr
        Prayer.SUNRISE -> R.string.w_prayer_sunrise
        Prayer.DHUHR -> R.string.w_prayer_dhuhr
        Prayer.ASR -> R.string.w_prayer_asr
        Prayer.MAGHRIB -> R.string.w_prayer_maghrib
        Prayer.ISHA -> R.string.w_prayer_isha
    }

    private fun countdown(v: RemoteViews, id: Int, next: NextInfo, now: LocalDateTime, format: String = "%s") {
        val ms = Duration.between(now, next.at).toMillis().coerceAtLeast(0L)
        v.setChronometerCountDown(id, true)
        v.setChronometer(id, SystemClock.elapsedRealtime() + ms, format, true)
    }

    private fun chronoSampleFor(now: LocalDateTime, nextAt: LocalDateTime): String {
        val mins = Duration.between(now, nextAt).toMinutes().coerceAtLeast(0L)
        return when {
            mins >= 600L -> "88:88:88"
            mins >= 60L -> "8:88:88"
            else -> "88:88"
        }
    }

    private fun dpToPx(ctx: Context, dp: Float): Int =
        (dp * ctx.resources.displayMetrics.density).roundToInt()

    private fun fontDamp(ctx: Context): Float {
        val fs = ctx.resources.configuration.fontScale.coerceAtLeast(0.85f)
        return if (fs > 1f) fs.pow(0.75f) else 1f
    }

    private fun measureWidthDp(
        ctx: Context,
        text: String,
        sp: Float,
        bold: Boolean = true,
        letterSpacingEm: Float = 0f
    ): Float {
        val dm = ctx.resources.displayMetrics
        @Suppress("DEPRECATION")
        val scaledDensity = dm.scaledDensity
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            letterSpacing = letterSpacingEm
            textSize = sp * scaledDensity
        }
        return paint.measureText(text) / dm.density
    }

    private fun fitSp(
        ctx: Context,
        text: String,
        targetSp: Float,
        maxWidthDp: Float,
        bold: Boolean = true,
        letterSpacingEm: Float = 0f,
        minSp: Float = 8f
    ): Float {
        if (maxWidthDp <= 0f || text.isEmpty()) return targetSp
        var sp = targetSp
        while (sp > minSp) {
            if (measureWidthDp(ctx, text, sp, bold, letterSpacingEm) <= maxWidthDp) break
            sp -= 0.4f
        }
        return sp.coerceAtLeast(minSp)
    }

    private fun setSp(v: RemoteViews, id: Int, sp: Float) {
        v.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, sp)
    }

    private fun small(
        v: RemoteViews,
        ctx: Context,
        loc: Locale,
        next: NextInfo,
        now: LocalDateTime,
        widthDp: Int,
        heightDp: Int
    ) {
        // Default 2x2 grid size on launcher is ~125x221dp
        val w = (if (widthDp > 0) widthDp else 125).toFloat()
        val h = (if (heightDp > 0) heightDp else 221).toFloat()
        val s = min(w / 125f, h / 221f).coerceIn(0.75f, 1.8f)
        val fd = fontDamp(ctx)
        fun sz(baseSp: Float): Float = (baseSp * s) / fd

        val padTop = (11f * s).coerceAtLeast(8f)
        val padSide = (9f * s).coerceAtLeast(7f)
        val padBot = (10f * s).coerceAtLeast(8f)
        v.setViewPadding(
            R.id.content_box,
            dpToPx(ctx, padSide),
            dpToPx(ctx, padTop),
            dpToPx(ctx, padSide),
            dpToPx(ctx, padBot)
        )

        val sunSize = (20f * s).coerceAtLeast(16f)
        val sunMargin = (10f * s).coerceAtLeast(8f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            v.setViewLayoutWidth(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutHeight(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_TOP, sunMargin, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_END, sunMargin, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.next_label, RemoteViews.MARGIN_END, sunSize + 3f, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.until_label, RemoteViews.MARGIN_TOP, (3f * s).coerceAtLeast(2f), TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.small_namaz_row, RemoteViews.MARGIN_TOP, (6f * s).coerceAtLeast(4f), TypedValue.COMPLEX_UNIT_DIP)
        }

        val innerW = (w - 2f * padSide).coerceAtLeast(80f)
        val topAvailW = (innerW - sunSize - 3f).coerceAtLeast(58f)

        val prayerNameUpper = ctx.getString(nameRes(next.slot.prayer)).uppercase(loc)
        val fullTop = ctx.getString(R.string.next_label, prayerNameUpper).uppercase(loc)
        val targetTopSp = sz(11.5f)
        val fallbackTopSp = sz(10.2f)
        val (topText, topSp) = when {
            measureWidthDp(ctx, fullTop, targetTopSp, bold = true, letterSpacingEm = 0.06f) <= topAvailW ->
                fullTop to targetTopSp
            measureWidthDp(ctx, fullTop, fallbackTopSp, bold = true, letterSpacingEm = 0.06f) <= topAvailW ->
                fullTop to fallbackTopSp
            else ->
                prayerNameUpper to fitSp(ctx, prayerNameUpper, targetTopSp, topAvailW, bold = true, letterSpacingEm = 0.06f)
        }
        v.setTextViewText(R.id.next_label, topText)
        setSp(v, R.id.next_label, topSp)

        countdown(v, R.id.countdown, next, now, "%s")
        val cdSample = chronoSampleFor(now, next.at)
        setSp(v, R.id.countdown, fitSp(ctx, cdSample, sz(40f), innerW, bold = true, minSp = 18f))

        val untilText = ctx.getString(R.string.until_adhan)
        v.setTextViewText(R.id.until_label, untilText)
        setSp(v, R.id.until_label, fitSp(ctx, untilText, sz(13.5f), innerW, bold = true, minSp = 9.5f))
        v.setViewVisibility(R.id.until_label, if (h < 135f) View.GONE else View.VISIBLE)

        val panelPadV = (9f * s).coerceAtLeast(6f)
        val panelPadH = (8f * s).coerceAtLeast(6f)
        v.setViewPadding(
            R.id.small_panel,
            dpToPx(ctx, panelPadH),
            dpToPx(ctx, panelPadV),
            dpToPx(ctx, panelPadH),
            dpToPx(ctx, panelPadV)
        )
        val panelInnerW = (innerW - 2f * panelPadH).coerceAtLeast(64f)

        val adhanLbl = ctx.getString(R.string.adhan)
        val namazLbl = ctx.getString(R.string.namaz)
        val adhanStr = DateText.time(ctx, next.slot.adhan, loc, true)
        val namazStr = next.slot.namaz?.let { DateText.time(ctx, it, loc, true) } ?: DASH

        var lblSp = sz(11.5f)
        var timeSp = sz(14.5f)
        while (timeSp > 9.5f) {
            val row1W = measureWidthDp(ctx, adhanLbl, lblSp, bold = true) +
                measureWidthDp(ctx, adhanStr, timeSp, bold = true) + 4f
            val row2W = measureWidthDp(ctx, namazLbl, lblSp, bold = true) +
                measureWidthDp(ctx, namazStr, timeSp, bold = true) + 4f
            if (maxOf(row1W, row2W) <= panelInnerW) break
            timeSp -= 0.4f
            lblSp = (lblSp - 0.3f).coerceAtLeast(8.5f)
        }

        v.setTextViewText(R.id.adhan_label, adhanLbl)
        v.setTextViewText(R.id.namaz_label, namazLbl)
        v.setTextViewText(R.id.adhan_time, adhanStr)
        v.setTextViewText(R.id.namaz_time, namazStr)
        setSp(v, R.id.adhan_label, lblSp)
        setSp(v, R.id.namaz_label, lblSp)
        setSp(v, R.id.adhan_time, timeSp)
        setSp(v, R.id.namaz_time, timeSp)

        val shortDate = DateText.gregorian(now.toLocalDate(), loc, false)
        v.setTextViewText(R.id.date_en, shortDate)
        setSp(v, R.id.date_en, fitSp(ctx, shortDate, sz(12.5f), innerW, bold = true, minSp = 9.5f))
    }

    private fun medium(
        v: RemoteViews,
        ctx: Context,
        loc: Locale,
        next: NextInfo,
        now: LocalDateTime,
        widthDp: Int,
        heightDp: Int
    ) {
        // Default wide grid size on launcher is ~480x221dp
        val w = (if (widthDp > 0) widthDp else 480).toFloat()
        val h = (if (heightDp > 0) heightDp else 221).toFloat()
        val s = min(w / 480f, h / 221f).coerceIn(0.7f, 1.8f)
        val fd = fontDamp(ctx)
        fun sz(baseSp: Float): Float = (baseSp * s) / fd

        val pad = (13f * s).coerceAtLeast(9f)
        v.setViewPadding(
            R.id.content_box,
            dpToPx(ctx, pad),
            dpToPx(ctx, pad),
            dpToPx(ctx, pad),
            dpToPx(ctx, pad)
        )

        val sunSize = (26f * s).coerceAtLeast(18f)
        val sunTop = (13f * s).coerceAtLeast(9f)
        val sunEnd = (14f * s).coerceAtLeast(9f)
        val sectionGap = (9f * s).coerceAtLeast(6f)
        val cardHalfGap = (3f * s).coerceAtLeast(2f)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            v.setViewLayoutWidth(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutHeight(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_TOP, sunTop, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_END, sunEnd, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.header_block, RemoteViews.MARGIN_END, sunSize + 6f, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.date_combined, RemoteViews.MARGIN_TOP, (3f * s).coerceAtLeast(2f), TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.cards_row, RemoteViews.MARGIN_TOP, sectionGap, TypedValue.COMPLEX_UNIT_DIP)
        }

        val innerW = (w - 2f * pad).coerceAtLeast(220f)
        val headerAvailW = (innerW - sunSize - 6f).coerceAtLeast(160f)

        val nextPrayerName = ctx.getString(nameRes(next.slot.prayer))
        val headlinePrefix = ctx.getString(R.string.headline_in, nextPrayerName)
        val cdFormat = "$headlinePrefix %s"
        countdown(v, R.id.countdown, next, now, cdFormat)
        val headlineSample = "$headlinePrefix ${chronoSampleFor(now, next.at)}"
        setSp(v, R.id.countdown, fitSp(ctx, headlineSample, sz(29f), headerAvailW, bold = true, minSp = 15f))

        val date = now.toLocalDate()
        val combinedFull = "${DateText.gregorian(date, loc, true)} · ${DateText.hijri(ctx, date, loc, false)}"
        val combinedShort = "${DateText.gregorian(date, loc, false)} · ${DateText.hijri(ctx, date, loc, true)}"
        val targetDateSp = sz(14f)
        val (dateLine, dateSp) = if (measureWidthDp(ctx, combinedFull, targetDateSp, bold = true) <= headerAvailW) {
            combinedFull to targetDateSp
        } else if (measureWidthDp(ctx, combinedFull, sz(11.5f), bold = true) <= headerAvailW) {
            combinedFull to fitSp(ctx, combinedFull, targetDateSp, headerAvailW, bold = true, minSp = 10f)
        } else {
            combinedShort to fitSp(ctx, combinedShort, targetDateSp, headerAvailW, bold = true, minSp = 10f)
        }
        v.setTextViewText(R.id.date_combined, dateLine)
        setSp(v, R.id.date_combined, dateSp)

        val five = next.today.fivePrayers
        val cardPad = (3f * s).coerceAtLeast(2f)
        val cardInnerW = ((innerW - 8f * cardHalfGap) / 5f - 2f * cardPad - 1f).coerceAtLeast(42f)
        val namazWord = ctx.getString(R.string.namaz)

        val names = five.map { ctx.getString(nameRes(it.prayer)) }
        val adhans = five.map { DateText.time(ctx, it.adhan, loc, false) }
        val namazs = five.map { slot ->
            val t = slot.namaz?.let { DateText.time(ctx, it, loc, false) } ?: DASH
            "$namazWord $t"
        }

        val uniformNameSp = names.minOf { fitSp(ctx, it, sz(15f), cardInnerW, bold = true, minSp = 10f) }
        val uniformAdhanSp = adhans.minOf { fitSp(ctx, it, sz(24.5f), cardInnerW, bold = true, minSp = 13f) }
        val uniformNamazSp = namazs.minOf { fitSp(ctx, it, sz(12.5f), cardInnerW, bold = true, minSp = 9f) }

        five.forEachIndexed { i, slot ->
            val c = CELLS[i]
            val hi = slot.prayer == next.slot.prayer
            v.setInt(c[0], "setBackgroundResource", if (hi) R.drawable.bg_next else R.drawable.bg_glass_cell)
            v.setViewPadding(
                c[0],
                dpToPx(ctx, cardPad),
                dpToPx(ctx, cardPad),
                dpToPx(ctx, cardPad),
                dpToPx(ctx, cardPad)
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                v.setViewLayoutMargin(c[0], RemoteViews.MARGIN_START, if (i == 0) 0f else cardHalfGap, TypedValue.COMPLEX_UNIT_DIP)
                v.setViewLayoutMargin(c[0], RemoteViews.MARGIN_END, if (i == 4) 0f else cardHalfGap, TypedValue.COMPLEX_UNIT_DIP)
                val lineGap = (5f * s).coerceAtLeast(2f)
                v.setViewLayoutMargin(c[2], RemoteViews.MARGIN_TOP, lineGap, TypedValue.COMPLEX_UNIT_DIP)
                v.setViewLayoutMargin(c[2], RemoteViews.MARGIN_BOTTOM, lineGap, TypedValue.COMPLEX_UNIT_DIP)
            }

            v.setTextViewText(c[1], names[i])
            v.setTextViewText(c[2], adhans[i])
            v.setTextViewText(c[3], namazs[i])

            setSp(v, c[1], uniformNameSp)
            setSp(v, c[2], uniformAdhanSp)
            setSp(v, c[3], uniformNamazSp)

            v.setTextColor(c[1], if (hi) HIGHLIGHT_TEXT else WHITE)
            v.setTextColor(c[2], if (hi) HIGHLIGHT_TEXT else WHITE)
            v.setTextColor(c[3], if (hi) HIGHLIGHT_TEXT else WHITE_95)
        }
    }

    private fun large(
        v: RemoteViews,
        ctx: Context,
        loc: Locale,
        next: NextInfo,
        now: LocalDateTime,
        widthDp: Int,
        heightDp: Int
    ) {
        // Default 4x4 grid size on launcher is ~267x458dp
        val w = (if (widthDp > 0) widthDp else 267).toFloat()
        val h = (if (heightDp > 0) heightDp else 458).toFloat()
        val s = min(w / 267f, h / 458f).coerceIn(0.7f, 1.8f)
        val fd = fontDamp(ctx)
        fun sz(baseSp: Float): Float = (baseSp * s) / fd

        val padTop = (14f * s).coerceAtLeast(10f)
        val padSide = (12f * s).coerceAtLeast(9f)
        val padBot = (12f * s).coerceAtLeast(9f)
        v.setViewPadding(
            R.id.content_box,
            dpToPx(ctx, padSide),
            dpToPx(ctx, padTop),
            dpToPx(ctx, padSide),
            dpToPx(ctx, padBot)
        )

        val sunSize = (28f * s).coerceAtLeast(20f)
        val sunMargin = (14f * s).coerceAtLeast(10f)
        val sectionGap = (10f * s).coerceAtLeast(6f)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            v.setViewLayoutWidth(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutHeight(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_TOP, sunMargin, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_END, sunMargin, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.header_block, RemoteViews.MARGIN_END, sunSize + 6f, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.date_hijri, RemoteViews.MARGIN_TOP, (2f * s).coerceAtLeast(1f), TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.next_block, RemoteViews.MARGIN_TOP, sectionGap, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.next_block, RemoteViews.MARGIN_END, sunSize + 6f, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.progress, RemoteViews.MARGIN_TOP, sectionGap, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutHeight(R.id.progress, (5f * s).coerceAtLeast(3f), TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.table_box, RemoteViews.MARGIN_TOP, sectionGap, TypedValue.COMPLEX_UNIT_DIP)
        }

        val innerW = (w - 2f * padSide).coerceAtLeast(160f)
        val headerAvailW = (innerW - sunSize - 6f).coerceAtLeast(110f)

        val date = now.toLocalDate()
        val gregStr = DateText.gregorian(date, loc, true)
        val hijriStr = DateText.hijri(ctx, date, loc, false)
        v.setTextViewText(R.id.date_en, gregStr)
        v.setTextViewText(R.id.date_hijri, hijriStr)
        setSp(v, R.id.date_en, fitSp(ctx, gregStr, sz(16f), headerAvailW, bold = true, minSp = 11f))
        setSp(v, R.id.date_hijri, fitSp(ctx, hijriStr, sz(13f), headerAvailW, bold = true, minSp = 9.5f))

        val nextTitle = ctx.getString(R.string.next_prayer_title).uppercase(loc)
        val nextName = ctx.getString(nameRes(next.slot.prayer))
        val inPrefix = ctx.getString(R.string.in_label)
        v.setTextViewText(R.id.next_title, nextTitle)
        v.setTextViewText(R.id.next_name, nextName)
        setSp(v, R.id.next_title, fitSp(ctx, nextTitle, sz(11.5f), headerAvailW, bold = true, letterSpacingEm = 0.10f, minSp = 9f))
        setSp(v, R.id.next_name, fitSp(ctx, nextName, sz(42f), headerAvailW, bold = true, minSp = 22f))

        countdown(v, R.id.countdown, next, now, "$inPrefix %s")
        val cdSample = "$inPrefix ${chronoSampleFor(now, next.at)}"
        setSp(v, R.id.countdown, fitSp(ctx, cdSample, sz(20f), headerAvailW, bold = true, minSp = 13f))

        val total = Duration.between(next.previousAt, next.at).toMillis().coerceAtLeast(1L)
        val elapsed = Duration.between(next.previousAt, now).toMillis().coerceIn(0L, total)
        v.setProgressBar(R.id.progress, 1000, (elapsed * 1000 / total).toInt(), false)
        v.setViewVisibility(R.id.progress, if (h < 240f) View.GONE else View.VISIBLE)

        val hideSunrise = h < 340f && next.slot.prayer != Prayer.SUNRISE
        val hideNamazCol = (h < 290f || w < 200f)

        val tablePad = (4f * s).coerceAtLeast(3f)
        val rowPadH = (10f * s).coerceAtLeast(6f)
        val hdrPadTop = (4f * s).coerceAtLeast(2f)
        val hdrPadBot = (5f * s).coerceAtLeast(3f)

        v.setViewPadding(
            R.id.table_box,
            dpToPx(ctx, tablePad),
            dpToPx(ctx, tablePad),
            dpToPx(ctx, tablePad),
            dpToPx(ctx, tablePad)
        )
        v.setViewPadding(
            R.id.table_header,
            dpToPx(ctx, rowPadH),
            dpToPx(ctx, hdrPadTop),
            dpToPx(ctx, rowPadH),
            dpToPx(ctx, hdrPadBot)
        )

        val colPrayerTxt = ctx.getString(R.string.col_prayer).uppercase(loc)
        val colAdhanTxt = ctx.getString(R.string.adhan).uppercase(loc)
        val colNamazTxt = ctx.getString(R.string.namaz).uppercase(loc)
        v.setTextViewText(R.id.h_prayer, colPrayerTxt)
        v.setTextViewText(R.id.h_adhan, colAdhanTxt)
        v.setTextViewText(R.id.h_namaz, colNamazTxt)
        v.setViewVisibility(R.id.h_namaz, if (hideNamazCol) View.GONE else View.VISIBLE)

        val tableInnerW = (innerW - 2f * tablePad - 2f * rowPadH).coerceAtLeast(120f)
        val totalWeight = if (hideNamazCol) 2.15f else 3.15f
        val col1W = (tableInnerW * (1.15f / totalWeight) - 3f).coerceAtLeast(44f)
        val col2W = (tableInnerW * (1.0f / totalWeight) - 3f).coerceAtLeast(40f)
        val col3W = (tableInnerW * (1.0f / totalWeight) - 3f).coerceAtLeast(40f)

        val hdrSp = minOf(
            fitSp(ctx, colPrayerTxt, sz(11f), col1W, bold = true, letterSpacingEm = 0.08f, minSp = 8.5f),
            fitSp(ctx, colAdhanTxt, sz(11f), col2W, bold = true, letterSpacingEm = 0.08f, minSp = 8.5f),
            if (hideNamazCol) 99f else fitSp(ctx, colNamazTxt, sz(11f), col3W, bold = true, letterSpacingEm = 0.08f, minSp = 8.5f)
        )
        setSp(v, R.id.h_prayer, hdrSp)
        setSp(v, R.id.h_adhan, hdrSp)
        setSp(v, R.id.h_namaz, hdrSp)

        val slots = next.today.slots
        val rowNames = slots.map { ctx.getString(nameRes(it.prayer)) }
        val rowAdhans = slots.map { DateText.time(ctx, it.adhan, loc, true) }
        val rowNamazs = slots.map { slot -> slot.namaz?.let { DateText.time(ctx, it, loc, true) } ?: DASH }

        val targetRowSp = sz(16.5f)
        var uniformRowSp = targetRowSp
        slots.indices.forEach { i ->
            if (i == 1 && hideSunrise) return@forEach
            uniformRowSp = minOf(
                uniformRowSp,
                fitSp(ctx, rowNames[i], uniformRowSp, col1W, bold = true, minSp = 11f),
                fitSp(ctx, rowAdhans[i], uniformRowSp, col2W, bold = true, minSp = 11f),
                if (hideNamazCol) 99f else fitSp(ctx, rowNamazs[i], uniformRowSp, col3W, bold = true, minSp = 11f)
            )
        }

        slots.forEachIndexed { i, slot ->
            val r = ROWS[i]
            if (i == 1 && hideSunrise) {
                v.setViewVisibility(r[0], View.GONE)
                return@forEachIndexed
            }
            v.setViewVisibility(r[0], View.VISIBLE)
            val hi = slot.prayer == next.slot.prayer
            v.setInt(r[0], "setBackgroundResource", if (hi) R.drawable.bg_next_row else R.drawable.bg_row)
            v.setViewPadding(r[0], dpToPx(ctx, rowPadH), 0, dpToPx(ctx, rowPadH), 0)

            v.setTextViewText(r[1], rowNames[i])
            v.setTextViewText(r[2], rowAdhans[i])
            v.setTextViewText(r[3], rowNamazs[i])
            v.setViewVisibility(r[3], if (hideNamazCol) View.GONE else View.VISIBLE)

            for (k in 1..3) {
                setSp(v, r[k], uniformRowSp)
                v.setTextColor(r[k], if (hi) HIGHLIGHT_TEXT else WHITE)
            }
        }
    }
}
