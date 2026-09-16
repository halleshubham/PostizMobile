package com.postiz.mobile.util

/**
 * Simple wrapper around the result of a network call so screens can render
 * loading / success / error states without leaking Retrofit/OkHttp types.
 */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}

inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) action(data)
    return this
}
