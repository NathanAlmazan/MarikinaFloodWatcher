package com.floodalert.disafeter.screens.authentication

import android.text.TextUtils
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.floodalert.disafeter.model.UserDetails
import com.floodalert.disafeter.model.repos.AuthRepository
import com.floodalert.disafeter.model.repos.Result
import kotlinx.coroutines.launch

sealed class FormError(val type: String, val message: String) {
    data object EmailError: FormError("email", "Invalid email.")
    data object ContactError: FormError("contact", "Invalid contact number.")
    data object PasswordError: FormError("password", "Password is too short.")
    data object ConfirmPasswordError: FormError("confirmPassword", "Password does not match.")
}

class AuthViewModel(private val repository: AuthRepository): ViewModel() {
    private var _email by mutableStateOf("")
    private var _username by mutableStateOf("")
    private var _contact by mutableStateOf("")
    private var _password by mutableStateOf("")
    private var _confirmPassword by mutableStateOf("")
    private var _registered by mutableStateOf(false)
    private var _logged by mutableStateOf(false)
    private val _users = mutableStateListOf<UserDetails>()
    private var _submitError by mutableStateOf<String?>(null)
    private var _formError by mutableStateOf<FormError?>(null)

    var email: String
        get() = _email
        set(value) { _email = value }

    var username: String
        get() = _username
        set(value) { _username = value }

    var contact: String
        get() = _contact
        set(value) { _contact = value }

    var password: String
        get() = _password
        set(value) { _password = value }

    var confirmPassword: String
        get() = _confirmPassword
        set(value) { _confirmPassword = value }

    val submitError: String? get() = _submitError
    val formError: FormError? get() = _formError
    val registered: Boolean get() = _registered
    val logged: Boolean get() = _logged
    val users: List<UserDetails> get() = _users

    fun signInUser() {
        if (!isValidEmail(email)) {
            _formError = FormError.EmailError
            return
        }

        if (password.length < 8) {
            _formError = FormError.PasswordError
            return
        }

        viewModelScope.launch {
            when (val result = repository.loginUserWithEmailPassword(
                email, password
            )) {
                is Result.Success<FirebaseUser?> -> _logged = true
                is Result.Error -> _submitError = result.exception.message
            }
        }
    }

    fun registerUser() {
        if (password != confirmPassword) {
            _formError = FormError.ConfirmPasswordError
            return
        }

        if (!isValidEmail(email)) {
            _formError = FormError.EmailError
            return
        }

        if (!isValidContact(contact)) {
            _formError = FormError.ContactError
            return
        }

        if (password.length < 8) {
            _formError = FormError.PasswordError
            return
        }

        viewModelScope.launch {
            when (val result = repository.registerUserWithEmailPassword(
                email, password
            )) {
                is Result.Success<FirebaseUser?> -> result.data?.uid?.let { saveUser(it) }
                is Result.Error -> _submitError = result.exception.message
            }
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            when(val result = repository.fetchAllUser()) {
                is Result.Success<List<UserDetails>> -> {
                    _users.clear()
                    _users.addAll(result.data)
                }
                is Result.Error -> _submitError = result.exception.message
            }
        }
    }

    fun setUserAsAdmin(user: UserDetails) {
        viewModelScope.launch {
            when(val result = repository.setAdmin(user)) {
                is Result.Success<UserDetails> -> fetchAllUsers()
                is Result.Error -> _submitError = result.exception.message
            }
        }
    }

    private fun saveUser(uid: String) {
        viewModelScope.launch {
            when (val result = repository.createUser(
                username, email, contact, uid
            )) {
                is Result.Success<UserDetails> -> _registered = true
                is Result.Error -> _submitError = result.exception.message
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return if (TextUtils.isEmpty(email)) false
        else android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidContact(contact: String): Boolean {
        return if (TextUtils.isEmpty(contact)) false
        else if (contact.length == 11) true
        else contact[0] == '0' && contact[1] == '9'
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val auth = Firebase.auth
                val database = Firebase.firestore
                AuthViewModel(AuthRepository(
                    database, auth
                ))
            }
        }
    }
}