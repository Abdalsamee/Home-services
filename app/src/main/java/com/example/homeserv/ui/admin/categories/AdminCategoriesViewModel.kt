package com.example.homeserv.ui.admin.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeserv.base.BaseViewModel
import com.example.homeserv.data.model.Category
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.repository.CategoryRepository

class AdminCategoriesViewModel : BaseViewModel() {

    private val repo = CategoryRepository()

    private val _categories = MutableLiveData<Resource<List<Category>>>()
    val categories: LiveData<Resource<List<Category>>> = _categories

    private val _category = MutableLiveData<Resource<Category>>()
    val category: LiveData<Resource<Category>> = _category

    private val _saveResult = MutableLiveData<Resource<Unit>>()
    val saveResult: LiveData<Resource<Unit>> = _saveResult

    private val _deleteResult = MutableLiveData<Resource<Unit>>()
    val deleteResult: LiveData<Resource<Unit>> = _deleteResult

    fun loadCategories() {
        _categories.value = Resource.Loading
        launchSafe(onError = { _categories.value = Resource.Error(it) }) {
            _categories.value = repo.getCategories()
        }
    }

    fun loadCategory(id: String) {
        launchSafe {
            val result = repo.getCategories()
            if (result is Resource.Success) {
                val cat = result.data.find { it.id == id }
                if (cat != null) _category.value = Resource.Success(cat)
            }
        }
    }

    fun addCategory(category: Category, imagePath: String) {
        _saveResult.value = Resource.Loading
        launchSafe(onError = { _saveResult.value = Resource.Error(it) }) {
            _saveResult.value = repo.addCategory(category, imagePath)
        }
    }

    fun updateCategory(category: Category, imagePath: String) {
        _saveResult.value = Resource.Loading
        launchSafe(onError = { _saveResult.value = Resource.Error(it) }) {
            _saveResult.value = repo.updateCategory(category, imagePath)
        }
    }

    fun deleteCategory(id: String) {
        launchSafe {
            _deleteResult.value = repo.deleteCategory(id)
        }
    }
}