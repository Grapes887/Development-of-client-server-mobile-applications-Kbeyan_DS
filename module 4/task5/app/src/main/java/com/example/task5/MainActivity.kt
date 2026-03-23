package com.example.task5

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TimerService.ACTION_UPDATE) {
                val seconds = intent.getIntExtra(TimerService.EXTRA_SECONDS, 0)
                tvTimer.text = seconds.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Это строка 30

        tvTimer = findViewById(R.id.tvTimer)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        btnStart.setOnClickListener {
            val intent = Intent(this, TimerService::class.java)
            startForegroundService(intent)
        }

        btnStop.setOnClickListener {
            val intent = Intent(this, TimerService::class.java)
            stopService(intent)
            tvTimer.text = "0"
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            timerReceiver,
            IntentFilter(TimerService.ACTION_UPDATE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver)
    }
}