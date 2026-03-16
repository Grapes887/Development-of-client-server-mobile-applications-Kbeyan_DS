package com.example.task10

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val address: String?
)

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context, Locale.getDefault())

    // Получение текущего местоположения с использованием getCurrentLocation (Android 12+)
    suspend fun getCurrentLocation(): LocationResult? {
        return try {
            // Создаем запрос с высоким приоритетом точности
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L // интервал 5 сек
            )
                .setMaxUpdateAgeMillis(10000L) // не старше 10 сек
                .build()

            val cancellationTokenSource = CancellationTokenSource()

            // Запрашиваем текущую локацию
            val location = fusedLocationClient.getCurrentLocation(
                locationRequest.priority,
                cancellationTokenSource.token
            ).await()

            location?.let {
                val address = getAddressFromLocation(it.latitude, it.longitude)
                LocationResult(it.latitude, it.longitude, address)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Пробуем получить последнюю известную локацию как fallback
            getLastKnownLocation()
        }
    }

    // Получение последней известной локации (fallback метод)
    private suspend fun getLastKnownLocation(): LocationResult? {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                val address = getAddressFromLocation(it.latitude, it.longitude)
                LocationResult(it.latitude, it.longitude, address)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Обратное геокодирование (координаты -> адрес)
    private fun getAddressFromLocation(lat: Double, lng: Double): String? {
        return try {
            // Проверяем доступность Geocoder
            if (!Geocoder.isPresent()) {
                return null
            }

            val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)

            addresses?.firstOrNull()?.let { address ->
                buildAddressString(address)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Формирование читаемого адреса из объекта Address
    private fun buildAddressString(address: Address): String {
        val parts = mutableListOf<String>()

        // Добавляем улицу и номер дома
        val street = address.thoroughfare ?: address.subThoroughfare
        val streetNumber = address.subThoroughfare
        if (street != null) {
            if (streetNumber != null && !street.contains(streetNumber)) {
                parts.add("$street $streetNumber")
            } else {
                parts.add(street)
            }
        } else if (streetNumber != null) {
            parts.add(streetNumber)
        }

        // Добавляем район/город
        address.locality?.let { parts.add(it) }
        address.subAdminArea?.takeIf { it != address.locality }?.let { parts.add(it) }

        // Добавляем область/регион
        address.adminArea?.takeIf { it != address.locality && it != address.subAdminArea }?.let { parts.add(it) }

        // Добавляем страну
        address.countryName?.let { parts.add(it) }

        // Добавляем почтовый индекс
        address.postalCode?.let { parts.add(it) }

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            // Если не удалось построить адрес, возвращаем null
            address.getAddressLine(0) ?: "Адрес не определен"
        }
    }

    // Проверка доступности интернета для Geocoder
    fun isGeocoderAvailable(): Boolean {
        return Geocoder.isPresent()
    }
}