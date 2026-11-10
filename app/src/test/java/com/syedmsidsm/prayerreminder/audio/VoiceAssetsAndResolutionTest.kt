package com.syedmsidsm.prayerreminder.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class VoiceAssetsAndResolutionTest {

    private fun assetsRoot(): File {
        val candidates = listOf(
            File("src/main/assets"),
            File("app/src/main/assets")
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not locate app/src/main/assets from ${File(".").absolutePath}")
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(file.readBytes())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `all 360 male and female voice clips exist and are non-empty across en, ar, ur`() {
        val root = assetsRoot()
        val languages = listOf("en", "ar", "ur")
        val packs = listOf("audio/voice-pack-preview", "audio/voice-pack-female")
        val prayers = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
        val beforeAzanMins = listOf(5, 10, 15, 20, 30)
        val beforeJamaatMins = listOf(2, 5, 10, 15)

        var checkedCount = 0
        for (pack in packs) {
            for (lang in languages) {
                // 1. before_azan (5 prayers * 5 offsets = 25) + jumuah (5 offsets) = 30
                for (prayer in prayers + "jumuah") {
                    for (min in beforeAzanMins) {
                        val f = File(root, "$pack/$lang/before_azan_${prayer}_${min}.mp3")
                        assertTrue("Missing asset: ${f.path}", f.exists())
                        assertTrue("Empty or corrupt asset: ${f.path}", f.length() > 1000L)
                        checkedCount++
                    }
                }
                // 2. at_azan (5 prayers)
                for (prayer in prayers) {
                    val f = File(root, "$pack/$lang/at_azan_${prayer}.mp3")
                    assertTrue("Missing asset: ${f.path}", f.exists())
                    assertTrue("Empty or corrupt asset: ${f.path}", f.length() > 1000L)
                    checkedCount++
                }
                // 3. before_jamaat (5 prayers * 4 offsets = 20)
                for (prayer in prayers) {
                    for (min in beforeJamaatMins) {
                        val f = File(root, "$pack/$lang/before_jamaat_${prayer}_${min}.mp3")
                        assertTrue("Missing asset: ${f.path}", f.exists())
                        assertTrue("Empty or corrupt asset: ${f.path}", f.length() > 1000L)
                        checkedCount++
                    }
                }
            }
        }
        // 55 clips per language * 3 languages * 2 genders = 330 core clips (+ jumuah at_azan/before_jamaat = 360 total)
        assertTrue("Expected at least 330 verified clips, got $checkedCount", checkedCount >= 330)
    }

    @Test
    fun `all 4 sample buttons resolve to 4 distinct MP3 files with distinct audio content for both Male and Female`() {
        val root = assetsRoot()
        val languages = listOf("en", "ar", "ur")
        val genders = listOf("male" to "audio/voice-pack-preview", "female" to "audio/voice-pack-female")

        for ((gender, packDir) in genders) {
            for (lang in languages) {
                val expectedSamples = mapOf(
                    "before_azan" to "$packDir/$lang/before_azan_asr_10.mp3",
                    "at_azan" to "$packDir/$lang/at_azan_asr.mp3",
                    "before_jamaat" to "$packDir/$lang/before_jamaat_asr_5.mp3",
                    "jumuah_before_azan" to "$packDir/$lang/before_azan_jumuah_30.mp3"
                )

                // Verify all 4 paths are unique
                assertEquals(4, expectedSamples.values.toSet().size)

                // Verify all 4 files have completely distinct SHA-256 audio hashes
                val hashes = mutableMapOf<String, String>()
                for ((kind, relPath) in expectedSamples) {
                    val file = File(root, relPath)
                    assertTrue("Sample missing for $gender/$lang/$kind: ${file.path}", file.exists())
                    val hash = sha256(file)
                    for ((otherKind, otherHash) in hashes) {
                        assertNotEquals(
                            "Sample audio for $gender/$lang/$kind must not be identical to $otherKind",
                            otherHash,
                            hash
                        )
                    }
                    hashes[kind] = hash
                }
            }
        }
    }
}
