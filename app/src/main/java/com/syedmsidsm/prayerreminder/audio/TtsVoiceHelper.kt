package com.syedmsidsm.prayerreminder.audio

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

/**
 * The app speaks with a male voice only.
 *
 * Sunrise and "after prayer" announcements use the phone's text-to-speech engine, so we
 * pick a male system voice when the phone has one and always use a deep, calm delivery.
 */
object TtsVoiceHelper {

    // "male" as its own word: matches "#male_1-local" or "_male", but NOT "female".
    // (A plain contains("male") also matches "female", which made the old code pick female voices.)
    private val MALE_WORD = Regex("(^|[^a-z])male")
    private val FEMALE_WORD = Regex("female")

    private fun looksMale(text: String): Boolean {
        val t = text.lowercase()
        return MALE_WORD.containsMatchIn(t) ||
            t.contains("_m0") || t.contains("-m-") || t.contains("_m_") || t.endsWith("_m")
    }

    private fun looksFemale(text: String): Boolean {
        val t = text.lowercase()
        return FEMALE_WORD.containsMatchIn(t) ||
            t.contains("_f0") || t.contains("-f-") || t.contains("_f_") || t.endsWith("_f")
    }

    private fun isMale(v: Voice): Boolean =
        looksMale(v.name) || v.features?.any { looksMale(it) } == true

    private fun isFemale(v: Voice): Boolean =
        looksFemale(v.name) || v.features?.any { looksFemale(it) } == true

    fun applyMaleVoiceAndPitch(
        tts: TextToSpeech,
        targetLocale: Locale
    ) {
        try {
            val allVoices = tts.voices
            if (!allVoices.isNullOrEmpty()) {
                val localeVoices = allVoices.filter {
                    it.locale.language.equals(targetLocale.language, ignoreCase = true)
                }

                // 1) a voice that says it is male, 2) any voice that is not female, 3) whatever exists
                val chosenVoice = localeVoices.firstOrNull { isMale(it) && !isFemale(it) }
                    ?: localeVoices.firstOrNull { !isFemale(it) }
                    ?: localeVoices.firstOrNull()

                if (chosenVoice != null) {
                    tts.voice = chosenVoice
                }
            }
        } catch (_: Exception) {}

        // Deep, calm male delivery
        tts.setPitch(0.76f)
        tts.setSpeechRate(0.92f)
    }
}
