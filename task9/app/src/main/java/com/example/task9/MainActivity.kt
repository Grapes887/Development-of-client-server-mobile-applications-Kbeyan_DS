package com.example.task9

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.task9.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: WeatherReportViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted - can start work
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[WeatherReportViewModel::class.java]

        checkNotificationPermission()
        setupViews()
        observeViewModel()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupViews() {
        binding.startButton.setOnClickListener {
            viewModel.startWeatherCollection()
            binding.cancelButton.visibility = android.view.View.VISIBLE
        }

        binding.cancelButton.setOnClickListener {
            viewModel.cancelWork()
            binding.cancelButton.visibility = android.view.View.GONE
        }
    }

    private fun observeViewModel() {
        viewModel.status.observe(this) { status ->
            binding.statusTextView.text = status
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.citiesProgressLabel.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.citiesProgressLayout.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.startButton.isEnabled = !isLoading

            if (!isLoading) {
                binding.cancelButton.visibility = android.view.View.GONE
            }
        }

        viewModel.citiesProgress.observe(this) { progressMap ->
            progressMap.forEach { (city, progress) ->
                when (city) {
                    "Москва" -> {
                        binding.moscowTextView.text = "Москва: $progress%"
                        binding.moscowProgressBar.progress = progress
                    }
                    "Лондон" -> {
                        binding.londonTextView.text = "Лондон: $progress%"
                        binding.londonProgressBar.progress = progress
                    }
                    "Нью-Йорк" -> {
                        binding.newYorkTextView.text = "Нью-Йорк: $progress%"
                        binding.newYorkProgressBar.progress = progress
                    }
                    "Токио" -> {
                        binding.tokyoTextView.text = "Токио: $progress%"
                        binding.tokyoProgressBar.progress = progress
                    }
                }
            }
        }

        viewModel.report.observe(this) { report ->
            binding.reportTextView.text = report
            binding.reportTextView.visibility = android.view.View.VISIBLE
        }
    }
}