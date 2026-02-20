package com.example.storynest.Profile.MVC

import android.util.Log
import com.example.storynest.Profile.ProfileData
import com.example.storynest.Profile.ProfileUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


object ProfileRepository{
    private val profileService = ProfileService()

    private val memoryCache = mutableMapOf<Long, ProfileData>()


    fun loadMyProfile(userId: Long): Flow<ProfileData> = flow{
        memoryCache[userId]?.let {
            emit(it)
        }
        val fresh = profileService.getMyProfile()

        memoryCache[userId] = fresh
        emit(fresh)
    }

    fun loadUserProfile(userId: Long): Flow<ProfileData> = flow {
        memoryCache[userId]?.let {
            emit(it)
        }
        val fresh = profileService.getUserProfile(userId)

        memoryCache[userId] = fresh
        emit(fresh)
    }

}