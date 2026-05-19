package com.whereduck.app.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.whereduck.app.data.remote.CloudFunctionsDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StarnazzoActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_OK = "com.whereduck.ACTION_OK"
        const val ACTION_MUTE = "com.whereduck.ACTION_MUTE"
        const val ACTION_REVENGE = "com.whereduck.ACTION_REVENGE"
        const val EXTRA_ALERT_ID = "extra_alert_id"
        const val EXTRA_FROM_USER_ID = "extra_from_user_id"
        const val EXTRA_NOTIF_ID = "extra_notif_id"
    }

    @Inject lateinit var cloudFunctions: CloudFunctionsDataSource

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val alertId = intent.getStringExtra(EXTRA_ALERT_ID) ?: ""
        val fromUserId = intent.getStringExtra(EXTRA_FROM_USER_ID) ?: ""
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, 0)

        // Stop sound
        val stopIntent = Intent(context, StarnazzoSoundService::class.java).apply {
            action = StarnazzoSoundService.ACTION_STOP
        }
        context.startService(stopIntent)

        // Dismiss notification
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(notifId)

        when (intent.action) {
            ACTION_OK -> {
                respondToAlert(alertId, "ok")
            }
            ACTION_MUTE -> {
                // Mute for 5 minutes from notification
                if (fromUserId.isNotBlank()) {
                    val prefs = context.getSharedPreferences("mute_prefs", Context.MODE_PRIVATE)
                    val muteUntil = System.currentTimeMillis() + (5 * 60 * 1000L)
                    prefs.edit().putLong("mute_$fromUserId", muteUntil).apply()
                }
                respondToAlert(alertId, "muto", 5)
            }
            ACTION_REVENGE -> {
                respondToAlert(alertId, "revenge")
                revengeStarnazzo(alertId)
            }
        }
    }

    private fun respondToAlert(alertId: String, response: String, muteDuration: Int? = null) {
        if (alertId.isBlank()) return
        scope.launch {
            try {
                cloudFunctions.respondStarnazzo(alertId, response, muteDuration)
            } catch (e: Exception) {
                android.util.Log.e("WTD", "Failed to respond: ${e.message}")
            }
        }
    }

    private fun revengeStarnazzo(alertId: String) {
        if (alertId.isBlank()) return
        scope.launch {
            try {
                cloudFunctions.revengeStarnazzo(alertId)
            } catch (e: Exception) {
                android.util.Log.e("WTD", "Failed to revenge: ${e.message}")
            }
        }
    }
}
