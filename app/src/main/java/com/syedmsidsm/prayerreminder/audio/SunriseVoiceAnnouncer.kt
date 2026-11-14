package com.syedmsidsm.prayerreminder.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

object SunriseVoiceAnnouncer {

    fun getSunriseText(language: String): String {
        return when (language.lowercase()) {
            "ar" -> "دخل وقت الشروق"
            "ur" -> "سورج نکلنے کا وقت ہو گیا ہے"
            else -> "It's time for sunrise"
        }
    }

    suspend fun speakSunrise(
        context: Context,
        language: String,
        gender: String = "male",
        usage: Int = AudioAttributes.USAGE_MEDIA
    ) {
        val ttsDeferred = CompletableDeferred<TextToSpeech?>()
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsDeferred.complete(ttsInstance)
            } else {
                ttsDeferred.complete(null)
            }
        }
        val tts = ttsDeferred.await() ?: return
        try {
            val targetLocale = when (language.lowercase()) {
                "ar" -> Locale("ar")
                "ur" -> Locale("ur")
                else -> Locale.ENGLISH
            }
            val isLangAvailable = tts.isLanguageAvailable(targetLocale)
            if (isLangAvailable >= TextToSpeech.LANG_AVAILABLE) {
                tts.language = targetLocale
            } else {
                tts.language = Locale.ENGLISH
            }

            // Apply Samsung and Google TTS male/female matching & pitch styling
            TtsVoiceHelper.applyVoiceAndPitch(tts, targetLocale, gender)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(usage)
                .build()
            tts.setAudioAttributes(audioAttributes)

            val utteranceId = "sunrise_utterance"
            val doneDeferred = CompletableDeferred<Unit>()
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    doneDeferred.complete(Unit)
                }
                override fun onError(utteranceId: String?) {
                    doneDeferred.complete(Unit)
                }
            })

            val params = Bundle()
            val textToSpeak = getSunriseText(language)
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            withTimeoutOrNull(6000L) {
                doneDeferred.await()
            }
        } catch (e: Exception) {
            Log.e("SunriseVoiceAnnouncer", "Error speaking sunrise announcement", e)
        } finally {
            try {
                tts.stop()
                tts.shutdown()
            } catch (_: Exception) {}
        }
    }
}
