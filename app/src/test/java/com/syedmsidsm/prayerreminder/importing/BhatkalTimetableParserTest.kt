package com.syedmsidsm.prayerreminder.importing

import com.syedmsidsm.prayerreminder.domain.OcrToken
import com.syedmsidsm.prayerreminder.domain.TimetableParser
import com.syedmsidsm.prayerreminder.domain.TimetableValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BhatkalTimetableParserTest {

    private val parser = TimetableParser()
    private val validator = TimetableValidator()

    @Test
    fun testRealUserTimetableParsesWithZeroValidationFlags() {
        // Construct tokens matching media_1794310806690_4777f390.jpg
        val tokens = mutableListOf<OcrToken>()

        // Header Row at Y ~ 20 (height ~ 20)
        // S.No. | Date | Day | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha
        val headerDefs = listOf(
            Pair("S.No.", 50),
            Pair("Date", 160),
            Pair("Day", 290),
            Pair("Fajr", 410),
            Pair("Sunrise", 530),
            Pair("Dhuhr", 645),
            Pair("Asr", 750),
            Pair("Maghrib", 855),
            Pair("Isha", 965)
        )
        for ((text, x) in headerDefs) {
            tokens.add(OcrToken(text, left = x - 30, top = 10, right = x + 30, bottom = 32))
        }

        // 30 Rows of times matching the user's timetable
        val rawTable = listOf(
            listOf("1", "20 Mar", "Fri", "05:21", "06:36", "12:45", "16:01", "18:45", "19:50"),
            listOf("2", "21 Mar", "Sat", "05:20", "06:35", "12:45", "16:01", "18:45", "19:50"),
            listOf("3", "22 Mar", "Sun", "05:20", "06:35", "12:45", "16:00", "18:45", "19:50"),
            listOf("4", "23 Mar", "Mon", "05:19", "06:34", "12:44", "16:00", "18:46", "19:50"),
            listOf("5", "24 Mar", "Tue", "05:18", "06:33", "12:44", "15:59", "18:46", "19:50"),
            listOf("6", "25 Mar", "Wed", "05:17", "06:33", "12:44", "15:59", "18:46", "19:51"),
            listOf("7", "26 Mar", "Thu", "05:17", "06:32", "12:43", "15:58", "18:46", "19:51"),
            listOf("8", "27 Mar", "Fri", "05:16", "06:31", "12:43", "15:57", "18:46", "19:51"),
            listOf("9", "28 Mar", "Sat", "05:15", "06:30", "12:43", "15:57", "18:46", "19:51"),
            listOf("10", "29 Mar", "Sun", "05:14", "06:30", "12:42", "15:56", "18:46", "19:51"),
            listOf("11", "30 Mar", "Mon", "05:14", "06:29", "12:42", "15:56", "18:46", "19:51"),
            listOf("12", "31 Mar", "Tue", "05:13", "06:28", "12:42", "15:55", "18:46", "19:51"),
            listOf("13", "01 Apr", "Wed", "05:12", "06:28", "12:41", "15:55", "18:46", "19:51"),
            listOf("14", "02 Apr", "Thu", "05:11", "06:27", "12:41", "15:54", "18:46", "19:52"),
            listOf("15", "03 Apr", "Fri", "05:11", "06:26", "12:41", "15:54", "18:46", "19:52"),
            listOf("16", "04 Apr", "Sat", "05:10", "06:26", "12:41", "15:53", "18:47", "19:52"),
            listOf("17", "05 Apr", "Sun", "05:09", "06:25", "12:40", "15:52", "18:47", "19:52"),
            listOf("18", "06 Apr", "Mon", "05:08", "06:24", "12:40", "15:52", "18:47", "19:52"),
            listOf("19", "07 Apr", "Tue", "05:08", "06:24", "12:40", "15:51", "18:47", "19:53"),
            listOf("20", "08 Apr", "Wed", "05:07", "06:23", "12:39", "15:50", "18:47", "19:53"),
            listOf("21", "09 Apr", "Thu", "05:06", "06:22", "12:39", "15:50", "18:47", "19:53"),
            listOf("22", "10 Apr", "Fri", "05:05", "06:22", "12:39", "15:49", "18:47", "19:53"),
            listOf("23", "11 Apr", "Sat", "05:05", "06:21", "12:39", "15:48", "18:47", "19:53"),
            listOf("24", "12 Apr", "Sun", "05:04", "06:20", "12:38", "15:48", "18:47", "19:53"),
            listOf("25", "13 Apr", "Mon", "05:03", "06:20", "12:38", "15:47", "18:47", "19:54"),
            listOf("26", "14 Apr", "Tue", "05:02", "06:19", "12:38", "15:47", "18:48", "19:54"),
            listOf("27", "15 Apr", "Wed", "05:02", "06:18", "12:38", "15:46", "18:48", "19:54"),
            listOf("28", "16 Apr", "Thu", "05:01", "06:18", "12:37", "15:45", "18:48", "19:54"),
            listOf("29", "17 Apr", "Fri", "05:00", "06:17", "12:37", "15:44", "18:48", "19:54"),
            listOf("30", "18 Apr", "Sat", "04:59", "06:17", "12:37", "15:44", "18:48", "19:55")
        )

        var rowY = 55
        val rowHeight = 31

        for ((idx, row) in rawTable.withIndex()) {
            val yTop = rowY
            val yBottom = rowY + 20

            // S.No
            tokens.add(OcrToken(row[0], left = 35, top = yTop, right = 65, bottom = yBottom))

            // Date: split into day and month as ML Kit often does
            val dateParts = row[1].split(" ")
            tokens.add(OcrToken(dateParts[0], left = 130, top = yTop, right = 155, bottom = yBottom))
            tokens.add(OcrToken(dateParts[1], left = 160, top = yTop, right = 195, bottom = yBottom))

            // Day
            tokens.add(OcrToken(row[2], left = 270, top = yTop, right = 310, bottom = yBottom))

            // Fajr
            tokens.add(OcrToken(row[3], left = 385, top = yTop, right = 435, bottom = yBottom))

            // Sunrise
            tokens.add(OcrToken(row[4], left = 505, top = yTop, right = 555, bottom = yBottom))

            // Dhuhr
            tokens.add(OcrToken(row[5], left = 620, top = yTop, right = 670, bottom = yBottom))

            // Asr
            tokens.add(OcrToken(row[6], left = 725, top = yTop, right = 775, bottom = yBottom))

            // Maghrib
            tokens.add(OcrToken(row[7], left = 830, top = yTop, right = 880, bottom = yBottom))

            // Isha
            tokens.add(OcrToken(row[8], left = 940, top = yTop, right = 990, bottom = yBottom))

            rowY += rowHeight
        }

        val result = parser.parse(tokens)
        assertNotNull("Result should not be null", result)

        val timetable = result!!.timetable
        assertEquals("Should parse 30 days", 30, timetable.days.size)
        assertEquals("First date must be 2026-03-20", "2026-03-20", timetable.firstDate)
        assertEquals("Last date must be 2026-04-18", "2026-04-18", timetable.lastDate)

        // Verify month rollover at row 13 (Day 13 = 1 April)
        assertEquals("Day 13 date", "2026-04-01", timetable.days[12].date)
        assertEquals("Day 13 weekday", "Wed", timetable.days[12].weekday)
        assertEquals("Day 13 Hijri", 13, timetable.days[12].hijriDay)

        // Verify prayer times for day 1
        val day1 = timetable.days[0]
        assertEquals("05:21", day1.times["fajr"])
        assertEquals("06:36", day1.times["sunrise"])
        assertEquals("12:45", day1.times["dhuhr"])
        assertEquals("16:01", day1.times["asr"])
        assertEquals("18:45", day1.times["maghrib"])
        assertEquals("19:50", day1.times["isha"])

        // Run TimetableValidator
        val flags = validator.validate(timetable)
        for (flag in flags) {
            println("Unexpected Flag: Day ${flag.dayIndex} (${flag.date}) ${flag.field}='${flag.currentValue}' reason=${flag.reason}")
        }
        assertEquals("TimetableValidator should produce ZERO flags on the clean timetable", 0, flags.size)
    }

    @Test
    fun testTimeFormats24hAnd12h() {
        // 24-hour inputs (from printed timetables like this one)
        assertEquals("16:01", parser.cleanAndFormatTime("16:01", "asr"))
        assertEquals("18:45", parser.cleanAndFormatTime("18:45", "maghrib"))
        assertEquals("19:50", parser.cleanAndFormatTime("19:50", "isha"))
        assertEquals("12:45", parser.cleanAndFormatTime("12:45", "dhuhr"))
        assertEquals("05:21", parser.cleanAndFormatTime("05:21", "fajr"))
        assertEquals("06:36", parser.cleanAndFormatTime("06:36", "sunrise"))

        // 12-hour inputs with omitted leading zeros or 12h PM
        assertEquals("16:01", parser.cleanAndFormatTime("4:01", "asr"))
        assertEquals("18:45", parser.cleanAndFormatTime("6:45", "maghrib"))
        assertEquals("19:50", parser.cleanAndFormatTime("7:50", "isha"))
        assertEquals("05:21", parser.cleanAndFormatTime("5:21", "fajr"))
    }
}
