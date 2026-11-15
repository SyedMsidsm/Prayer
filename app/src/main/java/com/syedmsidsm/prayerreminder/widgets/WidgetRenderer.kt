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
    private val WHITE_80 = 0xCCFFFFFF.toInt()
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

        // Shared design token: keep the warm dusk gradient and radial sun glow across themes
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

    /** Live countdown that ticks by itself (no battery-hungry refresh every second). */
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
        minSp: Float = 6.5f
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
        // Reference small-widget.html is 200x360
        val w = (if (widthDp > 0) widthDp else 125).toFloat()
        val h = (if (heightDp > 0) heightDp else 221).toFloat()
        val s = min(w / 200f, h / 360f).coerceIn(0.48f, 1.35f)
        val fd = fontDamp(ctx)
        fun refSp(px: Float): Float = (px * s) / fd

        val padTop = (24f * s).coerceAtLeast(8f)
        val padSide = (20f * s).coerceAtLeast(8f)
        val padBot = (20f * s).coerceAtLeast(8f)
        v.setViewPadding(
            R.id.content_box,
            dpToPx(ctx, padSide),
            dpToPx(ctx, padTop),
            dpToPx(ctx, padSide),
            dpToPx(ctx, padBot)
        )

        val sunSize = (30f * s).coerceAtLeast(14f)
        val sunMargin = (18f * s).coerceAtLeast(8f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            v.setViewLayoutWidth(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutHeight(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_TOP, sunMargin, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_END, sunMargin, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.next_label, RemoteViews.MARGIN_END, sunSize + 4f, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.until_label, RemoteViews.MARGIN_TOP, (6f * s).coerceAtLeast(2f), TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.small_namaz_row, RemoteViews.MARGIN_TOP, (10f * s).coerceAtLeast(3f), TypedValue.COMPLEX_UNIT_DIP)
        }

        val innerW = (w - 2f * padSide).coerceAtLeast(60f)
        val topAvailW = (innerW - sunSize - 4f).coerceAtLeast(45f)

        // Top: "NEXT · MAGHRIB" 13sp bold, letter-spacing 1.5, 80% white.
        // Must never truncate; if too long, shrink to 12sp and drop "NEXT ·".
        val prayerNameUpper = ctx.getString(nameRes(next.slot.prayer)).uppercase(loc)
        val fullTop = ctx.getString(R.string.next_label, prayerNameUpper).uppercase(loc)
        val sp13 = refSp(13f)
        val sp12 = refSp(12f)
        val (topText, topSp) = when {
            measureWidthDp(ctx, fullTop, sp13, bold = true, letterSpacingEm = 0.11f) <= topAvailW ->
                fullTop to sp13
            measureWidthDp(ctx, fullTop, sp12, bold = true, letterSpacingEm = 0.11f) <= topAvailW ->
                fullTop to sp12
            else ->
                prayerNameUpper to fitSp(ctx, prayerNameUpper, sp12, topAvailW, bold = true, letterSpacingEm = 0.11f)
        }
        v.setTextViewText(R.id.next_label, topText)
        setSp(v, R.id.next_label, topSp)

        // Middle: countdown "00:11" 52sp extra-bold, "until adhan" 17sp below.
        countdown(v, R.id.countdown, next, now, "%s")
        val cdSample = chronoSampleFor(now, next.at)
        setSp(v, R.id.countdown, fitSp(ctx, cdSample, refSp(52f), innerW, bold = true, minSp = 14f))

        val untilText = ctx.getString(R.string.until_adhan)
        v.setTextViewText(R.id.until_label, untilText)
        setSp(v, R.id.until_label, fitSp(ctx, untilText, refSp(17f), innerW, bold = false, minSp = 8f))
        v.setViewVisibility(R.id.until_label, if (h < 135f) View.GONE else View.VISIBLE)

        // Panel (radius 20, padding 14/16): two rows, "Adhan 6:04 PM" and "Namaz 6:19 PM"
        val panelPadV = (14f * s).coerceAtLeast(5f)
        val panelPadH = (16f * s).coerceAtLeast(6f)
        v.setViewPadding(
            R.id.small_panel,
            dpToPx(ctx, panelPadH),
            dpToPx(ctx, panelPadV),
            dpToPx(ctx, panelPadH),
            dpToPx(ctx, panelPadV)
        )
        val panelInnerW = (innerW - 2f * panelPadH).coerceAtLeast(48f)

        val adhanLbl = ctx.getString(R.string.adhan)
        val namazLbl = ctx.getString(R.string.namaz)
        val adhanStr = DateText.time(ctx, next.slot.adhan, loc, true)
        val namazStr = next.slot.namaz?.let { DateText.time(ctx, it, loc, true) } ?: DASH

        var lblSp = refSp(14f)
        var timeSp = refSp(19f)
        while (timeSp > 8f) {
            val row1W = measureWidthDp(ctx, adhanLbl, lblSp, bold = false) +
                measureWidthDp(ctx, adhanStr, timeSp, bold = true) + 6f
            val row2W = measureWidthDp(ctx, namazLbl, lblSp, bold = false) +
                measureWidthDp(ctx, namazStr, timeSp, bold = true) + 6f
            if (maxOf(row1W, row2W) <= panelInnerW) break
            timeSp -= 0.4f
            lblSp = (lblSp - 0.3f).coerceAtLeast(6.5f)
        }

        v.setTextViewText(R.id.adhan_label, adhanLbl)
        v.setTextViewText(R.id.namaz_label, namazLbl)
        v.setTextViewText(R.id.adhan_time, adhanStr)
        v.setTextViewText(R.id.namaz_time, namazStr)
        setSp(v, R.id.adhan_label, lblSp)
        setSp(v, R.id.namaz_label, lblSp)
        setSp(v, R.id.adhan_time, timeSp)
        setSp(v, R.id.namaz_time, timeSp)

        // Bottom: short date only, "Sun, 15 Nov", 14sp. Do NOT show the Hijri date here.
        val shortDate = DateText.gregorian(now.toLocalDate(), loc, false)
        v.setTextViewText(R.id.date_en, shortDate)
        setSp(v, R.id.date_en, fitSp(ctx, shortDate, refSp(14f), innerW, bold = false, minSp = 7.5f))
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
        // Reference wide-widget.html is 720x270
        val w = (if (widthDp > 0) widthDp else 480).toFloat()
        val h = (if (heightDp > 0) heightDp else 221).toFloat()
        val s = min(w / 720f, h / 270f).coerceIn(0.45f, 1.35f)
        val fd = fontDamp(ctx)
        fun refSp(px: Float): Float = (px * s) / fd

        val pad = (24f * s).coerceAtLeast(10f)
        v.setViewPadding(
            R.id.content_box,
            dpToPx(ctx, pad),
            dpToPx(ctx, pad),
            dpToPx(ctx, pad),
            dpToPx(ctx, pad)
        )

        val sunSize = (36f * s).coerceAtLeast(18f)
        val sunTop = (22f * s).coerceAtLeast(10f)
        val sunEnd = (24f * s).coerceAtLeast(10f)
        val sectionGap = (18f * s).coerceAtLeast(6f)
        val cardHalfGap = (5f * s).coerceAtLeast(2f)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            v.setViewLayoutWidth(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutHeight(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_TOP, sunTop, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_END, sunEnd, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.header_block, RemoteViews.MARGIN_END, sunSize + 8f, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.date_combined, RemoteViews.MARGIN_TOP, (6f * s).coerceAtLeast(2f), TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.cards_row, RemoteViews.MARGIN_TOP, sectionGap, TypedValue.COMPLEX_UNIT_DIP)
        }

        val innerW = (w - 2f * pad).coerceAtLeast(200f)
        val headerAvailW = (innerW - sunSize - 8f).coerceAtLeast(140f)

        // Top-left: "Maghrib in 00:11" 34sp extra-bold
        val nextPrayerName = ctx.getString(nameRes(next.slot.prayer))
        val headlinePrefix = ctx.getString(R.string.headline_in, nextPrayerName)
        val cdFormat = "$headlinePrefix %s"
        countdown(v, R.id.countdown, next, now, cdFormat)
        val headlineSample = "$headlinePrefix ${chronoSampleFor(now, next.at)}"
        setSp(v, R.id.countdown, fitSp(ctx, headlineSample, refSp(34f), headerAvailW, bold = true, minSp = 12f))

        // Under it, one line at 15sp: "Sunday, 15 Nov 2026 · 5 Jumada al-Akhirah 1448"
        val date = now.toLocalDate()
        val combinedFull = "${DateText.gregorian(date, loc, true)} · ${DateText.hijri(ctx, date, loc, false)}"
        val combinedShort = "${DateText.gregorian(date, loc, false)} · ${DateText.hijri(ctx, date, loc, true)}"
        val targetDateSp = refSp(15f)
        val (dateLine, dateSp) = if (measureWidthDp(ctx, combinedFull, targetDateSp, bold = false) <= headerAvailW) {
            combinedFull to targetDateSp
        } else if (measureWidthDp(ctx, combinedFull, refSp(13.5f), bold = false) <= headerAvailW) {
            combinedFull to fitSp(ctx, combinedFull, targetDateSp, headerAvailW, bold = false, minSp = 8.5f)
        } else {
            combinedShort to fitSp(ctx, combinedShort, targetDateSp, headerAvailW, bold = false, minSp = 8.5f)
        }
        v.setTextViewText(R.id.date_combined, dateLine)
        setSp(v, R.id.date_combined, dateSp)

        // Below: a row of 5 equal cards (Fajr, Dhuhr, Asr, Maghrib, Isha), gap 10, radius 20
        val five = next.today.fivePrayers
        val cardPad = (4f * s).coerceAtLeast(2f)
        val cardInnerW = ((innerW - 8f * cardHalfGap) / 5f - 2f * cardPad - 2f).coerceAtLeast(36f)
        val namazWord = ctx.getString(R.string.namaz)

        val names = five.map { ctx.getString(nameRes(it.prayer)) }
        val adhans = five.map { DateText.time(ctx, it.adhan, loc, false) }
        val namazs = five.map { slot ->
            val t = slot.namaz?.let { DateText.time(ctx, it, loc, false) } ?: DASH
            "$namazWord $t"
        }

        val uniformNameSp = names.minOf { fitSp(ctx, it, refSp(17f), cardInnerW, bold = true, minSp = 8f) }
        val uniformAdhanSp = adhans.minOf { fitSp(ctx, it, refSp(27f), cardInnerW, bold = true, minSp = 11f) }
        val uniformNamazSp = namazs.minOf { fitSp(ctx, it, refSp(15f), cardInnerW, bold = false, minSp = 7f) }

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
                val lineGap = (6f * s).coerceAtLeast(2f)
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
            v.setTextColor(c[3], if (hi) HIGHLIGHT_TEXT else WHITE_80)
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
        // Reference large-widget.html is 360x700
        val w = (if (widthDp > 0) widthDp else 267).toFloat()
        val h = (if (heightDp > 0) heightDp else 458).toFloat()
        val s = min(w / 360f, h / 700f).coerceIn(0.45f, 1.35f)
        val fd = fontDamp(ctx)
        fun refSp(px: Float): Float = (px * s) / fd

        val padTop = (28f * s).coerceAtLeast(10f)
        val padSide = (24f * s).coerceAtLeast(10f)
        val padBot = (24f * s).coerceAtLeast(10f)
        v.setViewPadding(
            R.id.content_box,
            dpToPx(ctx, padSide),
            dpToPx(ctx, padTop),
            dpToPx(ctx, padSide),
            dpToPx(ctx, padBot)
        )

        val sunSize = (40f * s).coerceAtLeast(20f)
        val sunMargin = (26f * s).coerceAtLeast(10f)
        val sectionGap = (22f * s).coerceAtLeast(8f)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            v.setViewLayoutWidth(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutHeight(R.id.sky_body, sunSize, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_TOP, sunMargin, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.sky_body, RemoteViews.MARGIN_END, sunMargin, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.header_block, RemoteViews.MARGIN_END, sunSize + 8f, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.date_hijri, RemoteViews.MARGIN_TOP, (4f * s).coerceAtLeast(2f), TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.next_block, RemoteViews.MARGIN_TOP, sectionGap, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.next_block, RemoteViews.MARGIN_END, sunSize + 8f, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.progress, RemoteViews.MARGIN_TOP, sectionGap, TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutHeight(R.id.progress, (6f * s).coerceAtLeast(3f), TypedValue.COMPLEX_UNIT_DIP)
            v.setViewLayoutMargin(R.id.table_box, RemoteViews.MARGIN_TOP, sectionGap, TypedValue.COMPLEX_UNIT_DIP)
        }

        val innerW = (w - 2f * padSide).coerceAtLeast(150f)
        val headerAvailW = (innerW - sunSize - 8f).coerceAtLeast(100f)

        // Header: "Sunday, 15 Nov 2026" 18sp bold; Hijri date below at 14sp, 80% white.
        val date = now.toLocalDate()
        val gregStr = DateText.gregorian(date, loc, true)
        val hijriStr = DateText.hijri(ctx, date, loc, false)
        v.setTextViewText(R.id.date_en, gregStr)
        v.setTextViewText(R.id.date_hijri, hijriStr)
        setSp(v, R.id.date_en, fitSp(ctx, gregStr, refSp(18f), headerAvailW, bold = true, minSp = 9f))
        setSp(v, R.id.date_hijri, fitSp(ctx, hijriStr, refSp(14f), headerAvailW, bold = false, minSp = 8f))

        // Next prayer block: label "NEXT PRAYER" 13sp, letter-spacing 2; prayer name 52sp extra-bold; "in 00:11" 24sp semibold.
        val nextTitle = ctx.getString(R.string.next_prayer_title).uppercase(loc)
        val nextName = ctx.getString(nameRes(next.slot.prayer))
        val inPrefix = ctx.getString(R.string.in_label)
        v.setTextViewText(R.id.next_title, nextTitle)
        v.setTextViewText(R.id.next_name, nextName)
        setSp(v, R.id.next_title, fitSp(ctx, nextTitle, refSp(13f), headerAvailW, bold = true, letterSpacingEm = 0.14f, minSp = 7.5f))
        setSp(v, R.id.next_name, fitSp(ctx, nextName, refSp(52f), headerAvailW, bold = true, minSp = 18f))

        countdown(v, R.id.countdown, next, now, "$inPrefix %s")
        val cdSample = "$inPrefix ${chronoSampleFor(now, next.at)}"
        setSp(v, R.id.countdown, fitSp(ctx, cdSample, refSp(24f), headerAvailW, bold = false, minSp = 11f))

        // Progress bar: 6dp high, 22% white track, solid white fill
        val total = Duration.between(next.previousAt, next.at).toMillis().coerceAtLeast(1L)
        val elapsed = Duration.between(next.previousAt, now).toMillis().coerceIn(0L, total)
        v.setProgressBar(R.id.progress, 1000, (elapsed * 1000 / total).toInt(), false)
        v.setViewVisibility(R.id.progress, if (h < 240f) View.GONE else View.VISIBLE)

        // Responsive adaptation on short sizes:
        // "On short sizes, hide the Sunrise row first, then the Namaz column, before shrinking text below 18sp."
        val hideSunrise = h < 340f && next.slot.prayer != Prayer.SUNRISE
        val hideNamazCol = (h < 290f || w < 200f)

        val tablePad = (6f * s).coerceAtLeast(3f)
        val rowPadH = (18f * s).coerceAtLeast(8f)
        val hdrPadTop = (6f * s).coerceAtLeast(2f)
        val hdrPadBot = (8f * s).coerceAtLeast(3f)

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

        val tableInnerW = (innerW - 2f * tablePad - 2f * rowPadH).coerceAtLeast(110f)
        val totalWeight = if (hideNamazCol) 2.2f else 3.2f
        val col1W = (tableInnerW * (1.2f / totalWeight) - 4f).coerceAtLeast(40f)
        val col2W = (tableInnerW * (1.0f / totalWeight) - 4f).coerceAtLeast(36f)
        val col3W = (tableInnerW * (1.0f / totalWeight) - 4f).coerceAtLeast(36f)

        val hdrSp = minOf(
            fitSp(ctx, colPrayerTxt, refSp(12f), col1W, bold = true, letterSpacingEm = 0.12f, minSp = 7f),
            fitSp(ctx, colAdhanTxt, refSp(12f), col2W, bold = true, letterSpacingEm = 0.12f, minSp = 7f),
            if (hideNamazCol) 99f else fitSp(ctx, colNamazTxt, refSp(12f), col3W, bold = true, letterSpacingEm = 0.12f, minSp = 7f)
        )
        setSp(v, R.id.h_prayer, hdrSp)
        setSp(v, R.id.h_adhan, hdrSp)
        setSp(v, R.id.h_namaz, hdrSp)

        val slots = next.today.slots
        val rowNames = slots.map { ctx.getString(nameRes(it.prayer)) }
        val rowAdhans = slots.map { DateText.time(ctx, it.adhan, loc, true) }
        val rowNamazs = slots.map { slot -> slot.namaz?.let { DateText.time(ctx, it, loc, true) } ?: DASH }

        val targetRowSp = refSp(21f)
        val minFloorSp = refSp(18f).coerceAtMost(targetRowSp)
        var uniformRowSp = targetRowSp
        slots.indices.forEach { i ->
            if (i == 1 && hideSunrise) return@forEach
            uniformRowSp = minOf(
                uniformRowSp,
                fitSp(ctx, rowNames[i], targetRowSp, col1W, bold = true, minSp = minFloorSp),
                fitSp(ctx, rowAdhans[i], targetRowSp, col2W, bold = false, minSp = minFloorSp),
                if (hideNamazCol) 99f else fitSp(ctx, rowNamazs[i], targetRowSp, col3W, bold = false, minSp = minFloorSp)
            )
        }
        // Final safety pass in case 1.3x font scale needs slightly below minFloorSp to avoid clipping
        slots.indices.forEach { i ->
            if (i == 1 && hideSunrise) return@forEach
            uniformRowSp = minOf(
                uniformRowSp,
                fitSp(ctx, rowNames[i], uniformRowSp, col1W, bold = true, minSp = 8.5f),
                fitSp(ctx, rowAdhans[i], uniformRowSp, col2W, bold = false, minSp = 8.5f),
                if (hideNamazCol) 99f else fitSp(ctx, rowNamazs[i], uniformRowSp, col3W, bold = false, minSp = 8.5f)
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
