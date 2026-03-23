package com.example.task8.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompressPhotoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("PhotoWorker", "Starting photo compression...")

            // Имитация сжатия фото с прогрессом
            for (i in 0..100 step 10) {
                delay(300) // Имитация работы
                setProgress(workDataOf("progress" to i, "step" to 1))
                Log.d("PhotoWorker", "Compression progress: $i%")
            }

            // Имитация сохранения сжатого файла
            val compressedPath = "${applicationContext.filesDir}/compressed_photo_${System.currentTimeMillis()}.jpg"

            val outputData = workDataOf(
                "compressedPath" to compressedPath,
                "step" to 1
            )

            Log.d("PhotoWorker", "Photo compressed successfully: $compressedPath")
            Result.success(outputData)
        } catch (e: Exception) {
            Log.e("PhotoWorker", "Compression failed", e)
            Result.failure(workDataOf("error" to "Compression failed: ${e.message}"))
        }
    }
}