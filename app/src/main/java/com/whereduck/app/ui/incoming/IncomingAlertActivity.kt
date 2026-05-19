package com.whereduck.app.ui.incoming

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.data.remote.CloudFunctionsDataSource
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        val fromName = intent.getStringExtra("fromName") ?: "Qualcuno"
        val levelKey = intent.getStringExtra("level") ?: "medium"
        val level = StarnazzoLevel.fromKey(levelKey)
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

    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        silenceAndFinish()
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }
}
