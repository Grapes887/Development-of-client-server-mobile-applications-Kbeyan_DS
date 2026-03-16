package com.example.task9.workers

import android.content.Context
import androidx.work.WorkerParameters

class TokyoWeatherWorker(
    context: Context,
    params: WorkerParameters
) : BaseWeatherWorker(context, params) {
    override val cityName = "Токио"
    override val minTemp = 18
    override val maxTemp = 28
}