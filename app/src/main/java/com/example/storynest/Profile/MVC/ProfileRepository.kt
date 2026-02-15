package com.example.storynest.Profile.MVC

import com.example.storynest.Profile.ProfileData

class ProfileRepository {
    private val profileService = ProfileService()

    suspend fun loadMyProfile(): ProfileData {
        return profileService.getMyProfile()
    }
}