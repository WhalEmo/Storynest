package com.example.storynest.Notification

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiController {

    @GET("follow/request/pending")
    suspend fun getFollowRequests(): List<FollowResponseDTO>

    @PUT("follow/request/{id}/accept")
    suspend fun acceptFollow(
        @Path("id") id: Long
    ): Response<FollowResponseDTO>

    @PUT("follow/request/{id}/reject")
    suspend fun rejectFollow(
        @Path("id") id: Long
    ): Response<FollowResponseDTO>


}