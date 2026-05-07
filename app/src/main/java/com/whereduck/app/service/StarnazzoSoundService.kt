package com.whereduck.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.whereduck.app.data.model.StarnazzoLevel
import kotlin.math.sin

class StarnazzoSoundService : Service() {

    companion object {
        const val EXTRA_LEVEL = "extra_level"
        const val EXTRA_FROM_NAME = "extra_from_name"
        const val ACTION_STOP = "com.whereduck.STOP_STARNAZZO"
        private const val NOTIF_ID = 9999
        private const val SAMPLE_RATE = 44100
    }

    private var audioTrack: AudioTrack? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var originalVolume: Int = -1
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAndCleanup()
            return START_NOT_STICKY
        }

        // Stop any existing playback first
        stopPlayback()

        val levelKey = intent?.getStringExtra(EXTRA_LEVEL) ?: "medium"
        val level = StarnazzoLevel.fromKey(levelKey)
        val fromName = intent?.getStringExtra(EXTRA_FROM_NAME) ?: "Qualcuno"

        startForeground(NOTIF_ID, buildNotification(fromName, level))
        acquireWakeLock()
        forceMaxVolume()
        playTone(level)
        startVibration(level)

        // Auto-stop after 15 seconds
        handler.postDelayed({ stopAndCleanup() }, 15_000)

        return START_NOT_STICKY
    }

    private fun buildNotification(fromName: String, level: StarnazzoLevel): Notification {
        val stopIntent = Intent(this, StarnazzoSoundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "starnazzo_v2")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${level.emoji} STARNAZZO in corso!")
            .setContentText("$fromName ti sta starnazzando!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "SILENZIA", stopPendingIntent)
            .build()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WhereTheDuck::StarnazzoWakeLock"
        ).apply {
            acquire(30_000) // max 30s
        }
    }

    private fun forceMaxVolume() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
    }

    private fun restoreVolume() {
        if (originalVolume >= 0) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
            originalVolume = -1
        }
    }

    private fun playTone(level: StarnazzoLevel) {
        isPlaying = true
        val frequency = level.toneFrequency
        val durationMs = when (level) {
            StarnazzoLevel.LIGHT -> 200
            StarnazzoLevel.MEDIUM -> 400
            StarnazzoLevel.HEAVY -> 600
        }
        val pauseMs = when (level) {
            StarnazzoLevel.LIGHT -> 100
            StarnazzoLevel.MEDIUM -> 200
            StarnazzoLevel.HEAVY -> 150
        }

        Thread {
            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val numSamples = (SAMPLE_RATE * durationMs / 1000)
                val buffer = ShortArray(numSamples)
                for (i in buffer.indices) {
                    val angle = 2.0 * Math.PI * frequency * i / SAMPLE_RATE
                    buffer[i] = (sin(angle) * Short.MAX_VALUE).toInt().toShort()
                }

                val bufferSize = buffer.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(
                        android.media.AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                audioTrack = track

                // Play tone in a loop pattern
                var repeats = 0
                val maxRepeats = 15000 / (durationMs + pauseMs) // fill 15 seconds
                while (isPlaying && repeats < maxRepeats) {
                    track.reloadStaticData()
                    track.play()
                    Thread.sleep(durationMs.toLong())
                    track.stop()
                    if (isPlaying) Thread.sleep(pauseMs.toLong())
                    repeats++
                }
            } catch (_: Exception) {
            }
        }.start()
    }

    private fun startVibration(level: StarnazzoLevel) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator?.vibrate(
            VibrationEffect.createWaveform(level.vibrationPattern, 0) // 0 = repeat
        )
    }

    private fun stopPlayback() {
        isPlaying = false
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) { }
        audioTrack = null

        vibrator?.cancel()
        vibrator = null
    }

    private fun stopAndCleanup() {
        handler.removeCallbacksAndMessages(null)
        stopPlayback()
        restoreVolume()
        try {
            wakeLock?.release()
        } catch (_: Exception) { }
        wakeLock = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopAndCleanup()
        super.onDestroy()
    }
}
