package com.example.storynest.RegisterLogin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterLoginRepo(
    private val api: RLController
) {
    suspend fun login(request: loginRequest): LoginResponse {
        val response = withContext(Dispatchers.IO) {
            api.login(request).execute()
        }

        if (response.isSuccessful) {
            return response.body()
                ?: throw Exception("Boş response")
        } else {
            throw Exception(response.errorBody()?.string())
        }
    }

    suspend fun register(request: registerRequest): UserResponse {
        val response = withContext(Dispatchers.IO) {
            api.register(request).execute()
        }
        if (response.isSuccessful) {
            return response.body()
                ?: throw Exception("Boş response")
        } else {
            throw Exception(response.errorBody()?.string())
        }
    }
}
