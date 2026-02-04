package com.example.storynest.Follow.MyFollowProcesses.MyFollowing

import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FollowingApiController {

    @GET("follow/userFollowing")
    suspend fun getUserFollowing(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<FollowUserResponseDTO>>
}