package com.example.storynest.Follow.ResponseDTO

data class FollowUserResponseDTO(
    val id: Long,
    val username: String,
    val profile: String?,
    val biography: String?,
    val followed: Boolean
)