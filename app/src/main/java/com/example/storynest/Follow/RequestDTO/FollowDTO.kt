package com.example.storynest.Follow.RequestDTO

data class FollowDTO(
    val followingId : Long,
    val followedId : Long,
    val followed : Boolean
)
