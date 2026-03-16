package com.example.task11

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // Show notification
        NotificationHelper.showReminderNotification(context, "Время принять таблетку!")

        // Reschedule for next day
        val reminderManager = ReminderManager(context)
        reminderManager.scheduleDailyReminder()
    }
}