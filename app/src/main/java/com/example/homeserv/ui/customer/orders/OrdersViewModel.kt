package com.example.homeserv.ui.customer.orders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.ServiceRequest
import com.example.homeserv.data.repository.ProviderRepository
import com.example.homeserv.data.repository.RequestRepository

class OrdersViewModel : BaseViewModel() {

    private val requestRepo  = RequestRepository()
    private val providerRepo = ProviderRepository()

    private val _orders = MutableLiveData<Resource<List<ServiceRequest>>>()
    val orders: LiveData<Resource<List<ServiceRequest>>> = _orders

    private val _ratingResult = MutableLiveData<Resource<Unit>>()
    val ratingResult: LiveData<Resource<Unit>> = _ratingResult

    private var allOrders: List<ServiceRequest> = emptyList()
    private var activeFilter: String? = null
    private var currentCustomerId: String = ""

    fun loadOrders(customerId: String) {
        currentCustomerId = customerId
        _orders.value = Resource.Loading
        launchSafe(onError = { _orders.value = Resource.Error(it) }) {
            val result = requestRepo.getCustomerRequests(customerId)
            if (result is Resource.Success) {
                allOrders = result.data
            }
            applyFilter()
            if (result is Resource.Error) _orders.value = result
        }
    }

    fun filterByStatus(status: String?) {
        activeFilter = status
        applyFilter()
    }

    fun submitRating(requestId: String, providerId: String, rating: Float) {
        Log.d("Rating", "▶ submitRating called: requestId=$requestId providerId=$providerId rating=$rating")

        if (requestId.isEmpty() || providerId.isEmpty()) {
            _ratingResult.value = Resource.Error("Invalid request or provider ID")
            return
        }

        _ratingResult.value = Resource.Loading

        launchSafe(onError = {
            Log.e("Rating", "✗ Error: $it")
            _ratingResult.value = Resource.Error(it)
        }) {

            // ── Step 1: Save rating on the request document ───────
            Log.d("Rating", "Step 1: Saving rating to requests/$requestId")
            val saveResult = requestRepo.submitRating(requestId, rating)
            Log.d("Rating", "Step 1 result: $saveResult")

            if (saveResult is Resource.Error) {
                _ratingResult.value = saveResult
                return@launchSafe
            }

            // ── Step 2: Update local list immediately ─────────────
            allOrders = allOrders.map { order ->
                if (order.id == requestId) order.copy(rating = rating) else order
            }
            applyFilter()

            // ── Step 3: Fetch all rated requests for this provider ─
            Log.d("Rating", "Step 3: Fetching all ratings for provider=$providerId")
            val ratedResult = requestRepo.getCompletedRequestsForProvider(providerId)
            Log.d("Rating", "Step 3 result: $ratedResult")

            val newAverage: Double
            val newCount: Int

            if (ratedResult is Resource.Success && ratedResult.data.isNotEmpty()) {
                val ratings  = ratedResult.data.map { it.rating.toDouble() }
                newAverage   = ratings.average()
                newCount     = ratings.size
                Log.d("Rating", "Calculated: average=$newAverage count=$newCount")
            } else {
                // Fallback: use only this rating
                newAverage = rating.toDouble()
                newCount   = 1
                Log.d("Rating", "Fallback: using single rating=$newAverage")
            }

            // ── Step 4: Write average to providers collection ──────
            Log.d("Rating", "Step 4: Updating providers/$providerId rate=$newAverage reviewCount=$newCount")
            val updateResult = providerRepo.updateProviderRating(
                providerId  = providerId,
                newAverage  = newAverage.toFloat(),
                reviewCount = newCount
            )
            Log.d("Rating", "Step 4 result: $updateResult")

            if (updateResult is Resource.Error) {
                Log.e("Rating", "Failed to update provider rating: ${updateResult.message}")
                // Still consider it a success for the user — rating was saved
            }

            _ratingResult.value = Resource.Success(Unit)
            Log.d("Rating", "✓ Rating flow completed successfully")
        }
    }

    private fun applyFilter() {
        val filtered = if (activeFilter == null) allOrders
        else allOrders.filter { it.status == activeFilter }
        _orders.value = Resource.Success(filtered)
    }
}