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
        handler.removeCallbacksAndMessages(null)

        val levelKey = intent?.getStringExtra(EXTRA_LEVEL) ?: "medium"
        val level = StarnazzoLevel.fromKey(levelKey)
        val fromName = intent?.getStringExtra(EXTRA_FROM_NAME) ?: "Qualcuno"

        isPlaying = true
        startForeground(NOTIF_ID, buildNotification(fromName, level))
        acquireWakeLock()
        forceMaxVolume()

        // Phase 1 (0s): Only screen/notification — nothing else
        // Phase 2 (3s): Short vibrations, one per second for 3 seconds
        handler.postDelayed({
            if (!isPlaying) return@postDelayed
            vibrateShort()
        }, 3_000)
        handler.postDelayed({
            if (!isPlaying) return@postDelayed
            vibrateShort()
        }, 4_000)
        handler.postDelayed({
            if (!isPlaying) return@postDelayed
            vibrateShort()
        }, 5_000)

        // Phase 3 (6s): 3 long vibrations only
        handler.postDelayed({
            if (!isPlaying) return@postDelayed
            startLongVibrations()
        }, 6_000)

        // Phase 4 (9s): Long vibrations + sound
        handler.postDelayed({
            if (!isPlaying) return@postDelayed
            startLongVibrationsWithSound(level)
        }, 9_000)

        // Auto-stop after 30 seconds
        handler.postDelayed({ stopAndCleanup() }, 30_000)

        return START_NOT_STICKY
    }

    private fun initVibrator(): Vibrator {
        if (vibrator == null) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        }
        return vibrator!!
    }

    private fun vibrateShort() {
        val v = initVibrator()
        v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun startLongVibrations() {
        // 3 long vibrations only
        val pattern = longArrayOf(
            0, 600, 400,   // 1st
            600, 400,      // 2nd
            600             // 3rd
        )
        val v = initVibrator()
        v.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    private fun startLongVibrationsWithSound(level: StarnazzoLevel) {
        // Cancel previous vibration, start new continuous pattern + sound
        vibrator?.cancel()
        val pattern = longArrayOf(
            0, 800, 200,
            800, 200,
            800, 200,
            800, 200,
            800, 200,
            800, 200
        )
        val v = initVibrator()
        v.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 = repeat

        playTone(level)
    }

    private fun buildNotification(fromName: String, level: StarnazzoLevel): Notification {
        val stopIntent = Intent(this, StarnazzoSoundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "starnazzo_service")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${level.emoji} STARNAZZO in corso!")
            .setContentText("$fromName ti sta starnazzando!")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "SILENZIA", stopPendingIntent)
            .setSilent(true)
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

                // Play tone in a loop for remaining time (~21 seconds)
                var repeats = 0
                val maxRepeats = 21000 / (durationMs + pauseMs)
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
