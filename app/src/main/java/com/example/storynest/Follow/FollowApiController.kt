package com.example.storynest.Follow

import com.example.storynest.Follow.RequestDTO.FollowDTO
import com.example.storynest.Follow.RequestDTO.FollowRequestDTO
import com.example.storynest.Follow.ResponseDTO.FollowResponse
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

    @POST("follow/removeFollower")
    suspend fun removeFollower(
        @Body request: FollowDTO
    ): Response<FollowResponse>

    @GET("follow/otherUserFollowing/{userId}")
    suspend fun getOtherUserFollowing(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<FollowResponse>>

    @GET("follow/otherUserFollowers/{userId}")
    suspend fun getOtherUserFollowers(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<FollowResponse>>

    @POST("follow/unfollow")
    suspend fun unfollow(
        @Body request: FollowDTO
    ): Response<FollowResponse>


    @POST("follow/follow")
    suspend fun follow(
        @Body request: FollowDTO
    ): Response<FollowResponse>


}