package com.example.task9.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.task9.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlin.random.Random

abstract class BaseWeatherWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    abstract val cityName: String
    abstract val minTemp: Int
    abstract val maxTemp: Int

    override suspend fun doWork(): Result {
        return try {
            Log.d("WeatherWorker", "Starting work for $cityName")

            // Set as foreground work - делаем это в начале
            val foregroundInfo = getForegroundInfo()
            setForeground(foregroundInfo)

            // Simulate weather download with progress
            for (i in 1..10) {
                delay(500) // Simulate network delay

                // Update progress
                val progress = i * 10
                val progressData = Data.Builder()
                    .putString("city", cityName)
                    .putInt("progress", progress)
                    .putString("status", "Загружаем данные для $cityName... $progress%")
                    .build()

                setProgress(progressData)
                Log.d("WeatherWorker", "$cityName progress: $progress%")
            }

            // Generate random temperature
            val temperature = Random.nextInt(minTemp, maxTemp)

            // Simulate processing
            delay(1000)

            Log.d("WeatherWorker", "$cityName completed with temperature: $temperature")

            // Return result
            val outputData = Data.Builder()
                .putString("city", cityName)
                .putInt("temperature", temperature)
                .putString("status", "Готово: $cityName")
                .build()

            Result.success(outputData)

        } catch (e: Exception) {
            Log.e("WeatherWorker", "Error loading $cityName", e)
            val errorData = Data.Builder()
                .putString("city", cityName)
                .putString("error", e.message ?: "Unknown error")
                .build()

            Result.failure(errorData)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationHelper.createNotification(
            applicationContext,
            "Сбор прогноза погоды",
            "Загружаем данные для $cityName...",
            isIndeterminate = true
        )

        return ForegroundInfo(
            NotificationHelper.NOTIFICATION_ID,
            notification
        )
    }
}