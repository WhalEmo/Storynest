package com.example.storynest.Profile

sealed class ProfileScreenState{
    object Loading : ProfileScreenState()

    data class Success(
        val uiState: ProfileUiState
    ) : ProfileScreenState()

    data class Error(
        val message: String
    ) : ProfileScreenState()
}
