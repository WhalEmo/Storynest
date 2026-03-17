package com.example.storynest.Profile.MVC

import com.example.storynest.Api.ApiResponse
import com.example.storynest.Profile.ProfileResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProfileApiController {
    @GET("profile/myProfile")
    suspend fun getMyProfile(): Response<ApiResponse<ProfileResponse>>

    @GET("profile/otherProfile/{targetId}")
    suspend fun getUserProfile(
        @Path("targetId") targetId: Long
    ): Response<ApiResponse<ProfileResponse>>

}