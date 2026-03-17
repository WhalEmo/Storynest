package com.example.storynest.Notification


enum class FollowRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    UNFOLLOW,
    BLOCKED,
    CANCEL
}

data class SimpleUserDTO(
    val userId: Long,
    val username: String,
    val profileURL: String?
)

data class FollowResponseDTO(
    val id: Long,
    val requester: SimpleUserDTO,
    val requested: SimpleUserDTO,
    val status: FollowRequestStatus,
    val date: String,
    val followingYou: Boolean,
    val myFollower: Boolean
)

