package com.example.homeserv.ui.admin.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Provider
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.repository.ProviderRepository
import com.example.homeserv.data.repository.RequestRepository

class AdminDashboardViewModel : BaseViewModel() {

    private val requestRepo  = RequestRepository()
    private val providerRepo = ProviderRepository()

    private val _stats = MutableLiveData<Resource<Map<String, Any>>>()
    val stats: LiveData<Resource<Map<String, Any>>> = _stats

    private val _topProviders = MutableLiveData<Resource<List<Provider>>>()
    val topProviders: LiveData<Resource<List<Provider>>> = _topProviders

    private val _totalProviders = MutableLiveData<Int>()
    val totalProviders: LiveData<Int> = _totalProviders

    fun loadStats() {
        _stats.value = Resource.Loading
        launchSafe(onError = { _stats.value = Resource.Error(it) }) {
            _stats.value = requestRepo.getStats()
        }
    }

    fun loadTopProviders() {
        launchSafe {
            val result = providerRepo.getTopProviders(5)
            _topProviders.value = result
            val all = providerRepo.getProviders()
            if (all is Resource.Success)
                _totalProviders.value = all.data.size
        }
    }

    // One-time migration: rewrite rate field as Double for all providers
    fun fixProviderRatingTypes() {
        launchSafe {
            providerRepo.migrateRatesToDouble()
        }
    }
}