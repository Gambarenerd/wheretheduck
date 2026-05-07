package com.whereduck.app.service

import android.app.NotificationManager
import android.app.PendingIntent
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
            "group_invite" -> showInviteNotification(data)
            "invite_accepted", "invite_rejected" -> showGenericNotification(data)
        }
    }

    private fun handleStarnazzo(data: Map<String, String>) {
        val fromName = data["fromDisplayName"] ?: "Qualcuno"
        val levelKey = data["level"] ?: "medium"
        val animal = data["animalType"] ?: "duck"

        // 1. Start sound service
        val soundIntent = Intent(this, StarnazzoSoundService::class.java).apply {
            putExtra(StarnazzoSoundService.EXTRA_LEVEL, levelKey)
            putExtra(StarnazzoSoundService.EXTRA_FROM_NAME, fromName)
        }
        startForegroundService(soundIntent)

        // 2. Full-screen intent for lock screen / DND bypass
        val fullScreenIntent = Intent(this, IncomingAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("fromName", fromName)
            putExtra("level", levelKey)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 1, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Content intent (tap notification)
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Stop action
        val stopIntent = Intent(this, StarnazzoSoundService::class.java).apply {
            action = StarnazzoSoundService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 5. Build notification
        val notification = NotificationCompat.Builder(this, "starnazzo_v2")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("STARNAZZO!")
            .setContentText("$fromName ti ha starnazzato! ($animal)")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_delete, "SILENZIA", stopPendingIntent)
            .setAutoCancel(true)
            .setSound(null)
            .setVibrate(null)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
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
