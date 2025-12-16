package com.example.storynest.Notification

import retrofit2.http.GET

interface NotificationApiController {

    @GET("follow/request/pending")
    suspend fun getFollowRequests(): List<FollowResponseDTO>
}