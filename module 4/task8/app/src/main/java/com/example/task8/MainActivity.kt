package com.example.task8

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.task8.databinding.ActivityMainBinding
import com.example.task8.workers.AddWatermarkWorker
import com.example.task8.workers.CompressPhotoWorker
import com.example.task8.workers.UploadPhotoWorker
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val workManager = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.startButton.setOnClickListener {
            startPhotoProcessing()
        }
    }

    private fun startPhotoProcessing() {
        // Отключаем кнопку
        binding.startButton.isEnabled = false
        binding.progressBar.progress = 0
        binding.resultText.text = ""
        binding.statusText.text = "Начинаем обработку..."

        // Создаем первый Worker для сжатия
        val compressRequest = OneTimeWorkRequestBuilder<CompressPhotoWorker>()
            .setInputData(workDataOf("originalPath" to "/storage/emulated/0/DCIM/photo.jpg"))
            .addTag("photo_processing")
            .build()

        // Создаем второй Worker для водяного знака
        val watermarkRequest = OneTimeWorkRequestBuilder<AddWatermarkWorker>()
            .addTag("photo_processing")
            .build()

        // Создаем третий Worker для загрузки
        val uploadRequest = OneTimeWorkRequestBuilder<UploadPhotoWorker>()
            .addTag("photo_processing")
            .build()

        // Строим цепочку
        workManager
            .beginUniqueWork(
                "photo_processing_chain",
                ExistingWorkPolicy.REPLACE,
                compressRequest
            )
            .then(watermarkRequest)
            .then(uploadRequest)
            .enqueue()

        // Наблюдаем за прогрессом
        observeWorkProgress()
    }

    private fun observeWorkProgress() {
        lifecycleScope.launch {
            workManager.getWorkInfosByTagLiveData("photo_processing")
                .observe(this@MainActivity) { workInfos ->
                    updateUI(workInfos)
                }
        }
    }

    private fun updateUI(workInfos: List<WorkInfo>) {
        if (workInfos.isEmpty()) return

        workInfos.forEach { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.RUNNING -> {
                    val stepProgress = workInfo.progress.getInt("progress", 0)
                    val step = workInfo.progress.getInt("step", 0)

                    when (step) {
                        1 -> {
                            binding.statusText.text = "Сжимаем фото... $stepProgress%"
                        }
                        2 -> {
                            binding.statusText.text = "Добавляем водяной знак... $stepProgress%"
                        }
                        3 -> {
                            binding.statusText.text = "Загружаем фото... $stepProgress%"
                        }
                    }

                    binding.progressBar.progress = stepProgress
                }

                WorkInfo.State.SUCCEEDED -> {
                    if (workInfo.tags.contains(UploadPhotoWorker::class.java.simpleName)) {
                        val uploadedUrl = workInfo.outputData.getString("uploadedUrl")
                        val finalPath = workInfo.outputData.getString("finalPath")
                        binding.statusText.text = "Готово! Фото загружено"
                        binding.resultText.text = "Путь: $finalPath\nURL: $uploadedUrl"
                        binding.startButton.isEnabled = true
                    }
                }

                WorkInfo.State.FAILED -> {
                    val error = workInfo.outputData.getString("error") ?: "Unknown error"
                    binding.statusText.text = "Ошибка!"
                    binding.resultText.text = error
                    binding.startButton.isEnabled = true
                    Log.e("MainActivity", "Work failed: $error")
                }

                else -> {
                    // Другие состояния (ENQUEUED, BLOCKED, CANCELLED)
                }
            }
        }
    }
}