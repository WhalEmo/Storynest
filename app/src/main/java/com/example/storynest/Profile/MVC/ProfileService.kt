package com.example.storynest.Profile.MVC

import com.example.storynest.ApiClient
import com.example.storynest.Profile.MyProfile

class ProfileService {
    private lateinit var token: String

    init {
        token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbXJ1bGxhaHV5Z24iLCJpYXQiOjE3NjU1NTY3MzksImV4cCI6MTc2ODE0ODczOX0.9_84CMcwqgPWlbMXXOrASxGNV1BqN3FH3-1pAQy25hQ"
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