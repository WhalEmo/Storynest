package com.example.storynest.Follow

data class FollowParams(
    val followType: FollowType,
    val userId: Long?
)