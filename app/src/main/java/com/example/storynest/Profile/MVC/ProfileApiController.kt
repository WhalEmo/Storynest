package com.example.storynest.Profile.MVC

import com.example.storynest.Profile.ProfileResponse
import retrofit2.http.GET

interface ProfileApiController {
    @GET("profile/myProfile")
    suspend fun getMyProfile(): ProfileResponse

}