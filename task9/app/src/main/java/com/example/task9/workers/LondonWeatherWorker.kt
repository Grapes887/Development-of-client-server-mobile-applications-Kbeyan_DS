package com.example.task9.workers

import android.content.Context
import androidx.work.WorkerParameters

class LondonWeatherWorker(
    context: Context,
    params: WorkerParameters
) : BaseWeatherWorker(context, params) {
    override val cityName = "Лондон"
    override val minTemp = 12
    override val maxTemp = 22
}