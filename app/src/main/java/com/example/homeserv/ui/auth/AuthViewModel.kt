package com.example.homeserv.ui.auth



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.User
import com.example.homeserv.data.repository.AuthRepository

class AuthViewModel : BaseViewModel() {

    private val repo = AuthRepository()

    // ── Login ─────────────────────────────────────────────────────
    private val _loginResult = MutableLiveData<Resource<User>>()
    val loginResult: LiveData<Resource<User>> = _loginResult

    fun login(email: String, password: String) {
        _loginResult.value = Resource.Loading
        launchSafe(onError = { _loginResult.value = Resource.Error(it) }) {
            _loginResult.value = repo.login(email, password)
        }
    }

    // ── Register ──────────────────────────────────────────────────
    private val _registerResult = MutableLiveData<Resource<User>>()
    val registerResult: LiveData<Resource<User>> = _registerResult

    fun register(fullName: String, email: String, phone: String, password: String) {
        _registerResult.value = Resource.Loading
        launchSafe(onError = { _registerResult.value = Resource.Error(it) }) {
            _registerResult.value = repo.register(fullName, email, phone, password)
        }
    }

    // ── Logout ────────────────────────────────────────────────────
    fun logout() = repo.logout()
}