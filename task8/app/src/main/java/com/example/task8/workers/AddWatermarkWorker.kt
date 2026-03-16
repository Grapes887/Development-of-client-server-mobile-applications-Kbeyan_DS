package com.example.task8.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddWatermarkWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val compressedPath = inputData.getString("compressedPath") ?: ""

            Log.d("PhotoWorker", "Starting adding watermark to: $compressedPath")

            // Имитация добавления водяного знака с прогрессом
            for (i in 0..100 step 10) {
                delay(250) // Имитация работы
                setProgress(workDataOf("progress" to i, "step" to 2))
                Log.d("PhotoWorker", "Watermark progress: $i%")
            }

            // Имитация сохранения фото с водяным знаком
            val watermarkedPath = compressedPath.replace("compressed", "watermarked")

            val outputData = workDataOf(
                "watermarkedPath" to watermarkedPath,
                "step" to 2
            )

            Log.d("PhotoWorker", "Watermark added successfully: $watermarkedPath")
            Result.success(outputData)
        } catch (e: Exception) {
            Log.e("PhotoWorker", "Adding watermark failed", e)
            Result.failure(workDataOf("error" to "Adding watermark failed: ${e.message}"))
        }
    }
}