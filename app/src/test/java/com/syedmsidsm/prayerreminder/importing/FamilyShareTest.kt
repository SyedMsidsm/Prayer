package com.syedmsidsm.prayerreminder.importing

import com.syedmsidsm.prayerreminder.domain.model.Timetable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

class FamilyShareTest {

    private lateinit var goldenTimetable: Timetable
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Before
    fun setUp() {
        val file = File("src/main/assets/default_timetable.json").takeIf { it.exists() }
            ?: File("app/src/main/assets/default_timetable.json")
        goldenTimetable = json.decodeFromString(file.readText())
    }

    @Test
    fun testExportAndImportRoundTrip() {
        val exportedJson = json.encodeToString(
            Timetable.serializer(),
            goldenTimetable.copy(verified = true, source = "family-file")
        )

        val stream = ByteArrayInputStream(exportedJson.toByteArray(Charsets.UTF_8))
        val result = FamilyShare.parseAndValidate(stream)

        assertTrue("Should successfully parse and validate clean .prtt file", result.isSuccess)
        val table = result.getOrThrow()

        assertEquals("Bhatkal", table.place)
        assertEquals(10, table.hijriMonth)
        assertEquals("Shawwal", table.hijriMonthName)
        assertEquals(1447, table.hijriYear)
        assertEquals(30, table.days.size)
        assertTrue(table.verified)
        assertEquals("family-file", table.source)

        // Verify Tuesday 24 March times
        val day5 = table.days.first { it.date == "2026-03-24" }
        assertEquals("05:18", day5.times["fajr"])
        assertEquals("15:59", day5.times["asr"])
        assertEquals("19:50", day5.times["isha"])
    }

    @Test
    fun testRejectsInvalidSchemaKind() {
        val invalidJson = """
            {
              "schemaVersion": 1,
              "kind": "untrusted.file",
              "place": "Bhatkal",
              "hijriMonth": 10,
              "hijriMonthName": "Shawwal",
              "hijriYear": 1447,
              "firstDate": "2026-03-20",
              "lastDate": "2026-04-18",
              "days": []
            }
        """.trimIndent()

        val stream = ByteArrayInputStream(invalidJson.toByteArray(Charsets.UTF_8))
        val result = FamilyShare.parseAndValidate(stream)

        assertTrue("Should reject invalid schema kind", result.isFailure)
    }

    @Test
    fun testRejectsCorruptedTimetableTimes() {
        val modifiedDays = goldenTimetable.days.toMutableList()
        // Corrupt Fajr on day 0 to be after Sunrise
        val badTimes = modifiedDays[0].times.toMutableMap()
        badTimes["fajr"] = "07:00" // Sunrise is 06:36, Fajr 07:00 violates order!
        modifiedDays[0] = modifiedDays[0].copy(times = badTimes)

        val badTable = goldenTimetable.copy(days = modifiedDays, verified = true)
        val badJson = json.encodeToString(Timetable.serializer(), badTable)

        val stream = ByteArrayInputStream(badJson.toByteArray(Charsets.UTF_8))
        val result = FamilyShare.parseAndValidate(stream)

        assertTrue("Should reject file with chronological order violations", result.isFailure)
    }
}
