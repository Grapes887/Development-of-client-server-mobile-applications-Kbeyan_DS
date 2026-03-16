package com.example.task10

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.task10.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationHelper: LocationHelper
    private val activityScope = CoroutineScope(Dispatchers.Main)

    // Код запроса разрешений (для обратной совместимости)
    private val PERMISSION_REQUEST_CODE = 1001

    // Современный API для запроса разрешений
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            // Разрешения получены, получаем местоположение
            getLocation()
        } else {
            // Разрешения не получены, показываем ошибку
            showError("Необходимо разрешение на определение местоположения")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationHelper = LocationHelper(this)

        setupUI()
    }

    private fun setupUI() {
        binding.btnGetAddress.setOnClickListener {
            checkPermissionsAndGetLocation()
        }
    }

    private fun checkPermissionsAndGetLocation() {
        if (PermissionsHelper.hasLocationPermissions(this)) {
            // Разрешения уже есть, проверяем включен ли GPS
            if (isLocationEnabled()) {
                getLocation()
            } else {
                showLocationDisabledDialog()
            }
        } else {
            // Нет разрешений, запрашиваем
            if (PermissionsHelper.shouldShowPermissionRationale(this)) {
                showPermissionRationaleDialog()
            } else {
                permissionLauncher.launch(PermissionsHelper.REQUIRED_PERMISSIONS)
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                locationManager.isLocationEnabled
            } else {
                val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                gpsEnabled || networkEnabled
            }
        } catch (e: Exception) {
            true // В случае ошибки предполагаем, что включено
        }
    }

    private fun showLocationDisabledDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Геолокация отключена")
            .setMessage("Для получения адреса необходимо включить геолокацию")
            .setPositiveButton("Настройки") { _, _ ->
                startActivity(android.content.Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Разрешение на геолокацию")
            .setMessage("Для определения вашего адреса необходимо разрешение на определение местоположения")
            .setPositiveButton("ОК") { _, _ ->
                permissionLauncher.launch(PermissionsHelper.REQUIRED_PERMISSIONS)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun getLocation() {
        // Показываем индикатор загрузки
        showLoading(true)
        hideError()
        hideResult()

        activityScope.launch {
            try {
                // Проверяем доступность Geocoder
                if (!locationHelper.isGeocoderAvailable()) {
                    withContext(Dispatchers.Main) {
                        showError("Geocoder недоступен на этом устройстве")
                    }
                    return@launch
                }

                // Получаем местоположение
                val locationResult = withContext(Dispatchers.IO) {
                    locationHelper.getCurrentLocation()
                }

                withContext(Dispatchers.Main) {
                    if (locationResult != null) {
                        displayLocationResult(locationResult)
                    } else {
                        showError("Не удалось получить местоположение. Проверьте GPS и интернет соединение.")
                    }
                }
            } catch (e: SecurityException) {
                showError("Ошибка доступа к местоположению")
            } catch (e: Exception) {
                showError("Ошибка: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun displayLocationResult(result: LocationResult) {
        // Форматируем координаты
        val coordinates = String.format("%.6f, %.6f", result.latitude, result.longitude)
        binding.tvCoordinates.text = coordinates

        // Отображаем адрес или сообщение о его отсутствии
        if (!result.address.isNullOrBlank()) {
            binding.tvAddress.text = result.address
        } else {
            binding.tvAddress.text = getString(R.string.no_address)
            // Показываем Toast с пояснением
            Toast.makeText(this, "Адрес не найден. Возможно, отсутствует интернет-соединение.", Toast.LENGTH_LONG).show()
        }

        // Показываем результат
        showResult()
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = android.view.View.VISIBLE
            binding.btnGetAddress.isEnabled = false
        } else {
            binding.progressBar.visibility = android.view.View.GONE
            binding.btnGetAddress.isEnabled = true
        }
    }

    private fun showResult() {
        binding.cardResult.visibility = android.view.View.VISIBLE
        binding.tvError.visibility = android.view.View.GONE
    }

    private fun hideResult() {
        binding.cardResult.visibility = android.view.View.GONE
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = android.view.View.VISIBLE
        binding.cardResult.visibility = android.view.View.GONE

        // Добавляем кнопку повтора в ошибку
        binding.tvError.setOnClickListener {
            checkPermissionsAndGetLocation()
        }
    }

    private fun hideError() {
        binding.tvError.visibility = android.view.View.GONE
        binding.tvError.setOnClickListener(null)
    }

    // Для обратной совместимости (если используется старый метод запроса разрешений)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val fineGranted = grantResults.getOrNull(
                permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)
            ) == PackageManager.PERMISSION_GRANTED
            val coarseGranted = grantResults.getOrNull(
                permissions.indexOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            ) == PackageManager.PERMISSION_GRANTED

            if (fineGranted || coarseGranted) {
                getLocation()
            } else {
                showError("Необходимо разрешение на определение местоположения")
            }
        }
    }
}