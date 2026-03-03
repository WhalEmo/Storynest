package com.example.storynest.Profile

import com.example.storynest.Profile.ProfileUiStates.ProfileBasicUiState
import com.example.storynest.Profile.ProfileUiStates.ProfileBlockUiState
import com.example.storynest.Profile.ProfileUiStates.ProfileUiState

sealed class ProfileScreenState{
    object Loading : ProfileScreenState()

    data class Success(
        val uiState: ProfileUiState
    ) : ProfileScreenState()

    data class Update(
        val uiState: ProfileBasicUiState
    ) : ProfileScreenState()

    data class Blocked(
        val uiState: ProfileBlockUiState
    ) : ProfileScreenState()


    data class Error(
        val message: String
    ) : ProfileScreenState()
}
