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

                if (kind.contains("sunrise")) {
                    SunriseVoiceAnnouncer.speakSunrise(context, language, gender)
                } else if (kind == "after_prayer") {
                    PostPrayerVoiceAnnouncer.speakPostPrayer(context, language, gender, minutes)
                } else {
                    val clipPath = resolveClipPath(language, gender, kind, minutes)
                    if (clipPath != null) {
                        playAssetFile(clipPath)
                    }
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

    fun playVoicePreview(
        language: String,
        gender: String
    ) {
        val targetKind = "voice_${gender.lowercase()}"
        if (_isPlaying.value && _playingKind.value == targetKind) {
            stop()
            return
        }
        stop()
        playJob = scope.launch {
            _isPlaying.value = true
            _currentTrack.value = targetKind
            _playingKind.value = targetKind
            try {
                val clipPath = resolveClipPath(
                    language = language,
                    gender = gender,
                    kind = "before_azan",
                    minutes = 10
                )
                if (clipPath != null) {
                    playAssetFile(clipPath)
                }
            } catch (e: Exception) {
                android.util.Log.e("AudioSamplePlayer", "Error playing voice preview for $gender", e)
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
                playAssetFile(clipPath)
            } catch (e: Exception) {
                android.util.Log.e("AudioSamplePlayer", "Error playing dua sample", e)
            } finally {
                _isPlaying.value = false
                _currentTrack.value = null
                _playingKind.value = null
            }
        }
    }

    private suspend fun playAssetFile(path: String) {
        var afd: AssetFileDescriptor? = null
        var tempFile: java.io.File? = null
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }

            try {
                afd = context.assets.openFd(path)
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                mp.prepare()
            } catch (e: Exception) {
                android.util.Log.w("AudioSamplePlayer", "openFd failed for $path ($e), extracting to temp cache file")
                val safeName = "sample_" + path.replace("/", "_")
                val target = java.io.File(context.cacheDir, safeName)
                context.assets.open(path).use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile = target
                mp.reset()
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mp.setDataSource(target.absolutePath)
                mp.prepare()
            }

            mediaPlayer = mp
            val completionDeferred = CompletableDeferred<Unit>()
            mp.setOnCompletionListener {
                completionDeferred.complete(Unit)
            }
            mp.setOnErrorListener { _, what, extra ->
                android.util.Log.e("AudioSamplePlayer", "MediaPlayer error: what=$what extra=$extra")
                completionDeferred.complete(Unit)
                true
            }
            mp.start()
            completionDeferred.await()
        } catch (e: Exception) {
            android.util.Log.e("AudioSamplePlayer", "Error playing asset: $path", e)
        } finally {
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {}
            mediaPlayer = null
            try {
                afd?.close()
            } catch (_: Exception) {}
            try {
                tempFile?.delete()
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

        val prayer = when {
            kind == "jumuah_before_azan" || kind.contains("jumuah") -> "jumuah"
            kind.contains("sunrise") -> "sunrise"
            else -> "asr"
        }

        if (prayer == "sunrise" || kind == "after_prayer") {
            return null
        }

        val min = when (kind) {
            "before_azan", "jumuah_before_azan" -> {
                val supported = listOf(30, 20, 15, 10, 5)
                supported.firstOrNull { it <= minutes } ?: 5
            }
            "before_jamaat" -> {
                val supported = listOf(15, 10, 5, 2)
                supported.firstOrNull { it <= minutes } ?: 2
            }
            else -> null
        }

        val filename = when (kind) {
            "before_azan", "jumuah_before_azan" -> "before_azan_${prayer}_${min}.mp3"
            "at_azan" -> "at_azan_${prayer}.mp3"
            "before_jamaat" -> "before_jamaat_${prayer}_${min}.mp3"
            else -> "before_azan_${prayer}_10.mp3"
        }

        val isFemale = gender.lowercase() == "female"
        val folder = if (isFemale) "voice-pack-female" else "voice-pack-preview"

        val packPath = "audio/$folder/$lang/$filename"
        if (assetExists(packPath)) {
            return packPath
        }

        val defaultMinute = if (kind == "before_jamaat") 5 else 10
        val fallbackMinutePath = "audio/$folder/$lang/${if (kind == "before_jamaat") "before_jamaat_${prayer}_$defaultMinute.mp3" else "before_azan_${prayer}_$defaultMinute.mp3"}"
        if (assetExists(fallbackMinutePath)) {
            return fallbackMinutePath
        }

        val prayerFallback = "audio/$folder/$lang/at_azan_${prayer}.mp3"
        if (assetExists(prayerFallback)) {
            return prayerFallback
        }

        return "audio/samples/chime.mp3"
    }

    private fun assetExists(path: String): Boolean {
        return try {
            context.assets.openFd(path).close()
            true
        } catch (_: Exception) {
            try {
                context.assets.open(path).close()
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}
