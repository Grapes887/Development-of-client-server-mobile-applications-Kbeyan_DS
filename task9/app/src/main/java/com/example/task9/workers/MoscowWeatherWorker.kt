package com.example.task9.workers

import android.content.Context
import androidx.work.WorkerParameters

class MoscowWeatherWorker(
    context: Context,
    params: WorkerParameters
) : BaseWeatherWorker(context, params) {
    override val cityName = "Москва"
    override val minTemp = 10
    override val maxTemp = 25
}