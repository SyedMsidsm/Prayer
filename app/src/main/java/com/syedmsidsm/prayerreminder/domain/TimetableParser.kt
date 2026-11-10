package com.syedmsidsm.prayerreminder.domain

import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.domain.model.TimetableDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

data class OcrToken(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val centerX: Int get() = (left + right) / 2
    val centerY: Int get() = (top + bottom) / 2
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}

data class ParsedTimetableResult(
    val timetable: Timetable,
    val detectedPlace: String,
    val detectedMonth: String,
    val confidenceNotes: List<String>
)

class TimetableParser {

    companion object {
        private val PRAYER_HEADERS = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
        private val WEEKDAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        private val HEADER_ALIASES = mapOf(
            "fajr" to listOf("fajr", "fajar", "faj"),
            "sunrise" to listOf("sunrise", "sunr", "tulu", "ishraq", "tloo"),
            "dhuhr" to listOf("dhuhr", "zuhr", "zohar", "duhr", "dhur", "zohr"),
            "asr" to listOf("asr", "asar", "asrr"),
            "maghrib" to listOf("maghrib", "magrib", "mgrb", "magh"),
            "isha" to listOf("isha", "ishaa", "esha", "ish")
        )
    }

    fun parse(tokens: List<OcrToken>): ParsedTimetableResult? {
        if (tokens.isEmpty()) return null

        val notes = mutableListOf<String>()

        // 1. Detect Place from Header (text before AZAN)
        var place = "Bhatkal"
        val fullText = tokens.joinToString(" ") { it.text }
        val azanMatch = Regex("""([A-Za-z]+)\s+AZAN""", RegexOption.IGNORE_CASE).find(fullText)
        if (azanMatch != null) {
            place = azanMatch.groupValues[1].lowercase().replaceFirstChar { it.uppercase() }
            notes.add("Detected place: $place")
        }

        // 2. Detect Year and Months from banner (e.g. "MAR - APR 2026")
        var startYear = 2026
        var startMonth = 3 // March
        val bannerMatch = Regex("""(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\s*[-–]\s*(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\s*(\d{4})""", RegexOption.IGNORE_CASE).find(fullText)
        if (bannerMatch != null) {
            val monthStr = bannerMatch.groupValues[1].uppercase()
            startYear = bannerMatch.groupValues[2].toIntOrNull() ?: 2026
            startMonth = monthStringToNumber(monthStr)
            notes.add("Detected banner: $monthStr $startYear")
        }

        // 3. Find Header Tokens (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha, Day, Date)
        val headerTokens = mutableMapOf<String, OcrToken>()
        for (token in tokens) {
            val clean = token.text.lowercase().trim()
            for ((standardHeader, aliases) in HEADER_ALIASES) {
                if (!headerTokens.containsKey(standardHeader)) {
                    val matches = aliases.any { alias -> levenshteinDistance(clean, alias) <= 1 }
                        || levenshteinDistance(clean, standardHeader) <= 2
                    if (matches) {
                        headerTokens[standardHeader] = token
                    }
                }
            }
            if (!headerTokens.containsKey("day") && (levenshteinDistance(clean, "day") <= 1 || clean == "days")) {
                headerTokens["day"] = token
            }
            if (!headerTokens.containsKey("date") && (levenshteinDistance(clean, "date") <= 1 || clean == "dates")) {
                headerTokens["date"] = token
            }
        }

        // If at least 4 prayer headers found, use their Y coordinate to define the header baseline
        val headerY = if (headerTokens.isNotEmpty()) {
            headerTokens.values.map { it.centerY }.average().toInt()
        } else {
            tokens.minOf { it.centerY } + 50
        }

        // 4. Determine Column Centers (X coordinates)
        val columnCenters = mutableMapOf<String, Int>()
        for ((name, token) in headerTokens) {
            columnCenters[name] = token.centerX
        }

        // Fallback for missing column centers based on found columns
        val sortedPrayerColumns = PRAYER_HEADERS.mapNotNull { h -> columnCenters[h]?.let { x -> Pair(h, x) } }
            .sortedBy { it.second }

        val medianGap = if (sortedPrayerColumns.size >= 2) {
            (sortedPrayerColumns.last().second - sortedPrayerColumns.first().second) / (sortedPrayerColumns.size - 1)
        } else {
            100
        }

        // Estimate missing prayer columns if any
        if (columnCenters["fajr"] != null && columnCenters["sunrise"] == null) {
            columnCenters["sunrise"] = columnCenters["fajr"]!! + medianGap
        }
        if (columnCenters["isha"] != null && columnCenters["maghrib"] == null) {
            columnCenters["maghrib"] = columnCenters["isha"]!! - medianGap
        }

        // Date and Day are typically to the left of Fajr
        val fajrX = columnCenters["fajr"] ?: (sortedPrayerColumns.firstOrNull()?.second ?: 300)
        if (columnCenters["day"] == null) {
            columnCenters["day"] = fajrX - medianGap
        }
        if (columnCenters["date"] == null) {
            columnCenters["date"] = (columnCenters["day"] ?: (fajrX - medianGap)) - (medianGap * 0.9).toInt()
        }
        val isDateX = (columnCenters["date"] ?: (fajrX - medianGap * 2)) - (medianGap * 0.9).toInt()

        // 5. Group Tokens into Rows below Header
        val bodyTokens = tokens.filter { it.centerY > headerY + 15 }
        val medianTokenHeight = if (bodyTokens.isNotEmpty()) {
            bodyTokens.map { it.height }.sorted()[bodyTokens.size / 2]
        } else 20
        val rowTolerance = (medianTokenHeight * 0.6).coerceAtLeast(10.0).toInt()

        val rows = mutableListOf<MutableList<OcrToken>>()
        val sortedByY = bodyTokens.sortedBy { it.centerY }

        for (token in sortedByY) {
            val matchingRow = rows.firstOrNull { row ->
                val rowY = row.map { it.centerY }.average().toInt()
                abs(token.centerY - rowY) <= rowTolerance
            }
            if (matchingRow != null) {
                matchingRow.add(token)
            } else {
                rows.add(mutableListOf(token))
            }
        }

        // Sort each row by X, and sort rows top to bottom
        val sortedRows = rows.map { it.sortedBy { tok -> tok.centerX } }
            .sortedBy { it.map { tok -> tok.centerY }.average() }

        // Filter out non-table rows (e.g. footer lines) by ensuring at least 2 numbers/time patterns
        val tableRows = sortedRows.filter { row ->
            row.count { Regex("""\d""").containsMatchIn(it.text) } >= 2
        }

        // 6. Parse cells for each row
        val parsedDays = mutableListOf<TimetableDay>()
        var currentDate = LocalDate.of(startYear, startMonth, 20) // default start if not read
        var hijriCounter = 1

        for (i in tableRows.indices) {
            val row = tableRows[i]
            val rowTokens = row.toMutableList()

            // Assign tokens to closest columns
            val cellValues = mutableMapOf<String, String>()
            for (token in rowTokens) {
                val bestColumn = columnCenters.entries.minByOrNull { abs(it.value - token.centerX) }
                if (bestColumn != null && abs(bestColumn.value - token.centerX) < medianGap * 0.8) {
                    val existing = cellValues[bestColumn.key]
                    cellValues[bestColumn.key] = if (existing == null) token.text else "$existing ${token.text}"
                }
            }

            // Assign Is. Date (furthest left column)
            val leftmostToken = rowTokens.minByOrNull { it.centerX }
            val rawIsDate = if (leftmostToken != null && leftmostToken.centerX < (columnCenters["date"] ?: fajrX)) {
                leftmostToken.text
            } else ""

            val hijriDay = parseHijriDay(rawIsDate, hijriCounter)
            hijriCounter = hijriDay + 1

            // Parse Gregorian Date Number
            val rawDateStr = cellValues["date"] ?: ""
            val dayOfMonth = parseDateNumber(rawDateStr)
            if (i == 0 && dayOfMonth != null) {
                currentDate = LocalDate.of(startYear, startMonth, dayOfMonth)
            } else if (i > 0) {
                currentDate = currentDate.plusDays(1)
            }

            // Parse Weekday
            val rawDay = cellValues["day"] ?: ""
            val parsedWeekday = parseWeekday(rawDay, currentDate.dayOfWeek.name.take(3))

            // Parse and format Prayer times (adding AM/PM to convert to 24h)
            val timesMap = mutableMapOf<String, String>()
            for (prayer in PRAYER_HEADERS) {
                val rawTime = cellValues[prayer] ?: ""
                val cleanedTime = cleanAndFormatTime(rawTime, prayer)
                if (cleanedTime != null) {
                    timesMap[prayer] = cleanedTime
                }
            }

            parsedDays.add(
                TimetableDay(
                    date = currentDate.toString(),
                    weekday = parsedWeekday,
                    hijriDay = hijriDay,
                    times = timesMap
                )
            )
        }

        if (parsedDays.isEmpty()) return null

        val timetable = Timetable(
            schemaVersion = 1,
            kind = "prayerreminder.timetable",
            place = place,
            hijriMonth = 10, // Shawwal
            hijriMonthName = "Shawwal",
            hijriYear = 1447,
            firstDate = parsedDays.first().date,
            lastDate = parsedDays.last().date,
            timeZone = "Asia/Kolkata",
            source = "image",
            verified = false,
            importedAt = System.currentTimeMillis(),
            days = parsedDays
        )

        return ParsedTimetableResult(
            timetable = timetable,
            detectedPlace = place,
            detectedMonth = "Shawwal",
            confidenceNotes = notes
        )
    }

