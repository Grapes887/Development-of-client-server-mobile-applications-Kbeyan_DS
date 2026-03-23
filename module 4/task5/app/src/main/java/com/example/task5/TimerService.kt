package com.example.task5

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*

class TimerService : Service() {

    private val CHANNEL_ID = "timer_channel"
    private val NOTIFICATION_ID = 1
    private var seconds = 0
    private var isRunning = false
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val ACTION_UPDATE = "com.example.task5.UPDATE"
        const val EXTRA_SECONDS = "extra_seconds"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Исправлено: используем startForeground сразу
        startForeground(NOTIFICATION_ID, createNotification(0))

        if (!isRunning) {
            isRunning = true
            startTimer()
        }

        return START_STICKY
    }

    private fun startTimer() {
        serviceScope.launch {
            while (isRunning) {
                delay(1000)
                seconds++

                // Обновляем уведомление
                val notification = createNotification(seconds)
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(NOTIFICATION_ID, notification)

                // Отправляем обновление в Activity
                sendUpdateToActivity(seconds)
            }
        }
    }

    private fun sendUpdateToActivity(seconds: Int) {
        val intent = Intent(ACTION_UPDATE).apply {
            putExtra(EXTRA_SECONDS, seconds)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotification(seconds: Int): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Таймер")
            .setContentText("Прошло $seconds секунд")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for timer service"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}