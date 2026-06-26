package com.whereduck.app.ui.incoming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.whereduck.app.data.model.AnimalRegistry
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.data.remote.CloudFunctionsDataSource
import com.whereduck.app.service.StarnazzoFcmService
import com.whereduck.app.service.StarnazzoSoundService
import com.whereduck.app.ui.theme.WhereTheDuckTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IncomingAlertActivity : ComponentActivity() {

    @Inject lateinit var cloudFunctions: CloudFunctionsDataSource

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            silenceAndFinish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        // Listen for cancel from sender
        val filter = IntentFilter(StarnazzoFcmService.ACTION_CANCEL_STARNAZZO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(cancelReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(cancelReceiver, filter)
        }

        val fromName = intent.getStringExtra("fromName") ?: "Qualcuno"
        val levelKey = intent.getStringExtra("level") ?: "medium"
        val level = StarnazzoLevel.fromKey(levelKey)
        val animalType = intent.getStringExtra("animalType") ?: level.defaultAnimal
        val animalEmoji = AnimalRegistry.getEmoji(animalType, level)
        val fromUserId = intent.getStringExtra("fromUserId") ?: ""
        val alertId = intent.getStringExtra("alertId") ?: ""
        val fromPhotoUrl = intent.getStringExtra("fromPhotoUrl") ?: ""
        val isRevenge = intent.getBooleanExtra("isRevenge", false)

        setContent {
            WhereTheDuckTheme {
                IncomingAlertScreen(
                    fromName = fromName,
                    fromPhotoUrl = fromPhotoUrl,
                    level = level,
                    animalKey = animalType,
                    animalEmoji = animalEmoji,
                    isRevenge = isRevenge,
                    onOk = {
                        respondToStarnazzo(alertId, "ok")
                        silenceAndFinish()
                    },
                    onMute = { minutes ->
                        muteUser(fromUserId, minutes)
                        respondToStarnazzo(alertId, "muto", minutes)
                        silenceAndFinish()
                    },
                    onRevenge = {
                        respondToStarnazzo(alertId, "revenge")
                        revengeStarnazzo(alertId)
                        silenceAndFinish()
                    },
                    onDismiss = {
                        respondToStarnazzo(alertId, "dismissed")
                        silenceAndFinish()
                    }
                )
            }
        }
    }

    private fun respondToStarnazzo(alertId: String, response: String, muteDuration: Int? = null) {
        if (alertId.isBlank()) return
        scope.launch {
            try {
                cloudFunctions.respondStarnazzo(alertId, response, muteDuration)
            } catch (e: Exception) {
                android.util.Log.e("WTD", "Failed to respond to starnazzo", e)
            }
        }
    }

    private fun revengeStarnazzo(alertId: String) {
        if (alertId.isBlank()) return
        scope.launch {
            try {
                cloudFunctions.revengeStarnazzo(alertId)
            } catch (e: Exception) {
                android.util.Log.e("WTD", "Failed to revenge starnazzo", e)
            }
        }
    }

    private fun muteUser(fromUserId: String, minutes: Int) {
        if (fromUserId.isBlank()) return
        val prefs = getSharedPreferences("mute_prefs", Context.MODE_PRIVATE)
        val muteUntil = System.currentTimeMillis() + (minutes * 60 * 1000L)
        prefs.edit().putLong("mute_$fromUserId", muteUntil).apply()
    }

    private fun silenceAndFinish() {
        val stopIntent = Intent(this, StarnazzoSoundService::class.java).apply {
            action = StarnazzoSoundService.ACTION_STOP
        }
        startService(stopIntent)
        finish()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(cancelReceiver)
        } catch (_: Exception) { }
        super.onDestroy()
    }

    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        silenceAndFinish()
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }
}
