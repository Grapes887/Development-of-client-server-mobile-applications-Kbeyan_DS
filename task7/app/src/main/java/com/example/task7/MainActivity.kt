package com.example.task7

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvNumber: TextView
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button

    private var randomNumberService: RandomNumberService? = null
    private var isBound = false

    private val numberUpdateListener = object : RandomNumberService.OnNumberUpdateListener {
        override fun onNumberUpdated(number: Int) {
            runOnUiThread {
                tvNumber.text = number.toString()
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RandomNumberService.RandomNumberBinder
            randomNumberService = binder.getService()
            randomNumberService?.setNumberUpdateListener(numberUpdateListener)
            isBound = true
            Toast.makeText(this@MainActivity, "Подключено к сервису", Toast.LENGTH_SHORT).show()
            updateButtonState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            randomNumberService?.removeNumberUpdateListener()
            randomNumberService = null
            isBound = false
            Toast.makeText(this@MainActivity, "Отключено от сервиса", Toast.LENGTH_SHORT).show()
            updateButtonState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvNumber = findViewById(R.id.tvNumber)
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)

        btnConnect.setOnClickListener {
            bindService()
        }

        btnDisconnect.setOnClickListener {
            unbindService()
        }

        updateButtonState()
    }

    private fun bindService() {
        val intent = Intent(this, RandomNumberService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        if (isBound) {
            unbindService(connection)
            randomNumberService?.removeNumberUpdateListener()
            randomNumberService = null
            isBound = false
            tvNumber.text = "---"
            updateButtonState()
        }
    }

    private fun updateButtonState() {
        btnConnect.isEnabled = !isBound
        btnDisconnect.isEnabled = isBound
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            randomNumberService?.removeNumberUpdateListener()
            randomNumberService = null
            isBound = false
        }
    }
}