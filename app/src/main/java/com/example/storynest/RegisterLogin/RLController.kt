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
    fun login(@Body request: loginRequest): Call<LoginResponse>

    @POST("/api/users/register")
    fun register(@Body request: registerRequest): Call<UserResponse>

    @GET("/auth/verify")
    fun verify(@Query("token") token: String): Call<VerifyResponse>

    @POST("/auth/forgotPassword")
    fun forgotPassword(@Query("email") email: String): Call<VerifyResponse>


    @GET("/auth/reset-password")
    fun verifyResetPassword(@Query("token") token: String): Call<VerifyResponse>

    @POST("/auth/savePassword")
    fun saveNewPassword(@Query("token") token: String, @Body request: ResetPasswordRequest): Call<VerifyResponse>

}