package com.example.storynest.Follow

sealed class FollowActionUiState {
    object ShowAccept : FollowActionUiState()
    object ShowMessage : FollowActionUiState()
    object ShowPending : FollowActionUiState()
    object ShowUnfollow : FollowActionUiState()
}