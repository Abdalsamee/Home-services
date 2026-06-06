package com.example.homeserv.ui.admin.providers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Category
import com.example.homeserv.data.model.Provider
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.repository.CategoryRepository
import com.example.homeserv.data.repository.ProviderRepository

class AdminProvidersViewModel : BaseViewModel() {

    private val providerRepo = ProviderRepository()
    private val categoryRepo = CategoryRepository()

    private val _providers = MutableLiveData<Resource<List<Provider>>>()
    val providers: LiveData<Resource<List<Provider>>> = _providers

    private val _provider = MutableLiveData<Resource<Provider>>()
    val provider: LiveData<Resource<Provider>> = _provider

    private val _categories = MutableLiveData<Resource<List<Category>>>()
    val categories: LiveData<Resource<List<Category>>> = _categories

    private val _saveResult = MutableLiveData<Resource<Unit>>()
    val saveResult: LiveData<Resource<Unit>> = _saveResult

    private val _deleteResult = MutableLiveData<Resource<Unit>>()
    val deleteResult: LiveData<Resource<Unit>> = _deleteResult

    fun loadProviders() {
        _providers.value = Resource.Loading
        launchSafe(onError = { _providers.value = Resource.Error(it) }) {
            _providers.value = providerRepo.getProviders()
        }
    }

    fun loadProvider(id: String) {
        launchSafe(onError = { _provider.value = Resource.Error(it) }) {
            _provider.value = providerRepo.getProvider(id)
        }
    }

    fun loadCategories() {
        launchSafe(onError = { _categories.value = Resource.Error(it) }) {
            _categories.value = categoryRepo.getCategories()
        }
    }

    fun addProvider(provider: Provider, imagePath: String) {
        _saveResult.value = Resource.Loading
        launchSafe(onError = { _saveResult.value = Resource.Error(it) }) {
            _saveResult.value = providerRepo.addProvider(provider, imagePath)
        }
    }

    fun updateProvider(provider: Provider, imagePath: String) {
        if (provider.id.isEmpty()) {
            _saveResult.value = Resource.Error("Provider ID is missing")
            return
        }
        _saveResult.value = Resource.Loading
        launchSafe(onError = { _saveResult.value = Resource.Error(it) }) {
            _saveResult.value = providerRepo.updateProvider(provider, imagePath)
        }
    }

    fun deleteProvider(id: String) {
        if (id.isEmpty()) return
        launchSafe(onError = { _deleteResult.value = Resource.Error(it) }) {
            _deleteResult.value = providerRepo.deleteProvider(id)
        }
    }
}