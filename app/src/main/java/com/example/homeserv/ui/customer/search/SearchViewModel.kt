package com.example.homeserv.ui.customer.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Provider
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.SortFilter
import com.example.homeserv.data.model.SortOption
import com.example.homeserv.data.repository.ProviderRepository

class SearchViewModel : BaseViewModel() {

    private val repo = ProviderRepository()

    private val _providers = MutableLiveData<Resource<List<Provider>>>()
    val providers: LiveData<Resource<List<Provider>>> = _providers

    private var currentFilter = SortFilter()

    // ── Initial load ──────────────────────────────────────────────

    fun loadAll() {
        currentFilter = SortFilter()
        search()
    }

    fun loadByCategory(categoryId: String) {
        currentFilter = SortFilter(categoryId = categoryId)
        search()
    }

    // ── Filter actions ────────────────────────────────────────────

    fun onQueryChanged(query: String) {
        currentFilter = currentFilter.copy(query = query)
        search()
    }

    fun onAvailableOnly(enabled: Boolean) {
        currentFilter = currentFilter.copy(onlyAvailable = enabled)
        search()
    }

    fun onSortChanged(option: SortOption) {
        currentFilter = currentFilter.copy(sortBy = option)
        search()
    }

    fun resetFilters() {
        currentFilter = currentFilter.copy(
            onlyAvailable = false,
            sortBy        = SortOption.RATING
        )
        search()
    }

    // ── Core search ───────────────────────────────────────────────

    private fun search() {
        _providers.value = Resource.Loading
        launchSafe(onError = { _providers.value = Resource.Error(it) }) {
            _providers.value = repo.searchProviders(currentFilter)
        }
    }
}