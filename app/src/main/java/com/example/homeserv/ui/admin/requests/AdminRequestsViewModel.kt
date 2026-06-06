package com.example.homeserv.ui.admin.requests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.ServiceRequest
import com.example.homeserv.data.repository.RequestRepository

class AdminRequestsViewModel : BaseViewModel() {

    private val repo = RequestRepository()

    private val _requests = MutableLiveData<Resource<List<ServiceRequest>>>()
    val requests: LiveData<Resource<List<ServiceRequest>>> = _requests

    // Emits true while a status update is in progress
    private val _updating = MutableLiveData<Boolean>()
    val updating: LiveData<Boolean> = _updating

    private var allRequests: List<ServiceRequest> = emptyList()
    private var activeFilter: String? = null

    fun loadRequests() {
        _requests.value = Resource.Loading
        launchSafe(onError = { _requests.value = Resource.Error(it) }) {
            val result = repo.getAllRequests()
            if (result is Resource.Success) {
                allRequests = result.data
                applyFilter()
            } else {
                _requests.value = result
            }
        }
    }

    fun filterByStatus(status: String?) {
        activeFilter = status
        applyFilter()
    }

    fun updateStatus(requestId: String, status: String) {
        _updating.value = true
        launchSafe(onError = {
            _updating.value = false
            _requests.value = Resource.Error(it)
        }) {
            val result = repo.updateStatus(requestId, status)
            if (result is Resource.Success) {
                // Update local list immediately for instant UI feedback
                allRequests = allRequests.map { req ->
                    if (req.id == requestId) req.copy(status = status) else req
                }
                applyFilter()
            } else if (result is Resource.Error) {
                _requests.value = Resource.Error(result.message)
            }
            _updating.value = false
        }
    }

    private fun applyFilter() {
        val filtered = if (activeFilter == null) allRequests
        else allRequests.filter { it.status == activeFilter }
        _requests.value = Resource.Success(filtered)
    }
}