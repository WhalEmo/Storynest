package com.example.storynest

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> safeApiCall(
    call: suspend () -> T
): ResultWrapper<T> {

    return try {

        val result = withContext(Dispatchers.IO) {
            call()
        }

        ResultWrapper.Success(result)

    } catch (e: java.io.IOException) {

        ResultWrapper.Error(
            "İnternet bağlantısı yok",
            ErrorType.NETWORK_ERROR
        )

    } catch (e: retrofit2.HttpException) {

        val errorBody = e.response()?.errorBody()?.string()


        ResultWrapper.Error(
            errorBody ?: "Server hatası oluştu",
            ErrorType.SERVER_ERROR
        )

    } catch (e: Exception) {

        ResultWrapper.Error(e.message ?: "Bilinmeyen hata", ErrorType.SERVER_ERROR)

    }
}
