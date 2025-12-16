package com.example.storynest.Notification

import com.example.storynest.ApiClient

class NotificationService {

    private lateinit var token: String

    init {
        token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbXJ1bGxhaHV5Z24iLCJpYXQiOjE3NjU1NTY3MzksImV4cCI6MTc2ODE0ODczOX0.9_84CMcwqgPWlbMXXOrASxGNV1BqN3FH3-1pAQy25hQ"
    }

    val notificationController = ApiClient.getClient(token).create(NotificationApiController::class.java)

    suspend fun getFollowRequests(): List<FollowResponseDTO> {
        return notificationController.getFollowRequests()
    }

}