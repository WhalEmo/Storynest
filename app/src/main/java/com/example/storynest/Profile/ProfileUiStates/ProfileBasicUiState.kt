package com.example.storynest.Profile.ProfileUiStates

data class ProfileBasicUiState(
    val showFollowButton: Boolean?,
    val showMessageButton: Boolean?,
    val showPendingRequestButton: Boolean,
    val btnFollowYour: Boolean,
    val followersCount: Int,
    val followingCount: Int
)