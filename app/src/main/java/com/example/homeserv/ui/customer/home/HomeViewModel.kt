package com.example.homeserv.ui.customer.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Category
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.repository.CategoryRepository

class HomeViewModel : BaseViewModel() {

    private val categoryRepo = CategoryRepository()

    private val _categories = MutableLiveData<Resource<List<Category>>>()
    val categories: LiveData<Resource<List<Category>>> = _categories

    init { loadCategories() }

    fun loadCategories() {
        _categories.value = Resource.Loading
        launchSafe(onError = { _categories.value = Resource.Error(it) }) {
            _categories.value = categoryRepo.getCategories()
        }
    }
}