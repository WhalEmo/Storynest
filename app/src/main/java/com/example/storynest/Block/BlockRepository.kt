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


    suspend fun block(userId: Long): Boolean{
        safeApiCall {
            blockApiController.block(userId)
        }
        val response = blockApiController.block(userId)
        return response.success
    }

    suspend fun unBlock(userId: Long): Boolean{
        val response = blockApiController.unblock(userId)
        return response.success
    }


}