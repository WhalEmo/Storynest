package com.example.storynest.Follow

import com.example.storynest.Follow.RequestDTO.FollowDTO
import com.example.storynest.Follow.RequestDTO.FollowRequestDTO
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import com.example.storynest.Notification.FollowResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FollowApiController {

    @GET("follow/userFollowed")
    suspend fun getUserFollowed(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<FollowUserResponseDTO>>


    @POST("follow/request")
    suspend fun sendFollowRequest(
        @Body request: FollowRequestDTO
    ): Response<FollowResponseDTO>

    @PUT("follow/request/{id}/cancel")
    suspend fun cancelFollow(
        @Path("id") id: Long
    ): Response<FollowResponseDTO>

    @POST("follow/removeFollower")
    suspend fun removeFollower(
        @Body request: FollowDTO
    ): Response<FollowDTO>

}