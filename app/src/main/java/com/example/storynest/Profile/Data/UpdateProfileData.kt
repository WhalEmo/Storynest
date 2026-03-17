package com.example.storynest.Profile.Data

data class UpdateProfileData(
    val userId: Long,
    val follower: Boolean,
    val following: Boolean,
    val pending: Boolean,
    val followersCount: Int,
    val followingCount: Int
)
