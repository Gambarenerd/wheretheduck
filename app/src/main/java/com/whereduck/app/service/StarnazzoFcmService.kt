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
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.Lifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StarnazzoFcmService : FirebaseMessagingService() {

    companion object {
        const val ACTION_CANCEL_STARNAZZO = "com.whereduck.CANCEL_STARNAZZO"
    }

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
            "starnazzo_cancel" -> handleStarnazzoCancel()
            "starnazzo_response" -> showGenericNotification(data)
            "contact_invite" -> showInviteNotification(data)
            "contact_accepted", "contact_rejected" -> showGenericNotification(data)
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

    private fun handleStarnazzoCancel() {
        // Stop sound service
        val stopIntent = Intent(this, StarnazzoSoundService::class.java).apply {
            action = StarnazzoSoundService.ACTION_STOP
        }
        startService(stopIntent)

        // Tell IncomingAlertActivity to close
        val closeIntent = Intent(ACTION_CANCEL_STARNAZZO)
        sendBroadcast(closeIntent)

        // Dismiss any starnazzo notifications
        val manager = getSystemService(NotificationManager::class.java)
        manager.cancelAll()
    }

    private fun handleStarnazzo(data: Map<String, String>) {
        val fromName = data["fromDisplayName"] ?: "Qualcuno"
        val fromUserId = data["fromUserId"] ?: ""
        val levelKey = data["level"] ?: "medium"
        val animal = data["animalType"] ?: "duck"
        val alertId = data["alertId"] ?: ""
        val fromPhotoUrl = data["fromPhotoUrl"] ?: ""
        val isRevenge = data["isRevenge"] == "true"

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
            putExtra("fromPhotoUrl", fromPhotoUrl)
            putExtra("isRevenge", isRevenge)
        }
        startActivity(alertActivityIntent)

        // Skip notification if app is in foreground (full-screen alert is enough)
        val isInForeground = ProcessLifecycleOwner.get().lifecycle.currentState
            .isAtLeast(Lifecycle.State.RESUMED)
        if (isInForeground) return

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

        val okPi = actionPendingIntent(StarnazzoActionReceiver.ACTION_OK, 10)
        val mutePi = actionPendingIntent(StarnazzoActionReceiver.ACTION_MUTE, 11)
        val revengePi = actionPendingIntent(StarnazzoActionReceiver.ACTION_REVENGE, 12)

        val title = if (isRevenge) "REVENGE STARNAZZO!" else "STARNAZZO!"
        val text = if (isRevenge) "$fromName ti ha restituito lo starnazzo!"
                   else "$fromName ti sta starnazzando!"

        // 4. Build notification with action buttons (OK! / Non mi rompere / Revenge!)
        val builder = NotificationCompat.Builder(this, "starnazzo_v2")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "OK!", okPi)
            .addAction(0, "Non mi rompere", mutePi)

        // Revenge only if not already a revenge (no chains)
        if (!isRevenge) {
            builder.addAction(0, "Revenge!", revengePi)
        }

        val notification = builder
            .setDeleteIntent(okPi) // swipe = ok
            .setAutoCancel(true)
            .setSound(null)
            .setVibrate(null)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notifId, notification)
    }

    private fun showInviteNotification(data: Map<String, String>) {
        val fromName = data["fromDisplayName"] ?: "Qualcuno"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "general")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nuovo contatto!")
            .setContentText("$fromName vuole aggiungerti come contatto")
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
