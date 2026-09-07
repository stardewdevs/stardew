package io.stardew

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import io.stardew.service.SessionManager
import io.stardew.storage.Preferences

class StardewApplication : Application() {

    companion object {
        lateinit var instance: StardewApplication
            private set
    }

    private lateinit var preferences: Preferences
    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        instance = this

        preferences = Preferences(this)
        sessionManager = SessionManager()

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

    fun getPreferences(): Preferences = preferences
    fun getSessionManager(): SessionManager = sessionManager
}
