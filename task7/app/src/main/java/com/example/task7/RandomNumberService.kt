package com.example.task7

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*

class RandomNumberService : Service() {

    private val binder = RandomNumberBinder()
    private var numberUpdateListener: OnNumberUpdateListener? = null
    private var currentNumber = 0
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isGenerating = false

    interface OnNumberUpdateListener {
        fun onNumberUpdated(number: Int)
    }

    inner class RandomNumberBinder : Binder() {
        fun getService(): RandomNumberService = this@RandomNumberService
    }

    override fun onCreate() {
        super.onCreate()
        startNumberGeneration()
    }

    private fun startNumberGeneration() {
        if (isGenerating) return

        isGenerating = true
        serviceScope.launch {
            while (isGenerating) {
                delay(1000)
                currentNumber = (0..100).random()
                numberUpdateListener?.onNumberUpdated(currentNumber)
            }
        }
    }

    fun setNumberUpdateListener(listener: OnNumberUpdateListener) {
        this.numberUpdateListener = listener
        // Сразу отправляем текущее значение
        listener.onNumberUpdated(currentNumber)
    }

    fun removeNumberUpdateListener() {
        this.numberUpdateListener = null
    }

    fun getCurrentNumber(): Int = currentNumber

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        removeNumberUpdateListener()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        isGenerating = false
        serviceScope.cancel()
        removeNumberUpdateListener()
    }
}