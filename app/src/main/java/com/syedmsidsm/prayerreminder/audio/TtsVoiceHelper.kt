package com.syedmsidsm.prayerreminder.audio

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

object TtsVoiceHelper {

    fun applyVoiceAndPitch(
        tts: TextToSpeech,
        targetLocale: Locale,
        gender: String
    ) {
        val isFemale = gender.lowercase() == "female"
        try {
            val allVoices = tts.voices
            if (!allVoices.isNullOrEmpty()) {
                val localeVoices = allVoices.filter {
                    it.locale.language.equals(targetLocale.language, ignoreCase = true)
                }

                fun isVoiceExplicitlyFemale(v: Voice): Boolean {
                    val n = v.name.lowercase()
                    return n.contains("female") ||
                            n.contains("_f0") ||
                            n.contains("-f-") ||
                            n.contains("_f_") ||
                            n.endsWith("_f") ||
                            v.features?.any { it.contains("female", ignoreCase = true) } == true
                }

                fun isVoiceExplicitlyMale(v: Voice): Boolean {
                    val n = v.name.lowercase()
                    return n.contains("male") ||
                            n.contains("_m0") ||
                            n.contains("-m-") ||
                            n.contains("_m_") ||
                            n.endsWith("_m") ||
                            v.features?.any { it.contains("male", ignoreCase = true) } == true
                }

                val chosenVoice = if (isFemale) {
                    localeVoices.firstOrNull { isVoiceExplicitlyFemale(it) }
                        ?: localeVoices.firstOrNull { !isVoiceExplicitlyMale(it) }
                        ?: localeVoices.firstOrNull()
                } else {
                    // Male: Prioritize male voice, then non-female, then any
                    localeVoices.firstOrNull { isVoiceExplicitlyMale(it) }
                        ?: localeVoices.firstOrNull { !isVoiceExplicitlyFemale(it) }
                        ?: localeVoices.firstOrNull()
                }

                if (chosenVoice != null) {
                    tts.voice = chosenVoice
                }
            }
        } catch (_: Exception) {}

        // Enforce unmistakable pitch distinction for male vs female
        if (isFemale) {
            tts.setPitch(1.15f)
            tts.setSpeechRate(1.0f)
        } else {
            // Authoritative, deep masculine pitch and cadence
            tts.setPitch(0.76f)
            tts.setSpeechRate(0.92f)
        }
    }
}
