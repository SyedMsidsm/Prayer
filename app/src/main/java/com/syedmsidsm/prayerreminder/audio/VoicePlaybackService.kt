package com.syedmsidsm.prayerreminder.audio

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.syedmsidsm.prayerreminder.notifications.NotificationHelper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class VoicePlaybackService : Service() {

    companion object {
        const val FOREGROUND_NOTIF_ID = 999991

        const val EXTRA_LANGUAGE = "extra_language"
        const val EXTRA_GENDER = "extra_gender"
        const val EXTRA_KIND = "extra_kind"
        const val EXTRA_PRAYER = "extra_prayer"
        const val EXTRA_MINUTES = "extra_minutes"
        const val EXTRA_CHIME = "extra_chime"
        const val EXTRA_OVERRIDE_SILENT = "extra_override_silent"
        const val EXTRA_AT_AZAN_TYPE = "extra_at_azan_type"
        const val EXTRA_AZAN_RECITER = "extra_azan_reciter"
        const val EXTRA_AZAN_LENGTH = "extra_azan_length"
        const val EXTRA_PLAY_DUA = "extra_play_dua"

        fun start(
            context: Context,
            language: String,
            gender: String,
            kind: String,
            prayer: String,
            minutes: Int,
            chime: Boolean,
            overrideSilent: Boolean,
            atAzanType: String = "both",
            azanReciter: String = "ali_mala",
            azanLength: String = "full",
            playDua: Boolean = true
        ) {
            val intent = Intent(context, VoicePlaybackService::class.java).apply {
                putExtra(EXTRA_LANGUAGE, language)
                putExtra(EXTRA_GENDER, gender)
                putExtra(EXTRA_KIND, kind)
                putExtra(EXTRA_PRAYER, prayer)
                putExtra(EXTRA_MINUTES, minutes)
                putExtra(EXTRA_CHIME, chime)
                putExtra(EXTRA_OVERRIDE_SILENT, overrideSilent)
                putExtra(EXTRA_AT_AZAN_TYPE, atAzanType)
                putExtra(EXTRA_AZAN_RECITER, azanReciter)
                putExtra(EXTRA_AZAN_LENGTH, azanLength)
                putExtra(EXTRA_PLAY_DUA, playDua)
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val notifHelper = NotificationHelper(this)
        startForeground(FOREGROUND_NOTIF_ID, notifHelper.buildForegroundServiceNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val language = intent.getStringExtra(EXTRA_LANGUAGE) ?: "ur"
        val gender = intent.getStringExtra(EXTRA_GENDER) ?: "male"
        val kind = intent.getStringExtra(EXTRA_KIND) ?: "before_azan"
        val prayer = intent.getStringExtra(EXTRA_PRAYER) ?: "asr"
        val minutes = intent.getIntExtra(EXTRA_MINUTES, 10)
        val chime = intent.getBooleanExtra(EXTRA_CHIME, true)
        val overrideSilent = intent.getBooleanExtra(EXTRA_OVERRIDE_SILENT, false)
        val atAzanType = intent.getStringExtra(EXTRA_AT_AZAN_TYPE) ?: "both"
        val azanReciter = intent.getStringExtra(EXTRA_AZAN_RECITER) ?: "ali_mala"
        val azanLength = intent.getStringExtra(EXTRA_AZAN_LENGTH) ?: "full"
        val playDua = intent.getBooleanExtra(EXTRA_PLAY_DUA, true)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // 1. In a call -> don't play
        if (audioManager.mode == AudioManager.MODE_IN_CALL || audioManager.mode == AudioManager.MODE_IN_COMMUNICATION) {
            stopSelf()
            return START_NOT_STICKY
        }

        // 2. Ringer mode check
        val isSilentOrVibrate = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT ||
                audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE

        if (isSilentOrVibrate && !overrideSilent) {
            stopSelf()
            return START_NOT_STICKY
        }

        val audioUsage = if (isSilentOrVibrate && overrideSilent) {
            AudioAttributes.USAGE_ALARM
        } else {
            AudioAttributes.USAGE_NOTIFICATION_EVENT
        }

        val timeoutMs = if (kind == "at_azan") 300000L else 20000L // 5 min for azan, 20s for short reminders

        serviceScope.launch {
            withTimeoutOrNull(timeoutMs) {
                if (kind == "at_azan") {
                    playAtAzanSequence(
                        audioManager = audioManager,
                        usage = audioUsage,
                        language = language,
                        gender = gender,
                        prayer = prayer,
                        chime = chime,
                        atAzanType = atAzanType,
                        azanReciter = azanReciter,
                        azanLength = azanLength,
                        playDua = playDua
                    )
                } else {
                    playVoiceSequence(audioManager, audioUsage, language, gender, kind, prayer, minutes, chime)
                }
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private suspend fun playAtAzanSequence(
        audioManager: AudioManager,
        usage: Int,
        language: String,
        gender: String,
        prayer: String,
        chime: Boolean,
        atAzanType: String,
        azanReciter: String,
        azanLength: String,
        playDua: Boolean
    ) {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(usage)
            .build()

        requestAudioFocus(audioManager, audioAttributes)

        try {
            // 1. Announcement first (if speech_only or both)
            if (atAzanType == "speech_only" || atAzanType == "both") {
                if (chime) {
                    playAssetFile("audio/samples/chime.mp3", audioAttributes)
                    delay(250)
                }
                val clipPath = resolveClipPath(language, gender, "at_azan", prayer, 0)
                if (clipPath != null) {
                    playAssetFile(clipPath, audioAttributes)
                }
                if (atAzanType == "both") {
                    delay(1000)
                }
            }

            // 2. Azan audio (if azan_only or both)
            if (atAzanType == "azan_only" || atAzanType == "both") {
                val suffix = if (azanLength == "four_lines") "short" else "full"
                val azanPath = "audio/azan/${azanReciter}_$suffix.mp3"
                if (assetExists(azanPath)) {
                    playAssetFile(azanPath, audioAttributes)
                } else {
                    val fallback = "audio/azan/${azanReciter}_full.mp3"
                    if (assetExists(fallback)) playAssetFile(fallback, audioAttributes)
                }

                // 3. Optional Dua after Azan
                if (playDua) {
                    delay(1500)
                    val duaPath = "audio/azan/dua_after_azan.mp3"
                    if (assetExists(duaPath)) {
                        playAssetFile(duaPath, audioAttributes)
                    }
                }
            }
        } finally {
            abandonAudioFocus(audioManager)
        }
    }

    private suspend fun playVoiceSequence(
        audioManager: AudioManager,
        usage: Int,
        language: String,
        gender: String,
        kind: String,
        prayer: String,
        minutes: Int,
        chime: Boolean
    ) {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(usage)
            .build()

        // Request audio focus with ducking
        requestAudioFocus(audioManager, audioAttributes)

        try {
            if (chime) {
                playAssetFile("audio/samples/chime.mp3", audioAttributes)
                delay(250)
            }

            val clipPath = resolveClipPath(language, gender, kind, prayer, minutes)
            if (clipPath != null) {
                playAssetFile(clipPath, audioAttributes)
            }
        } finally {
            abandonAudioFocus(audioManager)
        }
    }

    private fun requestAudioFocus(audioManager: AudioManager, attributes: AudioAttributes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attributes)
                .setOnAudioFocusChangeListener { /* handle focus changes */ }
                .build()
            audioFocusRequest = req
            audioManager.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                if (attributes.usage == AudioAttributes.USAGE_ALARM) AudioManager.STREAM_ALARM else AudioManager.STREAM_NOTIFICATION,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    private fun abandonAudioFocus(audioManager: AudioManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private suspend fun playAssetFile(path: String, attributes: AudioAttributes) {
        var afd: AssetFileDescriptor? = null
        try {
            afd = assets.openFd(path)
            val mp = MediaPlayer().apply {
                setAudioAttributes(attributes)
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

    private fun resolveClipPath(
        language: String,
        gender: String,
        kind: String,
        prayer: String,
        minutes: Int
    ): String? {
        val lang = when (language.lowercase()) {
            "ar" -> "ar"
            "en" -> "en"
            else -> "ur"
        }

        val prayerName = if (kind == "jumuah_before_azan") "jumuah" else prayer.lowercase()
        val min = when (kind) {
            "before_azan" -> if (minutes > 0) minutes else 10
            "jumuah_before_azan" -> if (minutes > 0) minutes else 30
            "before_jamaat" -> if (minutes > 0) minutes else 5
            else -> null
        }

        val filename = when (kind) {
            "before_azan" -> "before_azan_${prayerName}_${min}.mp3"
            "jumuah_before_azan" -> "before_azan_jumuah_${min}.mp3"
            "at_azan" -> "at_azan_${prayerName}.mp3"
            "before_jamaat" -> "before_jamaat_${prayerName}_${min}.mp3"
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
            assets.openFd(path).close()
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}
