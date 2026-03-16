package com.example.task9.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.task9.utils.NotificationHelper
import kotlinx.coroutines.delay

class ReportGeneratorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            setForeground(getForegroundInfo())

            // Collect results from all weather workers
            val temperatures = mutableListOf<Int>()
            val cities = mutableListOf<String>()

            // Get all input data
            val inputData = inputData

            // Extract temperatures from the inputs of previous workers
            // In a real app, you might want a better way to pass data between workers
            val allKeys = inputData.keyValueMap.keys
            Log.d("ReportGenerator", "All keys: $allKeys")

            // This is a simplified approach - in production you'd use a more structured method
            allKeys.forEach { key ->
                when {
                    key.contains("temperature") -> {
                        inputData.getInt(key, 0)?.let { temperatures.add(it) }
                    }
                    key.contains("city") && key != "cities" -> {
                        inputData.getString(key)?.let { cities.add(it) }
                    }
                }
            }

            // If no data found through keys, try to extract from individual workers
            // This is a fallback mechanism
            if (temperatures.isEmpty()) {
                // Try to get data by specific city names
                listOf("Москва", "Лондон", "Нью-Йорк", "Токио").forEach { city ->
                    inputData.getInt("${city}_temp", 0)?.let {
                        if (it > 0) {
                            temperatures.add(it)
                            cities.add(city)
                        }
                    }
                }
            }

            // Simulate report generation
            delay(2000)

            // Calculate average temperature
            val averageTemp = if (temperatures.isNotEmpty()) {
                temperatures.average().toInt()
            } else {
                0
            }

            // Build report text
            val citiesText = if (cities.isNotEmpty()) {
                cities.joinToString(", ")
            } else {
                "все города"
            }

            val reportText = "Средняя температура: $averageTemp°C\nГорода: $citiesText"

            Log.d("ReportGenerator", "Report: $reportText")
            Log.d("ReportGenerator", "Temperatures: $temperatures")
            Log.d("ReportGenerator", "Cities: $cities")

            val outputData = Data.Builder()
                .putString("report", reportText)
                .putInt("average_temp", averageTemp)
                .putString("cities", citiesText)
                .build()

            Result.success(outputData)

        } catch (e: Exception) {
            Log.e("ReportGenerator", "Error generating report", e)
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NotificationHelper.NOTIFICATION_ID,
            NotificationHelper.createNotification(
                applicationContext,
                "Сбор прогноза погоды",
                "Формируем итоговый отчет...",
                isIndeterminate = true
            )
        )
    }
}