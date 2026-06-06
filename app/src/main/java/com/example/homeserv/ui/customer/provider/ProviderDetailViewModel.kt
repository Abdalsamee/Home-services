package com.example.homeserv.ui.customer.provider

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Provider
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.repository.ProviderRepository

class ProviderDetailViewModel : BaseViewModel() {

    private val repo = ProviderRepository()

    private val _provider = MutableLiveData<Resource<Provider>>()
    val provider: LiveData<Resource<Provider>> = _provider

    fun loadProvider(id: String) {
        _provider.value = Resource.Loading
        launchSafe(onError = { _provider.value = Resource.Error(it) }) {
            _provider.value = repo.getProvider(id)
        }
    }
}