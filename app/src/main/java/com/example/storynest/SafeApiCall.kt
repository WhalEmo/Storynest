package com.example.storynest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public suspend fun <T> safeApiCall(
    call: suspend () -> retrofit2.Response<T>
): ResultWrapper<T> {
    return try {
        val response = withContext(Dispatchers.IO) { call() }

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ResultWrapper.Success(body)
            } else {
                ResultWrapper.Error(
                    "Boş response",
                    ErrorType.EMPTY_RESPONSE
                )
            }
        } else {
            ResultWrapper.Error(
                parseErrorBody(response.errorBody()?.string()),
                ErrorType.SERVER_ERROR
            )
        }
    } catch (e: java.io.IOException) {
        ResultWrapper.Error(
            "İnternet bağlantısı yok",
            ErrorType.NETWORK_ERROR
        )
    } catch (e: Exception) {
        ResultWrapper.Error(
            e.message ?: "Bilinmeyen hata",
            ErrorType.SERVER_ERROR
        )
    }
}
