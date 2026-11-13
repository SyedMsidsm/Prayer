package com.syedmsidsm.prayerreminder.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioSamplePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var playJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<String?>(null)
    val currentTrack: StateFlow<String?> = _currentTrack.asStateFlow()

    private val _playingKind = MutableStateFlow<String?>(null)
    val playingKind: StateFlow<String?> = _playingKind.asStateFlow()

    fun stop() {
        playJob?.cancel()
        playJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
        _currentTrack.value = null
        _playingKind.value = null
    }

    fun playSample(
        language: String,
        gender: String,
        kind: String, // "before_azan", "at_azan", "before_jamaat", "jumuah_before_azan"
        minutes: Int = 10,
        chime: Boolean = true
    ) {
        stop()
        playJob = scope.launch {
            _isPlaying.value = true
            _currentTrack.value = "speech_$kind"
            _playingKind.value = kind
            try {
                if (chime) {
                    playAssetFile("audio/samples/chime.mp3")
                    delay(250)
                }

                val clipPath = resolveClipPath(language, gender, kind, minutes)
                if (clipPath != null) {
                    playAssetFile(clipPath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isPlaying.value = false
                _currentTrack.value = null
                _playingKind.value = null
            }
        }
    }

    fun playAzanSample(reciterId: String) {
        stop()
        playJob = scope.launch {
            _isPlaying.value = true
            _currentTrack.value = "azan_$reciterId"
            _playingKind.value = "azan_$reciterId"
            try {
                val clipPath = "audio/azan/${reciterId}_sample.mp3"
                if (assetExists(clipPath)) {
                    playAssetFile(clipPath)
                } else {
                    val fullPath = "audio/azan/${reciterId}_full.mp3"
                    if (assetExists(fullPath)) playAssetFile(fullPath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isPlaying.value = false
                _currentTrack.value = null
                _playingKind.value = null
            }
        }
    }

    fun playDuaSample() {
        stop()
        playJob = scope.launch {
            _isPlaying.value = true
            _currentTrack.value = "dua_after_azan"
            _playingKind.value = "dua_after_azan"
            try {
                val clipPath = "audio/azan/dua_after_azan.mp3"
                if (assetExists(clipPath)) {
                    playAssetFile(clipPath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isPlaying.value = false
                _currentTrack.value = null
                _playingKind.value = null
            }
        }
    }

    private suspend fun playAssetFile(path: String) {
        var afd: AssetFileDescriptor? = null
        try {
            afd = context.assets.openFd(path)
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
            }
            mediaPlayer = mp
            val completionDeferred = CompletableDeferred<Unit>()
            mp.setOnCompletionListener {
                completionDeferred.complete(Unit)
            }
            mp.setOnErrorListener { _, _, _ ->
                completionDeferred.complete(Unit)
                true
            }
            mp.start()
            completionDeferred.await()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {}
            mediaPlayer = null
            try {
                afd?.close()
            } catch (_: Exception) {}
        }
    }

    fun resolveClipPath(
        language: String,
        gender: String,
        kind: String,
        minutes: Int
    ): String? {
        val lang = when (language.lowercase()) {
            "ar" -> "ar"
            "en" -> "en"
            else -> "ur"
        }

        val prayer = if (kind == "jumuah_before_azan") "jumuah" else "asr"
        val min = when (kind) {
            "before_azan" -> if (minutes > 0) minutes else 10
            "jumuah_before_azan" -> if (minutes > 0) minutes else 30
            "before_jamaat" -> if (minutes > 0) minutes else 5
            else -> null
        }

        val filename = when (kind) {
            "before_azan" -> "before_azan_${prayer}_${min}.mp3"
            "jumuah_before_azan" -> "before_azan_jumuah_${min}.mp3"
            "at_azan" -> "at_azan_${prayer}.mp3"
            "before_jamaat" -> "before_jamaat_${prayer}_${min}.mp3"
            else -> "before_azan_asr_10.mp3"
        }

        if (gender.lowercase() == "female") {
            val femalePackPath = "audio/voice-pack-female/$lang/$filename"
            if (assetExists(femalePackPath)) {
                return femalePackPath
            }
            val femaleDefaultPackPath = "audio/voice-pack-female/$lang/before_azan_asr_10.mp3"
            if (assetExists(femaleDefaultPackPath)) {
                return femaleDefaultPackPath
            }
            val femaleAsset = "audio/samples/female_${lang}_before_azan_asr_10.mp3"
            if (assetExists(femaleAsset)) {
                return femaleAsset
            }
        }

        // Male preview pack
        val assetPath = "audio/voice-pack-preview/$lang/$filename"
        return if (assetExists(assetPath)) assetPath else "audio/voice-pack-preview/$lang/before_azan_asr_10.mp3"
    }

    private fun assetExists(path: String): Boolean {
        return try {
            context.assets.openFd(path).close()
            true
        } catch (_: Exception) {
            false
        }
    }
}
