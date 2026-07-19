package com.whereduck.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WhereTheDuckApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        // Delete old channels if exist (settings are sticky)
        manager.deleteNotificationChannel("starnazzo")
        manager.deleteNotificationChannel("starnazzo_reminder")

        val starnazzoChannel = NotificationChannel(
            "starnazzo_v2",
            "Duck",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifiche Duck"
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
