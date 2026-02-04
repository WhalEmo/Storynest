package com.example.storynest.Profile.MVC

import com.example.storynest.ApiClient
import com.example.storynest.Profile.MyProfile
import com.example.storynest.TestUserProvider

class ProfileService {
    private lateinit var token: String

    init {
        token = TestUserProvider.STATIC_TOKEN
    }

    val profileController = ApiClient.getClient(token).create(ProfileApiController::class.java)

    suspend fun getMyProfile(): MyProfile{
        val response = profileController.getMyProfile()
        return MyProfile(
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