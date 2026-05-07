package com.whereduck.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.whereduck.app.MainActivity
import com.whereduck.app.data.remote.FirestoreDataSource
import com.whereduck.app.ui.incoming.IncomingAlertActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StarnazzoFcmService : FirebaseMessagingService() {

    @Inject lateinit var firestoreDataSource: FirestoreDataSource
    @Inject lateinit var auth: FirebaseAuth

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = auth.currentUser?.uid ?: return
        serviceScope.launch {
            try {
                firestoreDataSource.updateFcmToken(userId, token)
            } catch (_: Exception) { }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        android.util.Log.d("WTD", "onMessageReceived: ${message.data}")

        val data = message.data
        val type = data["type"] ?: return

        when (type) {
            "starnazzo" -> handleStarnazzo(data)
            "starnazzo_response" -> showGenericNotification(data)
            "group_invite" -> showInviteNotification(data)
            "invite_accepted", "invite_rejected" -> showGenericNotification(data)
        }
    }

    private fun isUserMuted(fromUserId: String): Boolean {
        if (fromUserId.isBlank()) return false
        val prefs = getSharedPreferences("mute_prefs", Context.MODE_PRIVATE)
        val muteUntil = prefs.getLong("mute_$fromUserId", 0L)
        if (muteUntil == 0L) return false
        if (System.currentTimeMillis() >= muteUntil) {
            // Mute expired, clean up
            prefs.edit().remove("mute_$fromUserId").apply()
            return false
        }
        return true
    }

    private fun handleStarnazzo(data: Map<String, String>) {
        val fromName = data["fromDisplayName"] ?: "Qualcuno"
        val fromUserId = data["fromUserId"] ?: ""
        val levelKey = data["level"] ?: "medium"
        val animal = data["animalType"] ?: "duck"
        val alertId = data["alertId"] ?: ""

        // Check if this user is muted
        if (isUserMuted(fromUserId)) {
            android.util.Log.d("WTD", "Starnazzo from $fromName ignored (muted)")
            return
        }

        // 1. Start sound service
        val soundIntent = Intent(this, StarnazzoSoundService::class.java).apply {
            putExtra(StarnazzoSoundService.EXTRA_LEVEL, levelKey)
            putExtra(StarnazzoSoundService.EXTRA_FROM_NAME, fromName)
        }
        startForegroundService(soundIntent)

        // 2. Full-screen alert activity
        val alertActivityIntent = Intent(this, IncomingAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("fromName", fromName)
            putExtra("fromUserId", fromUserId)
            putExtra("level", levelKey)
            putExtra("alertId", alertId)
        }
        startActivity(alertActivityIntent)

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 1, alertActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Notification action buttons
        val notifId = System.currentTimeMillis().toInt()

        fun actionPendingIntent(action: String, requestCode: Int): PendingIntent {
            val intent = Intent(this, StarnazzoActionReceiver::class.java).apply {
                this.action = action
                putExtra(StarnazzoActionReceiver.EXTRA_ALERT_ID, alertId)
                putExtra(StarnazzoActionReceiver.EXTRA_FROM_USER_ID, fromUserId)
                putExtra(StarnazzoActionReceiver.EXTRA_NOTIF_ID, notifId)
            }
            return PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val arrivoPi = actionPendingIntent(StarnazzoActionReceiver.ACTION_ARRIVO, 10)
        val mutePi = actionPendingIntent(StarnazzoActionReceiver.ACTION_MUTE, 11)
        val dismissPi = actionPendingIntent(StarnazzoActionReceiver.ACTION_DISMISS, 12)

        // 4. Build notification with action buttons + swipe to silence
        val notification = NotificationCompat.Builder(this, "starnazzo_v2")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("STARNAZZO!")
            .setContentText("$fromName ti sta starnazzando!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "ARRIVO!", arrivoPi)
            .addAction(0, "MUTO 1min", mutePi)
            .addAction(0, "Chiudi", dismissPi)
            .setDeleteIntent(dismissPi) // swipe = silenzia
            .setAutoCancel(true)
            .setSound(null)
            .setVibrate(null)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notifId, notification)
    }

    private fun showInviteNotification(data: Map<String, String>) {
        val groupName = data["groupName"] ?: "un gruppo"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "general")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nuovo invito!")
            .setContentText("Sei stato invitato in $groupName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showGenericNotification(data: Map<String, String>) {
        val title = data["title"] ?: "WhereTheDuck"
        val body = data["body"] ?: ""

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "general")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
