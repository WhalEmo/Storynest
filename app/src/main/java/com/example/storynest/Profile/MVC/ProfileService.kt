package com.example.storynest.Profile.MVC

import com.example.storynest.ApiClient
import com.example.storynest.Profile.ProfileData
import com.example.storynest.TestUserProvider

class ProfileService {
    private lateinit var token: String

    init {
        token = TestUserProvider.STATIC_TOKEN
    }

    val profileController = ApiClient.getClient(token).create(ProfileApiController::class.java)

    suspend fun getMyProfile(): ProfileData{
        val response = profileController.getMyProfile()
        return ProfileData(
            id = response.userResponseDto.id,
            username = response.userResponseDto.username,
            email = response.userResponseDto.email,
            name = response.userResponseDto.name,
            surname = response.userResponseDto.surname,
            profile = response.userResponseDto.profile?: "",
            biography = response.userResponseDto.biography,
            followers = response.followerCount,
            following = response.followedCount
        )
    }
    suspend fun getUserProfile(userId: Long): ProfileData{
        val response = profileController.getUserProfile(userId)
        return ProfileData(
            id = response.userResponseDto.id,
            username = response.userResponseDto.username,
            email = response.userResponseDto.email,
            name = response.userResponseDto.name,
            surname = response.userResponseDto.surname,
            profile = response.userResponseDto.profile?: "",
            biography = response.userResponseDto.biography,
            followers = response.followerCount,
            following = response.followedCount
        )
    }
}