package com.example.storynest.Profile.MVC

import com.example.storynest.Api.BaseRepository
import com.example.storynest.ApiClient
import com.example.storynest.TestUserProvider

class ProfileService: BaseRepository() {
    private lateinit var token: String

    init {
        token = TestUserProvider.STATIC_TOKEN
    }

    val profileController = ApiClient.getClient(token).create(ProfileApiController::class.java)

}