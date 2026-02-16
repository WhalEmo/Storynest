package com.example.storynest.Profile.MVC

import com.example.storynest.Profile.ProfileResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ProfileApiController {
    @GET("profile/myProfile")
    suspend fun getMyProfile(): ProfileResponse

    @GET("profile/otherProfile/{targetId}")
    suspend fun getUserProfile(
        @Path("targetId") targetId: Long
    ): ProfileResponse

}