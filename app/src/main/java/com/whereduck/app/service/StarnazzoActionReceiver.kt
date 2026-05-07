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
        const val ACTION_ARRIVO = "com.whereduck.ACTION_ARRIVO"
        const val ACTION_DISMISS = "com.whereduck.ACTION_DISMISS"
        const val ACTION_MUTE = "com.whereduck.ACTION_MUTE"
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
            ACTION_ARRIVO -> {
                respondToAlert(alertId, "arrivo")
            }
            ACTION_MUTE -> {
                // Mute for 1 minute
                if (fromUserId.isNotBlank()) {
                    val prefs = context.getSharedPreferences("mute_prefs", Context.MODE_PRIVATE)
                    val muteUntil = System.currentTimeMillis() + (1 * 60 * 1000L)
                    prefs.edit().putLong("mute_$fromUserId", muteUntil).apply()
                }
                respondToAlert(alertId, "muto", 1)
            }
            ACTION_DISMISS -> {
                respondToAlert(alertId, "dismissed")
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
}
