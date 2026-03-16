package com.example.task14

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.task14.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CompassViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CompassViewModel::class.java]
        viewModel.initialize(this)

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.hasSensor.observe(this) { hasSensor ->
            if (!hasSensor) {
                binding.compassContainer.visibility = View.GONE
                binding.azimuthTextView.visibility = View.GONE
                binding.errorTextView.visibility = View.VISIBLE
            } else {
                binding.compassContainer.visibility = View.VISIBLE
                binding.azimuthTextView.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE
            }
        }

        viewModel.azimuth.observe(this) { azimuth ->
            binding.compassView.setAzimuth(azimuth)
            binding.azimuthTextView.text = getString(R.string.azimuth, azimuth.toInt())
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startListening()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopListening()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем текущий азимут для восстановления после поворота
        viewModel.azimuth.value?.let {
            outState.putFloat("azimuth", it)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Восстанавливаем азимут после поворота экрана
        val savedAzimuth = savedInstanceState.getFloat("azimuth", 0f)
        if (savedAzimuth != 0f) {
            binding.compassView.setAzimuth(savedAzimuth)
        }
    }
}