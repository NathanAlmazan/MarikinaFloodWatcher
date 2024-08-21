package com.floodalert.disafeter.screens.evacuate

import android.location.Location
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.floodalert.disafeter.model.repos.Result
import com.floodalert.disafeter.model.EvacuationCenter
import com.floodalert.disafeter.model.repos.DirectionResponse
import com.floodalert.disafeter.model.repos.EvacuateRepository
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

class EvacuateViewModel(private val repository: EvacuateRepository): ViewModel() {
    private val _centers = mutableStateListOf<EvacuationCenter>()
    private var _selectedCenter by mutableStateOf(EvacuationCenter())
    private var _directions by mutableStateOf<DirectionResponse?>(null)
    private var _errorMessage by mutableStateOf<String?>(null)

    val centers: List<EvacuationCenter> get() = _centers
    val selectedCenter: EvacuationCenter get() = _selectedCenter
    val directions: DirectionResponse? get() = _directions
    val errorMessage: String? get() = _errorMessage

    init {
        fetchEvacuationCenters()
    }

    fun setSelectedCenter(selected: EvacuationCenter) {
        _selectedCenter = selected
    }

    fun resetDirection() {
        _directions = null
    }

    fun createEvacuationCenter(evacuationCenter: EvacuationCenter, imageFile: Uri) {
        viewModelScope.launch {
            when(val result = repository.createEvacuationCenter(evacuationCenter, imageFile)) {
                is Result.Success<EvacuationCenter> -> {
                    fetchEvacuationCenters()
                }
                is Result.Error -> {
                    _errorMessage = result.exception.message
                }
            }
        }
    }

    fun updateEvacuationCenter(evacuationCenter: EvacuationCenter, imageFile: Uri?) {
        viewModelScope.launch {
            when(val result = repository.updateEvacuationCenter(evacuationCenter, imageFile)) {
                is Result.Success<EvacuationCenter> -> {
                    fetchEvacuationCenters()
                }
                is Result.Error -> {
                    _errorMessage = result.exception.message
                }
            }
        }
    }

    fun deleteEvacuationCenter(evacuationCenter: EvacuationCenter) {
        viewModelScope.launch {
            when(val result = repository.deleteEvacuationCenter(evacuationCenter)) {
                is Result.Success<EvacuationCenter> -> fetchEvacuationCenters()
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    fun fetchDirection(start: LatLng, destination: LatLng) {
        viewModelScope.launch {
            when(val result = repository.fetchDirection(start, destination)) {
                is Result.Success<DirectionResponse> -> { _directions = result.data }
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    fun calculateClosestCenter(userLocation: Location) {
        viewModelScope.launch {
            var closest: EvacuationCenter = centers[0]
            var hypotenuse = sqrt(
                (userLocation.latitude - closest.latitude).pow(2) +
                        (userLocation.longitude - closest.longitude).pow(2)
            )

            for (center in centers) {
                val currentHypotenuse = sqrt(
                    (userLocation.latitude - center.latitude).pow(2) +
                            (userLocation.longitude - center.longitude).pow(2)
                )

                if (currentHypotenuse < hypotenuse) {
                    closest = center
                    hypotenuse = currentHypotenuse
                }
            }

            _selectedCenter = closest
        }
    }

    private fun fetchEvacuationCenters() {
        viewModelScope.launch {
            when(val result = repository.fetchEvacuationCenters()) {
                is Result.Success<List<EvacuationCenter>> -> {
                    _centers.clear()
                    _centers.addAll(result.data)
                }
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = Firebase.firestore
                val storage = Firebase.storage
                EvacuateViewModel(EvacuateRepository(database, storage))
            }
        }
    }
}