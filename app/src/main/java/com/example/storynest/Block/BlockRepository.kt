package com.example.storynest.Block

import com.example.storynest.Api.BaseRepository
import com.example.storynest.Api.NetworkResult
import com.example.storynest.ApiClient
import com.example.storynest.Follow.FollowRepository
import com.example.storynest.GlobalEvent.FollowEvent
import com.example.storynest.Profile.MVC.ProfileRepository
import com.example.storynest.TestUserProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockRepository @Inject constructor(
    private val blockApiController: BlockApiController,
    private val profileRepo: ProfileRepository,
    private val followRepository: FollowRepository
): BaseRepository() {


    suspend fun block(userId: Long): Boolean{
        val response = safeApiCall {
            blockApiController.block(userId)
        }
        if(response is NetworkResult.Success){
            followRepository.addBlockGlobalFollowEvent(
                userId = userId,
                followEvent = FollowEvent.UNFOLLOW
            )
            profileRepo.updateBlockedOrUnBlockedProfile(
                userId = userId,
                blockStatus = BlockStatus.YOU_BLOCKER
            )
        }
        return response is NetworkResult.Success
    }

    suspend fun unBlock(userId: Long): Boolean{
        val response = safeApiCall {
            blockApiController.unblock(userId)
        }
        if(response is NetworkResult.Success){
            profileRepo.updateBlockedOrUnBlockedProfile(
                userId = userId,
                blockStatus = BlockStatus.UNBLOCKED
            )
        }
        return response is NetworkResult.Success
    }


}