package com.example.storynest.RegisterLogin

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

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
    val biography: String?
)

data class LoginResponse(
    val user: UserResponse,
    val token: String
)
interface RLController {
    @POST("/api/users/login")
    fun login(@Body request: loginRequest): Call<LoginResponse>

    @POST("/api/users/register")
    fun register(@Body request: registerRequest): Call<UserResponse>
}