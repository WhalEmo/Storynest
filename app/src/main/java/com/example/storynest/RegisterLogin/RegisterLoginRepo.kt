package com.example.storynest.RegisterLogin

import com.example.storynest.ResultWrapper
import com.example.storynest.mapSuccess
import com.example.storynest.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class RegisterLoginRepo(
    private val api: RLController
) {
    suspend fun login(request: loginRequest): ResultWrapper<LoginResponse> =
        safeApiCall {
            api.login(request)
        }

    suspend fun register(request: registerRequest): ResultWrapper<UserResponse> =
        safeApiCall {
            api.register(request)
        }

    suspend fun resetPassword(email: String): ResultWrapper<String> =
        safeApiCall {
            api.forgotPassword(email)
        }.mapSuccess {
            "Emailinize şifre sıfırlama linki gönderildi."
        }

    suspend fun savePassword(
        token: String,
        request: ResetPasswordRequest
    ): ResultWrapper<String> =
        safeApiCall {
            api.saveNewPassword(token, request)
        }.mapSuccess {
            it.message
        }
}
