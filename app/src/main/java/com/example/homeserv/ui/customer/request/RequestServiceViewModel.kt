package com.example.homeserv.ui.customer.request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Provider
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.ServiceRequest
import com.example.homeserv.data.repository.ProviderRepository
import com.example.homeserv.data.repository.RequestRepository

class RequestServiceViewModel : BaseViewModel() {

    private val providerRepo = ProviderRepository()
    private val requestRepo  = RequestRepository()

    private val _provider = MutableLiveData<Resource<Provider>>()
    val provider: LiveData<Resource<Provider>> = _provider

    private val _submitResult = MutableLiveData<Resource<Unit>>()
    val submitResult: LiveData<Resource<Unit>> = _submitResult

    fun loadProvider(id: String) {
        _provider.value = Resource.Loading
        launchSafe(onError = { _provider.value = Resource.Error(it) }) {
            _provider.value = providerRepo.getProvider(id)
        }
    }

    fun submitRequest(
        customerId: String,
        customerName: String,
        customerPhone: String,
        provider: Provider,
        date: String,
        time: String,
        address: String,
        hours: Int,
        notes: String
    ) {
        _submitResult.value = Resource.Loading
        launchSafe(onError = { _submitResult.value = Resource.Error(it) }) {
            val request = ServiceRequest(
                customerId       = customerId,
                customerName     = customerName,
                customerPhone    = customerPhone,
                providerId       = provider.id,
                providerName     = provider.name,
                providerImageUrl = provider.imageUrl,
                categoryName     = provider.categoryName,
                address          = address,
                scheduledDate    = date,
                scheduledTime    = time,
                notes            = notes,
                estimatedHours   = hours,
                pricePerHour     = provider.pricePerHour
            )
            _submitResult.value = requestRepo.submitRequest(request)
        }
    }
}