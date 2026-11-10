package com.syedmsidsm.prayerreminder.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class LanguageAndPolishTest {

    private lateinit var enStrings: Map<String, String>
    private lateinit var arStrings: Map<String, String>
    private lateinit var urStrings: Map<String, String>

    private lateinit var enMonths: List<String>
    private lateinit var arMonths: List<String>
    private lateinit var urMonths: List<String>

    @Before
    fun setUp() {
        val root = File(".").canonicalFile
        val resDir = File(root, "app/src/main/res").takeIf { it.exists() }
            ?: File(root, "src/main/res")

        enStrings = parseStringsFile(File(resDir, "values/strings.xml"))
        arStrings = parseStringsFile(File(resDir, "values-ar/strings.xml"))
        urStrings = parseStringsFile(File(resDir, "values-ur/strings.xml"))

        enMonths = parseStringArray(File(resDir, "values/strings.xml"), "hijri_months")
        arMonths = parseStringArray(File(resDir, "values-ar/strings.xml"), "hijri_months")
        urMonths = parseStringArray(File(resDir, "values-ur/strings.xml"), "hijri_months")
    }

    private fun parseStringsFile(file: File): Map<String, String> {
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(file)
        val nodes = doc.getElementsByTagName("string")
        val map = mutableMapOf<String, String>()
        for (i in 0 until nodes.length) {
            val el = nodes.item(i) as Element
            val name = el.getAttribute("name")
            val text = el.textContent
            map[name] = text
        }
        return map
    }

    private fun parseStringArray(file: File, arrayName: String): List<String> {
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(file)
        val nodes = doc.getElementsByTagName("string-array")
        for (i in 0 until nodes.length) {
            val el = nodes.item(i) as Element
            if (el.getAttribute("name") == arrayName) {
                val itemNodes = el.getElementsByTagName("item")
                val list = mutableListOf<String>()
                for (j in 0 until itemNodes.length) {
                    list.add(itemNodes.item(j).textContent)
                }
                return list
            }
        }
        return emptyList()
    }

    @Test
    fun testAllEnglishKeysTranslatedInArabicAndUrdu() {
        val missingInArabic = enStrings.keys.filter { !arStrings.containsKey(it) }
        val missingInUrdu = enStrings.keys.filter { !urStrings.containsKey(it) }

        assertTrue("Missing string keys in Arabic: $missingInArabic", missingInArabic.isEmpty())
        assertTrue("Missing string keys in Urdu: $missingInUrdu", missingInUrdu.isEmpty())
    }

    @Test
    fun testPrayerNamesInAllThreeLanguages() {
        val prayerKeys = listOf(
            "prayer_fajr",
            "prayer_sunrise",
            "prayer_dhuhr",
            "prayer_asr",
            "prayer_maghrib",
            "prayer_isha",
            "prayer_jumuah"
        )
        for (key in prayerKeys) {
            assertNotNull(enStrings[key])
            assertNotNull(arStrings[key])
            assertNotNull(urStrings[key])

            assertFalse(enStrings[key]!!.isBlank())
            assertFalse(arStrings[key]!!.isBlank())
            assertFalse(urStrings[key]!!.isBlank())
        }

        assertEquals("Fajr", enStrings["prayer_fajr"])
        assertEquals("الفجر", arStrings["prayer_fajr"])
        assertEquals("فجر", urStrings["prayer_fajr"])

        assertEquals("Jumu'ah", enStrings["prayer_jumuah"]?.replace("\\", ""))
        assertEquals("الجمعة", arStrings["prayer_jumuah"])
        assertEquals("جمعہ", urStrings["prayer_jumuah"])
    }

    @Test
    fun testNotificationChannelNamesAreTranslated() {
        val channelKeys = listOf("channel_voice", "channel_sound", "channel_general")
        for (key in channelKeys) {
            assertTrue(enStrings.containsKey(key))
            assertTrue(arStrings.containsKey(key))
            assertTrue(urStrings.containsKey(key))

            assertFalse(enStrings[key]!!.isBlank())
            assertFalse(arStrings[key]!!.isBlank())
            assertFalse(urStrings[key]!!.isBlank())
        }

        assertEquals("Reminders (voice)", enStrings["channel_voice"])
        assertEquals("التذكيرات (صوت)", arStrings["channel_voice"])
        assertEquals("یاد دہانیاں (آواز)", urStrings["channel_voice"])
    }

    @Test
    fun testAmPmMarkers() {
        assertEquals("AM", enStrings["am_marker"])
        assertEquals("PM", enStrings["pm_marker"])

        assertEquals("ص", arStrings["am_marker"])
        assertEquals("م", arStrings["pm_marker"])

        assertEquals("AM", urStrings["am_marker"])
        assertEquals("PM", urStrings["pm_marker"])
    }

    @Test
    fun testHijriMonthsCountIsTwelve() {
        assertEquals(12, enMonths.size)
        assertEquals(12, arMonths.size)
        assertEquals(12, urMonths.size)

        // Month 10: Shawwal
        assertEquals("Shawwal", enMonths[9])
        assertEquals("شوال", arMonths[9])
        assertEquals("شوال", urMonths[9])
    }
}
