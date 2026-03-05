package com.example.storynest.Profile.MVC

import com.example.storynest.Api.BaseRepository
import com.example.storynest.Api.NetworkResult
import com.example.storynest.ApiClient
import com.example.storynest.Block.BlockStatus
import com.example.storynest.Follow.ResponseDTO.FollowResponse
import com.example.storynest.GlobalEvent.FollowEvent
import com.example.storynest.Profile.Data.ProfileData
import com.example.storynest.Profile.ProfileMode
import com.example.storynest.Profile.ProfileMode.*
import com.example.storynest.Profile.ProfileResponse
import com.example.storynest.TestUserProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


object ProfileRepository: BaseRepository(){
    private lateinit var token: String

    init {
        token = TestUserProvider.STATIC_TOKEN
    }

    private val profileController = ApiClient.getClient(token).create(ProfileApiController::class.java)
    private val memoryCache = mutableMapOf<Long, ProfileData>()


    fun updateUserProfile(data: FollowResponse, event: FollowEvent): ProfileResponse?{
        val userId = data.userId
        val profileData = memoryCache[userId]

        if(profileData is ProfileData.CreateProfileData){
            val currentData = profileData.data
            val newCount = when(event){
                FollowEvent.FOLLOW -> currentData.followerCount + 1
                FollowEvent.UNFOLLOW -> currentData.followerCount - 1
            }
            val updateInnerData = currentData.copy(
                followerCount = newCount,
                follower = data.follower,
                following = data.following,
                pending = data.pending
            )
            val updateProfileWrapper = ProfileData.CreateProfileData(data = updateInnerData)
            memoryCache[userId] = updateProfileWrapper
            return updateInnerData
        }
        return null
    }


    fun loadProfile(
        userId: Long,
        profileMode: ProfileMode
    ): Flow<NetworkResult<ProfileData>> = flow {

        val cached = memoryCache[userId]

        if(cached != null){
            emit(NetworkResult.Success(cached))
        }
        else{
            emit(NetworkResult.Loading)
        }

        val fresh = when(profileMode){
            MY_PROFILE -> {
                safeApiCall {
                    profileController.getMyProfile()
                }
            }
            USER_PROFILE -> {
                safeApiCall {
                    profileController.getUserProfile(userId)
                }
            }
        }

        when(fresh){
            is NetworkResult.Loading -> {
                emit(NetworkResult.Loading)
            }
            is NetworkResult.Error -> {
                val status = fresh.status?.let {
                    BlockStatus.valueOf(fresh.status)
                }
                if(status is BlockStatus){
                    val blockedProfile = ProfileData.BlockProfileData(
                        userId = userId,
                        blockStatus = status
                    )
                    memoryCache[userId] = blockedProfile
                    emit(NetworkResult.Success(blockedProfile))
                }
                else{
                    emit(fresh)
                }
            }
            is NetworkResult.Success -> {
                val data = fresh.data
                val profileData = ProfileData.CreateProfileData(data)
                memoryCache[userId] = profileData
                emit(NetworkResult.Success(profileData))
            }
        }
    }
}