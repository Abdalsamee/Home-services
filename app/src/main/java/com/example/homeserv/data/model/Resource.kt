package com.example.homeserv.data.model


/**
 * A generic wrapper for UI state.
 * Every LiveData in the app emits Resource<T>.
 */
sealed class Resource<out T> {

    /** Operation is in progress — show loading indicator */
    object Loading : Resource<Nothing>()

    /** Operation succeeded — carry the result */
    data class Success<T>(val data: T) : Resource<T>()

    /** Operation failed — carry the error message */
    data class Error(val message: String) : Resource<Nothing>()

    // ── Convenience helpers ──────────────────────────────────────

    val isLoading get() = this is Loading
    val isSuccess get() = this is Success
    val isError   get() = this is Error

    fun dataOrNull(): T? = if (this is Success) data else null
    fun errorOrNull(): String? = if (this is Error) message else null
}