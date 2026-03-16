package com.example.task8.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadPhotoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val watermarkedPath = inputData.getString("watermarkedPath") ?: ""

            Log.d("PhotoWorker", "Starting upload of: $watermarkedPath")

            // Имитация загрузки в облако с прогрессом
            for (i in 0..100 step 5) {
                delay(200) // Имитация работы
                setProgress(workDataOf("progress" to i, "step" to 3))
                Log.d("PhotoWorker", "Upload progress: $i%")
            }

            // Имитация URL загруженного фото
            val uploadedUrl = "https://cloud.example.com/photos/photo_${System.currentTimeMillis()}.jpg"

            val outputData = workDataOf(
                "uploadedUrl" to uploadedUrl,
                "step" to 3,
                "finalPath" to watermarkedPath
            )

            Log.d("PhotoWorker", "Photo uploaded successfully: $uploadedUrl")
            Result.success(outputData)
        } catch (e: Exception) {
            Log.e("PhotoWorker", "Upload failed", e)
            Result.failure(workDataOf("error" to "Upload failed: ${e.message}"))
        }
    }
}