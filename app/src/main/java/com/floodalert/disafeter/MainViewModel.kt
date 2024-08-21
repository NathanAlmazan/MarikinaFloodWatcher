package com.floodalert.disafeter

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.floodalert.disafeter.model.UserDetails
import com.floodalert.disafeter.model.WeatherData
import com.floodalert.disafeter.model.repos.Result
import com.floodalert.disafeter.model.repos.WeatherRepository
import com.floodalert.disafeter.screens.weather.WeatherViewModel
import kotlinx.coroutines.launch

class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val weatherRepository: WeatherRepository,
    private val auth: FirebaseAuth
): ViewModel() {
    private val _requestLocation = MutableLiveData(false)
    private val _vibratePhone = MutableLiveData(false)
    private val _weatherData: MutableLiveData<WeatherData> by lazy {
        MutableLiveData<WeatherData>().also {
            viewModelScope.launch {
                val lat: Double = WeatherViewModel.BRGY_LATITUDE
                val lng: Double = WeatherViewModel.BRGY_LONGITUDE

                when (val result = weatherRepository.fetchWeatherData(lat, lng)) {
                    is Result.Success<WeatherData> -> it.postValue(result.data)
                    is Result.Error -> _errorMessage = result.exception.message.toString()
                }
            }
        }
    }

    private val _defaultLocation = if (isLoggedIn) "Weather" else "Register"
    private var _currentScreen by mutableStateOf(savedStateHandle["screen"] ?: _defaultLocation)
    private var _userLocation by mutableStateOf(Location(""))
    private var _currentUser by mutableStateOf<UserDetails?>(null)
    private var _hideActionButton by mutableStateOf(true)
    private var _hideNavbar by mutableStateOf(true)
    private var _errorMessage by mutableStateOf<String?>(null)

    val userLocation: Location get() = _userLocation
    val currentScreen: String get() = _currentScreen
    val requestLocation: LiveData<Boolean> = _requestLocation
    val vibratePhone: LiveData<Boolean> = _vibratePhone
    val weatherData: LiveData<WeatherData> = _weatherData
    val isLoggedIn: Boolean get() = auth.currentUser != null
    val currentUser: UserDetails? get() = _currentUser
    var hideActionButton: Boolean
        get() = _hideActionButton
        set(value) { _hideActionButton = value }

    var hideNavbar: Boolean
        get() = _hideNavbar
        set(value) { _hideNavbar = value }

    init {
        getCurrentUser()
    }

    fun setScreen(screen: String) {
        savedStateHandle["screen"] = screen
        _currentScreen = screen
    }

    fun setVibratePhone(value: Boolean) {
        _vibratePhone.value = value
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            // Log.d("execute", auth.currentUser.toString())
            if (auth.currentUser != null) {
                when (val result = weatherRepository.fetchUserDetails(auth.currentUser!!.uid)) {
                    is Result.Success<UserDetails?> -> {
                        Log.d("fetched", result.data.toString())
                        _currentUser = result.data
                    }
                    is Result.Error -> {
                        Log.d("fetched", result.exception.message.toString())
                        _errorMessage = result.exception.message
                    }
                }
            }
        }
    }

    fun requestUserLocation() {
        _requestLocation.value = false
        _requestLocation.value = true
    }

    fun setUserLocation(location: Location) {
        _userLocation = location
    }

    // Initialize view model
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db = Firebase.firestore
                val auth = Firebase.auth
                val savedStateHandle = createSavedStateHandle()
                val weatherRepository = WeatherRepository(db)
                MainViewModel(savedStateHandle, weatherRepository, auth)
            }
        }
    }
}