package com.syedmsidsm.prayerreminder.widgets

import android.content.Context
import com.syedmsidsm.prayerreminder.R
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.chrono.HijrahChronology
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateText {
    private fun localizeDigits(text: String, loc: Locale): String {
        if (loc.language != "ar") return text
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val sb = StringBuilder(text.length)
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(arabicDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun time(ctx: Context, t: LocalTime, loc: Locale, withAmPm: Boolean): String {
        val pattern = when {
            WidgetLocale.is24Hour(ctx) -> "HH:mm"
            withAmPm -> "h:mm a"
            else -> "h:mm"
        }
        val formatted = t.format(DateTimeFormatter.ofPattern(pattern, loc)).replace('\u202f', ' ')
        return localizeDigits(formatted, loc)
    }

    fun gregorian(d: LocalDate, loc: Locale, long: Boolean): String {
        val formatted = d.format(DateTimeFormatter.ofPattern(if (long) "EEEE, d MMM yyyy" else "EEE, d MMM", loc))
        return localizeDigits(formatted, loc)
    }

    fun hijri(ctx: Context, d: LocalDate, loc: Locale, short: Boolean): String {
        val months = ctx.resources.getStringArray(
            if (short) R.array.w_hijri_months_short else R.array.w_hijri_months
        )
        val nf = NumberFormat.getInstance(loc).apply { isGroupingUsed = false }

        val resolvedHijri = runCatching { PrayerDataSource.hijriFor(ctx, d) }.getOrNull()
        val (day, monthIdx, year) = if (resolvedHijri != null && resolvedHijri.month in 1..12) {
            Triple(resolvedHijri.day, resolvedHijri.month - 1, resolvedHijri.year)
        } else {
            val h = HijrahChronology.INSTANCE.date(d)
                .plus(WidgetConfig.HIJRI_OFFSET_DAYS.toLong(), ChronoUnit.DAYS)
            Triple(
                h.get(ChronoField.DAY_OF_MONTH),
                (h.get(ChronoField.MONTH_OF_YEAR) - 1).coerceIn(0, 11),
                h.get(ChronoField.YEAR)
            )
        }

        val raw = "${nf.format(day)} ${months[monthIdx]} ${nf.format(year)}"
        return localizeDigits(raw, loc)
    }
}
