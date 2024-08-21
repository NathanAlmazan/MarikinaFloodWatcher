package com.floodalert.disafeter.screens.bulletin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.floodalert.disafeter.model.Bulletin
import com.floodalert.disafeter.model.repos.BulletinRepository
import com.floodalert.disafeter.model.repos.Result
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class BulletinViewModel(private val repository: BulletinRepository): ViewModel() {
    private var _announcements = mutableStateListOf<Bulletin>()
    private var _selected by mutableStateOf<Bulletin?>(null)
    private var _errorMessage by mutableStateOf<String?>(null)

    val announcements: List<Bulletin> get() = _announcements
    val selected: Bulletin? get() = _selected
    val errorMessage: String? get() = _errorMessage

    init {
        fetchAllAnnouncements()
    }

    private fun fetchAllAnnouncements() {
        viewModelScope.launch {
            when (val result = repository.fetchAnnouncements()) {
                is Result.Success<List<Bulletin>> -> {
                    _announcements.clear()
                    _announcements.addAll(result.data)
                }
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    fun setSelectedBulletin(bulletin: Bulletin?) {
        _selected = bulletin
    }

    fun createAnnouncement(bulletin: Bulletin) {
        viewModelScope.launch {
            when (val result = repository.addAnnouncements(bulletin)) {
                is Result.Success<Bulletin> -> fetchAllAnnouncements()
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    fun updateAnnouncement(bulletin: Bulletin) {
        viewModelScope.launch {
            when (val result = repository.updateAnnouncement(bulletin)) {
                is Result.Success<Bulletin> -> fetchAllAnnouncements()
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    fun deleteAnnouncement(uid: String) {
        viewModelScope.launch {
            when (val result = repository.deleteAnnouncement(uid)) {
                is Result.Success<String> -> fetchAllAnnouncements()
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = Firebase.firestore
                BulletinViewModel(BulletinRepository(database))
            }
        }
    }
}