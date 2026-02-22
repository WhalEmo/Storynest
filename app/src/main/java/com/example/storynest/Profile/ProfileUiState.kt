package com.example.storynest.Profile

data class ProfileUiState(

    val id: Int,
    val username: String,
    val name: String,
    val surname: String,
    val biography: String,
    val profileImageUrl: String?,
    val followers: Int,
    val following: Int,

    val isFollowing: Boolean,
    val isFollower: Boolean,

    val isOwnProfile: Boolean,
    val showEditButton: Boolean,
    val showFollowButton: Boolean,
    val showSettingsButton: Boolean,
    val showDotMenuButton: Boolean,
    val showMessageButton: Boolean,
    val showNotificationButton: Boolean,
    val showPendingRequestButton: Boolean,
    val btnFollowYour: Boolean,
    val btnShareProfile: Boolean
)
