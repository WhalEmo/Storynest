package com.example.storynest.Block

import com.example.storynest.Api.BaseRepository
import com.example.storynest.ApiClient
import com.example.storynest.TestUserProvider

object BlockRepository: BaseRepository() {
    private lateinit var token: String

    init {
        token = TestUserProvider.STATIC_TOKEN
    }
    private val blockApiController = ApiClient.getClient(token).create(BlockApiController::class.java)




}