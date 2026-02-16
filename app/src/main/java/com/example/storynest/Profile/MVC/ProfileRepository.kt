package com.example.storynest.Profile.MVC

import android.util.Log
import com.example.storynest.Profile.ProfileData

class ProfileRepository {
    private val profileService = ProfileService()

    suspend fun loadMyProfile(): ProfileData {
        return profileService.getMyProfile()
    }

    suspend fun loadUserProfile(userId: Long): ProfileData {
        Log.d("ProfileRepository", "Loading user profile for user ID: $userId")
        return profileService.getUserProfile(userId)
    }

}