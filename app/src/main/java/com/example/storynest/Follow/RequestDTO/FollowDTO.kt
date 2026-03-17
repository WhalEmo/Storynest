package com.example.storynest.Follow.RequestDTO

data class FollowDTO(
    val followerId : Long,
    val followingId : Long,
    val followed : Boolean
)
