package com.floodalert.disafeter.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.floodalert.disafeter.model.UserDetails
import com.floodalert.disafeter.model.repos.AuthRepository
import com.floodalert.disafeter.model.repos.Result
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AuthRepository): ViewModel() {
    private var _uid by mutableStateOf("")
    private var _email by mutableStateOf("")
    private var _username by mutableStateOf("")
    private var _contact by mutableStateOf("")
    private var _submitError by mutableStateOf<String?>(null)

    var uid: String
        get() = _uid
        set(value) { _uid = value }

    var email: String
        get() = _email
        set(value) { _email = value }

    var username: String
        get() = _username
        set(value) { _username = value }

    var contact: String
        get() = _contact
        set(value) { _contact = value }

    val submitError: String? get() = _submitError

    fun editProfile() {
        viewModelScope.launch {
            when (val result = repository.editUser(
                username, email, contact, uid
            )) {
                is Result.Success<UserDetails> -> {
                    _email = result.data.email
                    _username = result.data.username
                    _contact = result.data.contact
                }
                is Result.Error -> _submitError = result.exception.message
            }
        }
    }

    fun logOut() {
        repository.logOutUser()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val auth = Firebase.auth
                val database = Firebase.firestore
                ProfileViewModel(AuthRepository(
                    database, auth
                ))
            }
        }
    }
}