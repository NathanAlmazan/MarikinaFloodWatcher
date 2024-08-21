package com.floodalert.disafeter.screens.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.floodalert.disafeter.model.FloodData
import com.floodalert.disafeter.model.FloodSummary
import com.floodalert.disafeter.model.repos.Result
import com.floodalert.disafeter.model.WeatherData
import com.floodalert.disafeter.model.repos.CurrentRiver
import com.floodalert.disafeter.model.repos.ForecastRiver
import com.floodalert.disafeter.model.repos.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository): ViewModel() {
    private var _riverLevel by mutableFloatStateOf(0.0f)
    private var _riverForecasts = mutableStateListOf<ForecastRiver>()
    private var _floodData by mutableStateOf(FloodData())
    private var _weatherData by mutableStateOf(WeatherData())
    private var _floodHistory = mutableStateListOf<FloodData>()
    private var _floodSummary = mutableStateListOf<FloodSummary>()
    private var _errorMessage by mutableStateOf<String?>(null)

    val riverLevel: Float get() = _riverLevel
    val floodData: FloodData get() = _floodData
    val weatherData: WeatherData get() = _weatherData
    val floodHistory: List<FloodData> get() = _floodHistory
    val floodSummary: List<FloodSummary> get() = _floodSummary
    val riverForecasts: List<ForecastRiver> get() = _riverForecasts

    init {
        fetchRiverData()
        fetchFloodHistory()
        fetchFloodSummary()
        fetchRiverForecast()
    }

    fun addListener() {
        viewModelScope.launch {
            repository.addDbListener(::onDocumentEvent, ::onError)
        }
    }

    fun removeListener() {
        viewModelScope.launch {
            repository.removeDbListener()
        }
    }

    fun setWeatherData(lat: Double = BRGY_LATITUDE, lng: Double = BRGY_LONGITUDE) {
        viewModelScope.launch {
            when (val result = repository.fetchWeatherData(lat, lng)) {
                is Result.Success<WeatherData> -> _weatherData = result.data
                is Result.Error -> _errorMessage = result.exception.message.toString()
            }
        }
    }

    private fun onDocumentEvent(data: FloodData) {
        _floodData = data
        setWeatherData()
    }

    private fun onError(exception: Exception) {
        _errorMessage = exception.message!!
    }

    fun fetchRiverData() {
        viewModelScope.launch {
            when (val result = repository.fetchCurrentRiver()) {
                is Result.Success<List<CurrentRiver>> -> {
                    _riverLevel = result.data[0].level
                }
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    fun fetchRiverForecast() {
        viewModelScope.launch {
            when (val result = repository.fetchForecastRiver()) {
                is Result.Success<List<ForecastRiver>> -> {
                    _riverForecasts.clear()
                    _riverForecasts.addAll(result.data)
                }
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    private fun fetchFloodHistory() {
        viewModelScope.launch {
            when (val result = repository.fetchFloodHistory()) {
                is Result.Success<List<FloodData>> -> {
                    _floodHistory.clear()
                    _floodHistory.addAll(result.data)
                }
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    private fun fetchFloodSummary() {
        viewModelScope.launch {
            when (val result = repository.fetchFloodSummary()) {
                is Result.Success<List<FloodSummary>> -> {
                    _floodSummary.clear()
                    _floodSummary.addAll(result.data)
                }
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = Firebase.firestore
                WeatherViewModel(WeatherRepository(database))
            }
        }

        const val BRGY_LATITUDE = 14.647512434076672
        const val BRGY_LONGITUDE = 120.96360798231696
    }
}