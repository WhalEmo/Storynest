package com.example.storynest.Follow.ResponseDTO

data class FollowResponse(
    val userId: Long,
    val username: String,
    val bio: String?,
    val profileUrl: String?,
    val follower: Boolean,
    val following: Boolean,
    val pending: Boolean
)