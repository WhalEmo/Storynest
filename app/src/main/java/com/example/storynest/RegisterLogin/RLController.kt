package com.example.storynest.RegisterLogin

import android.R
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class loginRequest(val username:String,val password:String)
data class registerRequest(
    val username: String,
    val email: String,
    val password: String,
    val name: String,
    val surname: String,
    val profile: String?,
    val biography: String?
)
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val surname: String,
    val profile: String?,
    val date: String?,
    val biography: String?,
    val emailVerified: Boolean
)

data class LoginResponse(
    val user: UserResponse,
    val token: String
)

data class VerifyResponse(
    val message: String
)

data class ResetPasswordRequest(
    val newPassword: String,
    val confirmPassword: String
)


interface RLController {
    @POST("/api/users/login")
    suspend fun login(@Body request: loginRequest): LoginResponse

    @POST("/api/users/register")
    suspend fun register(@Body request: registerRequest): UserResponse

    @GET("/auth/verify")
    suspend fun verify(@Query("token") token: String): VerifyResponse

    @POST("/auth/forgotPassword")
    suspend fun forgotPassword(@Query("email") email: String): VerifyResponse


    @GET("/auth/reset-password")
    suspend fun verifyResetPassword(@Query("token") token: String): VerifyResponse

    @POST("/auth/savePassword")
    suspend fun saveNewPassword(@Query("token") token: String, @Body request: ResetPasswordRequest):VerifyResponse

}