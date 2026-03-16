package com.example.task9.workers

import android.content.Context
import androidx.work.WorkerParameters

class NewYorkWeatherWorker(
    context: Context,
    params: WorkerParameters
) : BaseWeatherWorker(context, params) {
    override val cityName = "Нью-Йорк"
    override val minTemp = 15
    override val maxTemp = 30
}