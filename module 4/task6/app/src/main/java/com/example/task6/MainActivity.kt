package com.example.task6

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etSeconds: EditText
    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etSeconds = findViewById(R.id.etSeconds)
        btnStart = findViewById(R.id.btnStart)

        btnStart.setOnClickListener {
            val seconds = etSeconds.text.toString().toIntOrNull()

            if (seconds != null && seconds > 0) {
                val intent = Intent(this, TimerService::class.java).apply {
                    putExtra(TimerService.EXTRA_SECONDS, seconds)
                }
                startService(intent)
                Toast.makeText(this, "Таймер запущен на $seconds секунд", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Введите корректное количество секунд", Toast.LENGTH_SHORT).show()
            }
        }
    }
}