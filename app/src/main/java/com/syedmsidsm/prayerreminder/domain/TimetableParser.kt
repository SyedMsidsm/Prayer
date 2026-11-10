package com.syedmsidsm.prayerreminder.domain

import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.domain.model.TimetableDay
import java.time.LocalDate
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
        val PRAYER_HEADERS = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
        val WEEKDAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val MONTHS = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

        val HEADER_ALIASES = mapOf(
            "sno" to listOf("s.no", "sno", "no", "no.", "sr", "sr.", "sl", "sl.", "serial", "s.no."),
            "date" to listOf("date", "dates", "tarikh", "tareekh", "dt"),
            "day" to listOf("day", "days", "din", "roz", "vaar"),
            "fajr" to listOf("fajr", "fajar", "faj", "subah", "subh"),
            "sunrise" to listOf("sunrise", "sunr", "tulu", "ishraq", "tloo", "shuruq", "sun"),
            "dhuhr" to listOf("dhuhr", "zuhr", "zohar", "duhr", "dhur", "zohr", "johur"),
            "asr" to listOf("asr", "asar", "asrr"),
            "maghrib" to listOf("maghrib", "magrib", "mgrb", "magh", "sunset", "gurub", "ghuroob"),
            "isha" to listOf("isha", "ishaa", "esha", "ish")
        )
    }

    fun parse(tokens: List<OcrToken>): ParsedTimetableResult? {
        if (tokens.isEmpty()) return null

        val notes = mutableListOf<String>()

        // 1. Detect Place & Banner if present
        var place = "Bhatkal"
        val fullText = tokens.joinToString(" ") { it.text }
        val azanMatch = Regex("""([A-Za-z]+)\s+AZAN""", RegexOption.IGNORE_CASE).find(fullText)
        if (azanMatch != null) {
            place = azanMatch.groupValues[1].lowercase().replaceFirstChar { it.uppercase() }
            notes.add("Detected place: $place")
        }

        var startYear = 2026
        var startMonth = 3 // March default
        val bannerMatch = Regex(
            """(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\s*[-–]\s*(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)?\s*(\d{4})?""",
            RegexOption.IGNORE_CASE
        ).find(fullText)
        if (bannerMatch != null) {
            val monthStr = bannerMatch.groupValues[1].uppercase()
            startMonth = monthStringToNumber(monthStr)
            bannerMatch.groupValues[2].toIntOrNull()?.let { startYear = it }
            notes.add("Detected banner: $monthStr $startYear")
        }

        // 2. Identify the Header Row
        // Cluster tokens in the upper region into horizontal lines to find the table header row
        val headerCandidates = clusterIntoLines(tokens)
        var bestHeaderLine: List<OcrToken>? = null
        var bestHeaderScore = 0

        for (line in headerCandidates) {
            var score = 0
            val matchedKeys = mutableSetOf<String>()
            for (token in line) {
                val clean = normalizeText(token.text)
                for ((key, aliases) in HEADER_ALIASES) {
                    if (!matchedKeys.contains(key)) {
                        val matches = aliases.any { alias ->
                            clean == alias || (alias.length >= 4 && levenshteinDistance(clean, alias) <= 1)
                        }
                        if (matches) {
                            matchedKeys.add(key)
                            score++
                        }
                    }
                }
            }
            if (score > bestHeaderScore && score >= 3) {
                bestHeaderScore = score
                bestHeaderLine = line
            }
        }

        val headerTokens = mutableMapOf<String, OcrToken>()
        val headerBottom: Int

        if (bestHeaderLine != null) {
            headerBottom = bestHeaderLine.maxOf { it.bottom }
            for (token in bestHeaderLine) {
                val clean = normalizeText(token.text)
                for ((key, aliases) in HEADER_ALIASES) {
                    if (!headerTokens.containsKey(key)) {
                        val matches = aliases.any { alias ->
                            clean == alias || (alias.length >= 4 && levenshteinDistance(clean, alias) <= 1)
                        }
                        if (matches) {
                            headerTokens[key] = token
                        }
                    }
                }
            }
            notes.add("Detected header row with ${headerTokens.size} columns at Y=$headerBottom")
        } else {
            // Fallback: search all tokens
            for (token in tokens) {
                val clean = normalizeText(token.text)
                for ((key, aliases) in HEADER_ALIASES) {
                    if (!headerTokens.containsKey(key)) {
                        val matches = aliases.any { alias ->
                            clean == alias || (alias.length >= 4 && levenshteinDistance(clean, alias) <= 1)
                        }
                        if (matches) {
                            headerTokens[key] = token
                        }
                    }
                }
            }
            val prayerHeaderTokens = PRAYER_HEADERS.mapNotNull { headerTokens[it] }
            headerBottom = if (prayerHeaderTokens.isNotEmpty()) {
                prayerHeaderTokens.maxOf { it.bottom }
            } else {
                tokens.minOf { it.top } + 40
            }
        }

        // 3. Determine Column Centers for the 6 Prayers
        val prayerCenters = mutableMapOf<String, Int>()
        for (p in PRAYER_HEADERS) {
            headerTokens[p]?.let { prayerCenters[p] = it.centerX }
        }

        // Interpolate any missing prayer column centers
        val knownPrayerList = PRAYER_HEADERS.mapNotNull { h -> prayerCenters[h]?.let { x -> Pair(h, x) } }
            .sortedBy { it.second }

        val prayerGap = if (knownPrayerList.size >= 2) {
            val totalSpan = knownPrayerList.last().second - knownPrayerList.first().second
            val spanSteps = PRAYER_HEADERS.indexOf(knownPrayerList.last().first) - PRAYER_HEADERS.indexOf(knownPrayerList.first().first)
            if (spanSteps > 0) totalSpan / spanSteps else 110
        } else 110

        // Fill missing prayer centers based on order
        for (i in PRAYER_HEADERS.indices) {
            val prayer = PRAYER_HEADERS[i]
            if (prayerCenters[prayer] == null) {
                val prev = (i - 1 downTo 0).firstOrNull { prayerCenters[PRAYER_HEADERS[it]] != null }
                val next = (i + 1 until PRAYER_HEADERS.size).firstOrNull { prayerCenters[PRAYER_HEADERS[it]] != null }
                if (prev != null && next != null) {
                    val pX = prayerCenters[PRAYER_HEADERS[prev]]!!
                    val nX = prayerCenters[PRAYER_HEADERS[next]]!!
                    prayerCenters[prayer] = pX + ((nX - pX) * (i - prev)) / (next - prev)
                } else if (prev != null) {
                    prayerCenters[prayer] = prayerCenters[PRAYER_HEADERS[prev]]!! + prayerGap * (i - prev)
                } else if (next != null) {
                    prayerCenters[prayer] = prayerCenters[PRAYER_HEADERS[next]]!! - prayerGap * (next - i)
                } else {
                    prayerCenters[prayer] = 400 + i * prayerGap
                }
            }
        }

        val fajrCenterX = prayerCenters["fajr"] ?: 400
        val sunriseCenterX = prayerCenters["sunrise"] ?: (fajrCenterX + prayerGap)
        val fajrLeftBoundary = fajrCenterX - (sunriseCenterX - fajrCenterX) / 2

        // 4. Filter and Preprocess Body Tokens
        // Body tokens strictly start below the header row
        val rawBodyTokens = tokens.filter { it.centerY > headerBottom - 5 && it.bottom > headerBottom }
        if (rawBodyTokens.isEmpty()) return null

        // Merge horizontally adjacent tokens that form a time pattern (e.g. "05:" + "21" -> "05:21")
        val bodyTokens = mergeAdjacentTimeFragments(rawBodyTokens)

        // 5. Cluster Body Tokens into Table Rows
        val sortedBody = bodyTokens.sortedBy { it.centerY }
        val medianTokenHeight = sortedBody.map { it.height }.sorted().let { it[it.size / 2] }
        val lineGapThreshold = maxOf(8, (medianTokenHeight * 0.45).toInt())

        val rawRows = mutableListOf<MutableList<OcrToken>>()
        for (tok in sortedBody) {
            val lastRow = rawRows.lastOrNull()
            if (lastRow != null && tok.centerY - lastRow.last().centerY <= lineGapThreshold) {
                lastRow.add(tok)
            } else {
                rawRows.add(mutableListOf(tok))
            }
        }

        // Filter valid table rows (must have at least one time pattern or digits)
        val tableRows = rawRows.filter { row ->
            row.any { hasTimePattern(it.text) } || row.count { Regex("""\d""").containsMatchIn(it.text) } >= 2
        }

        if (tableRows.isEmpty()) return null

        // 6. Parse Each Table Row into TimetableDay
        val parsedDays = mutableListOf<TimetableDay>()
        var currentDate = LocalDate.of(startYear, startMonth, 20)
        var currentMonth = startMonth
        var currentYear = startYear
        var prevDayOfMonth: Int? = null

        for (i in tableRows.indices) {
            val rowTokens = tableRows[i].sortedBy { it.centerX }

            // Split into metadata tokens (left of Fajr) and prayer tokens (at or right of Fajr)
            val metaTokens = rowTokens.filter { it.centerX < fajrLeftBoundary }
            val prayerTokens = rowTokens.filter { it.centerX >= fajrLeftBoundary }

            // --- A. Parse Metadata (S.No, Date, Weekday) ---
            var parsedWeekday = ""
            var parsedMonth = currentMonth
            var hijriDay = i + 1
            var dayOfMonth: Int? = null

            val remainingMeta = metaTokens.toMutableList()

            // 1) Find Weekday
            val weekdayMatch = remainingMeta.firstOrNull { tok ->
                val clean = normalizeText(tok.text)
                WEEKDAYS.any { w -> clean == w.lowercase() || levenshteinDistance(clean, w.lowercase()) <= 1 }
            }
            if (weekdayMatch != null) {
                val clean = normalizeText(weekdayMatch.text)
                parsedWeekday = WEEKDAYS.minByOrNull { levenshteinDistance(clean, it.lowercase()) } ?: weekdayMatch.text
                remainingMeta.remove(weekdayMatch)
            }

            // 2) Find Month Name (e.g. "Mar", "Apr")
            val monthMatch = remainingMeta.firstOrNull { tok ->
                val clean = tok.text.trim().uppercase()
                MONTHS.any { m -> clean.contains(m) }
            }
            if (monthMatch != null) {
                for (m in MONTHS) {
                    if (monthMatch.text.uppercase().contains(m)) {
                        parsedMonth = monthStringToNumber(m)
                        break
                    }
                }
                remainingMeta.remove(monthMatch)
            }

            // 3) Find Numbers in remaining metadata tokens
            val numbersInMeta = remainingMeta.flatMap { tok ->
                Regex("""\d+""").findAll(tok.text).mapNotNull { it.value.toIntOrNull() }.toList()
            }

            if (numbersInMeta.size >= 2) {
                // First is S.No / Hijri day, second is Gregorian date
                hijriDay = numbersInMeta[0]
                dayOfMonth = numbersInMeta[1]
            } else if (numbersInMeta.size == 1) {
                val num = numbersInMeta[0]
                if (num in 1..31) {
                    dayOfMonth = num
                } else {
                    hijriDay = num
                }
            }

            // --- B. Date Rollover and Continuity ---
            if (parsedMonth != currentMonth) {
                currentMonth = parsedMonth
            } else if (dayOfMonth != null && prevDayOfMonth != null && dayOfMonth < prevDayOfMonth!!) {
                // Month rollover without explicit month label (e.g. 31 -> 1)
                currentMonth = if (currentMonth == 12) 1 else currentMonth + 1
                if (currentMonth == 1) currentYear++
            }

            if (i == 0 && dayOfMonth != null) {
                currentDate = LocalDate.of(currentYear, currentMonth, dayOfMonth)
            } else if (dayOfMonth != null) {
                currentDate = LocalDate.of(currentYear, currentMonth, dayOfMonth)
            } else if (i > 0) {
                currentDate = currentDate.plusDays(1)
            }
            prevDayOfMonth = currentDate.dayOfMonth

            // Validate / fallback weekday against currentDate
            val expectedWeekday = currentDate.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            if (parsedWeekday.isBlank()) {
                parsedWeekday = expectedWeekday
            }

            // --- C. Parse Prayer Times ---
            val timesMap = mutableMapOf<String, String>()

            // Find all valid candidate times in prayer tokens
            val rowTimeStrings = mutableListOf<String>()
            for (tok in prayerTokens) {
                val t = extractTimeCandidate(tok.text)
                if (t != null) {
                    rowTimeStrings.add(t)
                }
            }

            // If exactly 6 times found, they are strictly Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha
            if (rowTimeStrings.size == 6) {
                for (pIndex in 0..5) {
                    val pName = PRAYER_HEADERS[pIndex]
                    val cleaned = cleanAndFormatTime(rowTimeStrings[pIndex], pName)
                    if (cleaned != null) {
                        timesMap[pName] = cleaned
                    }
                }
            } else {
                // Map prayer tokens to the closest prayer column center
                val prayerBoundaries = mutableListOf<Int>()
                for (pIndex in 0 until PRAYER_HEADERS.size - 1) {
                    val c1 = prayerCenters[PRAYER_HEADERS[pIndex]]!!
                    val c2 = prayerCenters[PRAYER_HEADERS[pIndex + 1]]!!
                    prayerBoundaries.add((c1 + c2) / 2)
                }

                for (tok in prayerTokens) {
                    val timeStr = extractTimeCandidate(tok.text) ?: continue
                    val colIndex = prayerBoundaries.indexOfFirst { tok.centerX < it }
                        .let { if (it == -1) PRAYER_HEADERS.size - 1 else it }
                    val pName = PRAYER_HEADERS[colIndex]
                    if (!timesMap.containsKey(pName)) {
                        cleanAndFormatTime(timeStr, pName)?.let { timesMap[pName] = it }
                    }
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
            hijriMonth = 10,
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

    /**
     * Clusters tokens into horizontal lines based on vertical proximity.
     */
    private fun clusterIntoLines(tokens: List<OcrToken>): List<List<OcrToken>> {
        val sorted = tokens.sortedBy { it.centerY }
        val lines = mutableListOf<MutableList<OcrToken>>()

        for (token in sorted) {
            val lastLine = lines.lastOrNull()
            if (lastLine != null && abs(token.centerY - lastLine.map { it.centerY }.average()) <= 12) {
                lastLine.add(token)
            } else {
                lines.add(mutableListOf(token))
            }
        }
        return lines.map { it.sortedBy { tok -> tok.centerX } }
    }

    /**
     * Merges adjacent tokens if they are horizontally close and form a time pattern
     * like "05:" followed by "21", or "16" followed by ":01".
     */
    private fun mergeAdjacentTimeFragments(tokens: List<OcrToken>): List<OcrToken> {
        val sorted = tokens.sortedWith(compareBy({ it.centerY / 15 }, { it.left }))
        val merged = mutableListOf<OcrToken>()

        var i = 0
        while (i < sorted.size) {
            val curr = sorted[i]
            if (i + 1 < sorted.size) {
                val next = sorted[i + 1]
                val sameLine = abs(curr.centerY - next.centerY) <= 10
                val horizontalGap = next.left - curr.right
                val combinedText = "${curr.text}${next.text}".replace(" ", "")

                if (sameLine && horizontalGap in -5..15 && hasTimePattern(combinedText)) {
                    merged.add(
                        OcrToken(
                            text = combinedText,
                            left = curr.left,
                            top = minOf(curr.top, next.top),
                            right = next.right,
                            bottom = maxOf(curr.bottom, next.bottom)
                        )
                    )
                    i += 2
                    continue
                }
            }
            merged.add(curr)
            i++
        }
        return merged
    }

    private fun hasTimePattern(text: String): Boolean {
        val clean = text.replace('O', '0').replace('o', '0')
        return Regex("""\d{1,2}[:.;,]\d{2}""").containsMatchIn(clean) ||
            Regex("""^\d{3,4}$""").matches(clean)
    }

    private fun extractTimeCandidate(raw: String): String? {
        val clean = raw.trim()
            .replace('O', '0')
            .replace('o', '0')
            .replace('l', '1')
            .replace('I', '1')
            .replace('i', '1')
            .replace('S', '5')
            .replace('s', '5')
            .replace('B', '8')

        val colonMatch = Regex("""(\d{1,2}\s*[:.;,]\s*\d{2})""").find(clean)
        if (colonMatch != null) return colonMatch.groupValues[1]

        val digitsMatch = Regex("""\b(\d{3,4})\b""").find(clean)
        if (digitsMatch != null) return digitsMatch.groupValues[1]

        return null
    }

    /**
     * Cleans OCR raw time string, repairs common misrecognitions, and normalizes to 24h format.
     */
    fun cleanAndFormatTime(raw: String, prayer: String): String? {
        val candidate = extractTimeCandidate(raw) ?: return null
        var text = candidate.replace(" ", "")

        val match = Regex("""(\d{1,2})[:.;,](\d{2})""").find(text)
            ?: Regex("""^(\d{3,4})$""").find(text)?.let { m ->
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

        // Normalize to 24-hour format
        when (prayer.lowercase()) {
            "fajr", "sunrise" -> {
                // AM: 5 -> 05; 12 -> 00
                if (hour == 12) hour = 0
            }
            "dhuhr" -> {
                // Midday: 11:xx = 11, 12:xx = 12, 1:xx = 13, 2:xx = 14
                if (hour in 1..2) hour += 12
            }
            "asr" -> {
                // Afternoon: 14:xx..17:xx keep as is; 2..5 -> 14..17
                if (hour in 2..5) hour += 12
            }
            "maghrib" -> {
                // Evening: 17:xx..20:xx keep as is; 5..8 -> 17..20
                if (hour in 5..8) hour += 12
            }
            "isha" -> {
                // Night: 18:xx..22:xx keep as is; 6..10 -> 18..22
                if (hour in 6..10) hour += 12
            }
        }

        return "%02d:%02d".format(hour, min)
    }

    private fun normalizeText(s: String): String {
        return s.lowercase().replace(Regex("[^a-z0-9.]"), "")
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