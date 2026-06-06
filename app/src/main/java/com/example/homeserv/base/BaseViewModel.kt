package com.example.homeserv.base



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeserv.data.model.Resource
import kotlinx.coroutines.launch

/**
 * All ViewModels extend this.
 * Provides a safe launch helper that catches unhandled exceptions.
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * Launches a coroutine in viewModelScope.
     * Catches exceptions and calls [onError] with the message.
     */
    protected fun launchSafe(
        onError: ((String) -> Unit)? = null,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                onError?.invoke(e.localizedMessage ?: "Unexpected error")
            }
        }
    }

    /**
     * Convenience: unwrap a Resource and route to success/error callbacks.
     */
    protected fun <T> Resource<T>.handle(
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit
    ) {
        when (this) {
            is Resource.Success -> onSuccess(data)
            is Resource.Error   -> onError(message)
            is Resource.Loading -> { /* handled by LiveData emission */ }
        }
    }
}