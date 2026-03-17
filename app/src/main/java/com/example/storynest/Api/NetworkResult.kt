package com.example.storynest.Api

sealed class NetworkResult<out T> {

    data class Success<T>(val data: T) : NetworkResult<T>()

    data class Error(
        val message: String?,
        val status: String?
    ) : NetworkResult<Nothing>()

    object Loading : NetworkResult<Nothing>()
}