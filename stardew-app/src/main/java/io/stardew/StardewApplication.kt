package io.stardew

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class StardewApplication : Application() {

    companion object {
        lateinit var instance: StardewApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "stardew_service",
                "Stardew Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Stardew running in the background"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
