package com.whereduck.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WhereTheDuckApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        // Delete old channels if exist (settings are sticky)
        manager.deleteNotificationChannel("starnazzo")
        manager.deleteNotificationChannel("starnazzo_reminder")

        val starnazzoChannel = NotificationChannel(
            "starnazzo_v2",
            "Starnazzi",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifiche starnazzo"
            setBypassDnd(true)
            enableVibration(false)
            setSound(null, null)
        }

        val generalChannel = NotificationChannel(
            "general",
            "Generale",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifiche generali (inviti, ecc.)"
        }

        val serviceChannel = NotificationChannel(
            "starnazzo_service",
            "Servizio audio",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Notifica silenziosa durante la riproduzione audio"
            setShowBadge(false)
        }

        manager.createNotificationChannel(starnazzoChannel)
        manager.createNotificationChannel(generalChannel)
        manager.createNotificationChannel(serviceChannel)
    }
}
