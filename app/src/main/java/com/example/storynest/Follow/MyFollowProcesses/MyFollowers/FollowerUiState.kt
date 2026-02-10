package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

sealed class FollowersUiState {
    object Loading : FollowersUiState()
    object Content : FollowersUiState()
    object Empty : FollowersUiState()
    data class Error(val message: String) : FollowersUiState()
}