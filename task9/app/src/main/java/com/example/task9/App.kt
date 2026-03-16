package com.example.task9

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.example.task9.utils.NotificationHelper

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    NotificationHelper.CHANNEL_ID,
                    NotificationHelper.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW  // Используем LOW вместо DEFAULT для foregound service
                ).apply {
                    description = NotificationHelper.CHANNEL_DESCRIPTION
                    setShowBadge(false)
                    enableLights(false)
                    enableVibration(false)
                    setSound(null, null)  // Без звука для foreground service
                }

                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
                Log.d("App", "Notification channel created: ${NotificationHelper.CHANNEL_ID}")
            } catch (e: Exception) {
                Log.e("App", "Error creating notification channel", e)
            }
        }
    }
}