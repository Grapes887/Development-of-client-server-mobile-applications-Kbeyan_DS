package com.example.task9

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.task9.utils.NotificationHelper
import com.example.task9.workers.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WeatherReportViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)
    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _status = MutableLiveData<String>("Готов к сбору прогноза")
    val status: LiveData<String> = _status

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _progress = MutableLiveData<Int>(0)
    val progress: LiveData<Int> = _progress

    private val _report = MutableLiveData<String>("")
    val report: LiveData<String> = _report

    private val _citiesProgress = MutableLiveData<Map<String, Int>>(emptyMap())
    val citiesProgress: LiveData<Map<String, Int>> = _citiesProgress

    private var workChain: WorkContinuation? = null

    fun startWeatherCollection() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _status.postValue("Начинаем сбор прогноза погоды...")

            // Create workers for each city
            val moscowWork = OneTimeWorkRequestBuilder<MoscowWeatherWorker>()
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .addTag("weather_moscow")
                .build()

            val londonWork = OneTimeWorkRequestBuilder<LondonWeatherWorker>()
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .addTag("weather_london")
                .build()

            val newYorkWork = OneTimeWorkRequestBuilder<NewYorkWeatherWorker>()
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .addTag("weather_newyork")
                .build()

            val tokyoWork = OneTimeWorkRequestBuilder<TokyoWeatherWorker>()
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .addTag("weather_tokyo")
                .build()

            // Run them in parallel
            val parallelWork = workManager.beginWith(listOf(moscowWork, londonWork, newYorkWork, tokyoWork))

            // Then generate report
            val reportWork = OneTimeWorkRequestBuilder<ReportGeneratorWorker>()
                .build()

            workChain = parallelWork.then(reportWork)

            // Observe progress
            workManager.getWorkInfosByTagLiveData("weather_moscow").observeForever { workInfos ->
                updateCityProgress("Москва", workInfos)
            }

            workManager.getWorkInfosByTagLiveData("weather_london").observeForever { workInfos ->
                updateCityProgress("Лондон", workInfos)
            }

            workManager.getWorkInfosByTagLiveData("weather_newyork").observeForever { workInfos ->
                updateCityProgress("Нью-Йорк", workInfos)
            }

            workManager.getWorkInfosByTagLiveData("weather_tokyo").observeForever { workInfos ->
                updateCityProgress("Токио", workInfos)
            }

            // Observe the chain
            workManager.getWorkInfoByIdLiveData(reportWork.id).observeForever { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val reportText = workInfo.outputData.getString("report") ?: "Отчет готов"
                            _report.postValue(reportText)
                            _status.postValue("Отчет готов!")
                            _isLoading.postValue(false)

                            // Final notification update
                            NotificationHelper.updateNotification(
                                getApplication(),
                                notificationManager,
                                "Прогноз погоды готов",
                                reportText,
                                progress = 100,
                                maxProgress = 100,
                                isIndeterminate = false
                            )
                        }
                        WorkInfo.State.FAILED -> {
                            _status.postValue("Ошибка при формировании отчета")
                            _isLoading.postValue(false)
                        }
                        WorkInfo.State.RUNNING -> {
                            _status.postValue("Формируем итоговый отчет...")
                            NotificationHelper.updateNotification(
                                getApplication(),
                                notificationManager,
                                "Сбор прогноза погоды",
                                "Формируем итоговый отчет...",
                                isIndeterminate = true
                            )
                        }
                        else -> {}
                    }
                }
            }

            // Show initial notification
            NotificationHelper.updateNotification(
                getApplication(),
                notificationManager,
                "Сбор прогноза погоды",
                "Загружаем данные для 4 городов...",
                isIndeterminate = true
            )

            // Enqueue the work
            workChain?.enqueue()
        }
    }

    private fun updateCityProgress(cityName: String, workInfos: List<WorkInfo>) {
        if (workInfos.isEmpty()) return

        val workInfo = workInfos.first()
        val progress = workInfo.progress.getInt("progress", 0)
        val status = workInfo.progress.getString("status") ?: ""

        viewModelScope.launch {
            // Update cities progress map
            val currentMap = _citiesProgress.value?.toMutableMap() ?: mutableMapOf()
            currentMap[cityName] = progress
            _citiesProgress.postValue(currentMap)

            // Calculate completed and in-progress cities
            val completedCities = currentMap.filter { it.value == 100 }.keys
            val inProgressCities = currentMap.filter { it.value in 1..99 }.keys
            val allCities = listOf("Москва", "Лондон", "Нью-Йорк", "Токио")
            val pendingCities = allCities.filter { !currentMap.containsKey(it) || currentMap[it] == 0 }

            val statusText = buildString {
                if (completedCities.isNotEmpty()) {
                    append("Готово: ${completedCities.joinToString(", ")}. ")
                }
                if (inProgressCities.isNotEmpty()) {
                    append("В процессе: ${inProgressCities.joinToString(", ")}. ")
                }
                if (pendingCities.isNotEmpty()) {
                    append("Ожидают: ${pendingCities.joinToString(", ")}")
                }
            }

            _status.postValue(statusText)

            // Update notification
            val notificationText = when {
                inProgressCities.isNotEmpty() -> {
                    "Загружаем: ${inProgressCities.joinToString(", ")}"
                }
                completedCities.isNotEmpty() && completedCities.size < allCities.size -> {
                    "Готово ${completedCities.size} из ${allCities.size} городов"
                }
                completedCities.size == allCities.size -> {
                    "Все данные получены, формируем отчет..."
                }
                else -> status
            }

            val progressValue = (completedCities.size * 100 / allCities.size)

            NotificationHelper.updateNotification(
                getApplication(),
                notificationManager,
                "Сбор прогноза погоды",
                notificationText,
                progress = progressValue,
                maxProgress = 100,
                isIndeterminate = false
            )
        }
    }

    fun cancelWork() {
        workChain?.let {
            workManager.cancelUniqueWork("weather_chain")
            _isLoading.postValue(false)
            _status.postValue("Сбор прогноза отменен")

            NotificationHelper.updateNotification(
                getApplication(),
                notificationManager,
                "Сбор прогноза отменен",
                "Работа была отменена пользователем",
                progress = 0,
                isIndeterminate = false
            )
        }
    }
}