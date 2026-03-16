package com.example.task13.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class CurrencyViewModel : ViewModel() {

    private val _usdRate = MutableStateFlow(90.5)
    val usdRate: StateFlow<Double> = _usdRate.asStateFlow()

    private val _previousRate = MutableStateFlow(90.5)

    val rateChangeDirection: StateFlow<RateDirection> = combine(
        _usdRate,
        _previousRate
    ) { current, previous ->
        when {
            current > previous -> RateDirection.UP
            current < previous -> RateDirection.DOWN
            else -> RateDirection.STABLE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RateDirection.STABLE
    )

    init {
        startAutoUpdate()
    }

    private fun startAutoUpdate() {
        viewModelScope.launch {
            while (true) {
                delay(5000) // Обновление каждые 5 секунд
                generateNewRate()
            }
        }
    }

    fun refreshRate() {
        viewModelScope.launch {
            generateNewRate()
        }
    }

    private fun generateNewRate() {
        _previousRate.update { _usdRate.value }

        val randomChange = Random.nextDouble(-2.0, 2.0)
        val newRate = _usdRate.value + randomChange

        _usdRate.update {
            // Округляем до 2 знаков после запятой
            kotlin.math.round(newRate * 100) / 100
        }
    }
}

enum class RateDirection {
    UP, DOWN, STABLE
}