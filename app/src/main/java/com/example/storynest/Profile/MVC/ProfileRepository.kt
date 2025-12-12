package com.example.storynest.Profile.MVC

import com.example.storynest.Profile.MyProfile

class ProfileRepository {
    private val profileService = ProfileService()

    suspend fun loadMyProfile(): MyProfile {
        return profileService.getMyProfile()
    }
}