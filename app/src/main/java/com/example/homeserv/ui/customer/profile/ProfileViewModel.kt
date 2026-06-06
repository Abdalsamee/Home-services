package com.example.homeserv.ui.customer.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.User
import com.example.homeserv.data.repository.AuthRepository
import com.example.homeserv.data.repository.RequestRepository

class ProfileViewModel : BaseViewModel() {

    private val authRepo    = AuthRepository()
    private val requestRepo = RequestRepository()

    private val _user = MutableLiveData<Resource<User>>()
    val user: LiveData<Resource<User>> = _user

    private val _orderCount = MutableLiveData<Int>()
    val orderCount: LiveData<Int> = _orderCount

    fun loadProfile(uid: String) {
        _user.value = Resource.Loading
        launchSafe(onError = { _user.value = Resource.Error(it) }) {
            _user.value = authRepo.fetchUser(uid)
        }
    }

    fun loadOrderCount(uid: String) {
        launchSafe {
            val result = requestRepo.getCustomerRequests(uid)
            if (result is Resource.Success)
                _orderCount.value = result.data.size
        }
    }

    fun logout() = authRepo.logout()
}