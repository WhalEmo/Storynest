package com.example.storynest.Api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response

abstract class BaseRepository {

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<ApiResponse<T>>
    ): NetworkResult<T> {

        return try {

            val response = apiCall()
            Log.d("BaseRepository", "safeApiCall: ${response.body()}")

            if (response.isSuccessful) {

                val body = response.body()

                if (body?.success == true && body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    NetworkResult.Error(body?.message, body?.status)
                }

            } else {
                val errorJson = response.errorBody()?.string()
                val error = Gson().fromJson(errorJson, ApiResponse::class.java)
                NetworkResult.Error(
                    message = error.message,
                    status = error.status
                )
            }

        } catch (e: Exception) {

            NetworkResult.Error(
                message = e.localizedMessage,
                status = "NETWORK_EXCEPTION"
            )
        }
    }
}