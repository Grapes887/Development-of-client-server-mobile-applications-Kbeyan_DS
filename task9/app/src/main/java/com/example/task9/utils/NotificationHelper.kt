package com.example.task9.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.task9.MainActivity
import com.example.task9.R

object NotificationHelper {
    const val CHANNEL_ID = "weather_report_channel"
    const val CHANNEL_NAME = "Weather Reports"
    const val CHANNEL_DESCRIPTION = "Shows weather report progress"
    const val NOTIFICATION_ID = 1001

    fun createNotification(
        context: Context,
        title: String,
        content: String,
        progress: Int = 0,
        maxProgress: Int = 100,
        isIndeterminate: Boolean = false
    ): Notification {

        Log.d("NotificationHelper", "Creating notification: $title, $content")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // Используем системную иконку
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Важно для Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        // Устанавливаем прогресс
        if (isIndeterminate) {
            builder.setProgress(0, 0, true)
        } else {
            builder.setProgress(maxProgress, progress, false)
        }

        return builder.build()
    }

    fun updateNotification(
        context: Context,
        notificationManager: NotificationManager,
        title: String,
        content: String,
        progress: Int = 0,
        maxProgress: Int = 100,
        isIndeterminate: Boolean = false
    ) {
        try {
            val notification = createNotification(
                context,
                title,
                content,
                progress,
                maxProgress,
                isIndeterminate
            )
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d("NotificationHelper", "Notification updated: $title")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error updating notification", e)
        }
    }
}