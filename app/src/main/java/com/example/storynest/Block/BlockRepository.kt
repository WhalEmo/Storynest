package com.example.storynest.Block

import com.example.storynest.Api.BaseRepository
import com.example.storynest.Api.NetworkResult
import com.example.storynest.ApiClient
import com.example.storynest.Follow.FollowRepository
import com.example.storynest.GlobalEvent.FollowEvent
import com.example.storynest.TestUserProvider

object BlockRepository: BaseRepository() {
    private lateinit var token: String

    init {
        token = TestUserProvider.STATIC_TOKEN
    }
    private val followRepository = FollowRepository
    private val blockApiController = ApiClient.getClient(token).create(BlockApiController::class.java)


    suspend fun block(userId: Long): Boolean{
        val response = safeApiCall {
            blockApiController.block(userId)
        }
        if(response is NetworkResult.Success){
            followRepository.addBlockGlobalFollowEvent(
                userId = userId,
                followEvent = FollowEvent.UNFOLLOW
            )
        }
        return response is NetworkResult.Success
    }

    suspend fun unBlock(userId: Long): Boolean{
        return safeApiCall {
            blockApiController.unblock(userId)
        } is NetworkResult.Success
    }


}