package com.example.task11

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var statusIndicator: View
    private lateinit var statusText: TextView
    private lateinit var statusDetail: TextView
    private lateinit var nextReminderText: TextView
    private lateinit var actionButton: Button
    private lateinit var reminderManager: ReminderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        reminderManager = ReminderManager(this)

        initViews()
        checkNotificationPermission()
        updateUI()
        setupClickListeners()
    }

    private fun initViews() {
        statusIndicator = findViewById<View>(R.id.status_indicator)
        statusText = findViewById(R.id.status_text)
        statusDetail = findViewById(R.id.status_detail)
        nextReminderText = findViewById(R.id.next_reminder_text)
        actionButton = findViewById(R.id.action_button)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    private fun updateUI() {
        val isEnabled = reminderManager.isReminderEnabled()

        // Update status indicator
        if (isEnabled) {
            statusIndicator.setBackgroundResource(R.drawable.status_indicator_green)
            statusText.text = "Включено"
            statusDetail.text = "Напоминание активно"

            // Calculate next reminder time
            val nextTime = reminderManager.getNextReminderTime()
            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val nextTimeStr = formatter.format(Date(nextTime))

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = nextTime
            val today = Calendar.getInstance()

            val dayText = if (calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                "сегодня"
            } else {
                "завтра"
            }

            nextReminderText.text = "Следующее напоминание: $dayText в 20:00\n($nextTimeStr)"
            actionButton.text = "Выключить напоминание"
        } else {
            statusIndicator.setBackgroundResource(R.drawable.status_indicator_gray)
            statusText.text = "Выключено"
            statusDetail.text = "Напоминание не активно"
            nextReminderText.text = "Напоминание не установлено"
            actionButton.text = "Включить напоминание"
        }
    }

    private fun setupClickListeners() {
        actionButton.setOnClickListener {
            if (reminderManager.isReminderEnabled()) {
                // Disable reminder
                reminderManager.cancelReminder()
                Toast.makeText(this, "Напоминание отключено", Toast.LENGTH_SHORT).show()
            } else {
                // Enable reminder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Toast.makeText(this, "Пожалуйста, разрешите точные будильники в настройках", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                }

                reminderManager.scheduleDailyReminder()
                Toast.makeText(this, "Напоминание включено на 20:00", Toast.LENGTH_SHORT).show()
            }
            updateUI()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение на уведомления получено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Не удалось получить разрешение на уведомления", Toast.LENGTH_SHORT).show()
            }
        }
    }
}