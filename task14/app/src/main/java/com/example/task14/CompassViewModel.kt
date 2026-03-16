package com.example.task14

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CompassViewModel : ViewModel() {

    private val _azimuth = MutableLiveData<Float>()
    val azimuth: LiveData<Float> = _azimuth

    private val _hasSensor = MutableLiveData<Boolean>()
    val hasSensor: LiveData<Boolean> = _hasSensor

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                when (it.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(it.values, 0, accelerometerReading, 0, accelerometerReading.size)
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(it.values, 0, magnetometerReading, 0, magnetometerReading.size)
                    }
                }

                updateOrientationAngles()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Не используется
        }
    }

    fun initialize(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val hasAccelerometer = accelerometer != null
        val hasMagnetometer = magnetometer != null

        _hasSensor.postValue(hasAccelerometer && hasMagnetometer)
    }

    fun startListening() {
        accelerometer?.let {
            sensorManager?.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        magnetometer?.let {
            sensorManager?.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(sensorEventListener)
    }

    private fun updateOrientationAngles() {
        if (SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading
            )
        ) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            val azimuthInRadians = orientationAngles[0]
            val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
            val normalizedAzimuth = (azimuthInDegrees + 360) % 360

            _azimuth.postValue(normalizedAzimuth)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}