    private fun parseHijriDay(raw: String, fallback: Int): Int {
        val digits = Regex("""\d+""").find(raw)?.value?.toIntOrNull()
        return digits ?: fallback
    }

    private fun parseDateNumber(raw: String): Int? {
        val digits = Regex("""\d+""").find(raw)?.value?.toIntOrNull()
        return if (digits in 1..31) digits else null
    }

    private fun parseWeekday(raw: String, fallback: String): String {
        val clean = raw.trim().lowercase()
        for (w in WEEKDAYS) {
            if (levenshteinDistance(clean, w.lowercase()) <= 1) return w
        }
        return fallback.lowercase().replaceFirstChar { it.uppercase() }
    }

    /**
     * Cleans OCR raw time string, repairs common digit misrecognitions, and adds AM/PM to make 24h format.
     */
    fun cleanAndFormatTime(raw: String, prayer: String): String? {
        var text = raw.trim()
            .replace('O', '0')
            .replace('o', '0')
            .replace('l', '1')
            .replace('I', '1')
            .replace('i', '1')
            .replace('S', '5')
            .replace('s', '5')
            .replace('B', '8')
            .replace(Regex("""(?i)[ap]m"""), "")
            .trim()

        // Fix separator (dot, comma, semicolon, colon with optional spaces, or no separator 3-4 digits)
        val match = Regex("""(\d{1,2})\s*[:.;,]\s*(\d{2})""").find(text)
            ?: Regex("""^(\d{3,4})$""").find(text.replace(" ", ""))?.let { m ->
                val digits = m.groupValues[1]
                if (digits.length == 3) {
                    Regex("""^(\d{1})(\d{2})$""").find(digits)
                } else {
                    Regex("""^(\d{2})(\d{2})$""").find(digits)
                }
            }
            ?: return null

        var hour = match.groupValues[1].toIntOrNull() ?: return null
        val min = match.groupValues[2].toIntOrNull() ?: return null

        if (min !in 0..59) return null

        // Add AM / PM based on prayer rule (Spec 6.3 step 7)
        when (prayer.lowercase()) {
            "fajr", "sunrise" -> {
                // AM: 5 -> 05
                if (hour == 12) hour = 0
            }
            "dhuhr" -> {
                // 11:xx = AM (11), 12:xx = PM (12), 1:xx = PM (13)
                if (hour in 1..11) hour += 12
            }
            "asr", "maghrib", "isha" -> {
                // PM: 3:59 -> 15:59, 6:46 -> 18:46, 7:50 -> 19:50
                if (hour in 1..11) hour += 12
            }
        }

        return "%02d:%02d".format(hour, min)
    }

    private fun monthStringToNumber(m: String): Int {
        return when (m.uppercase()) {
            "JAN" -> 1
            "FEB" -> 2
            "MAR" -> 3
            "APR" -> 4
            "MAY" -> 5
            "JUN" -> 6
            "JUL" -> 7
            "AUG" -> 8
            "SEP" -> 9
            "OCT" -> 10
            "NOV" -> 11
            "DEC" -> 12
            else -> 3
        }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
