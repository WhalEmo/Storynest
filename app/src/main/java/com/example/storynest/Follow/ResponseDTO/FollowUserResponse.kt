package com.example.storynest.Follow.ResponseDTO

import com.example.storynest.Notification.FollowResponseDTO

data class FollowUserResponseDTO(
    val id: Long,
    val username: String,
    val profile: String?,
    val biography: String?,
    val followInfo: FollowResponseDTO,
    val followingYou: Boolean,
    val myFollower: Boolean
)