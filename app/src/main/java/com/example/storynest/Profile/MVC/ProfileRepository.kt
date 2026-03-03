package com.example.storynest.Profile.MVC

import com.example.storynest.Api.BaseRepository
import com.example.storynest.Api.NetworkResult
import com.example.storynest.ApiClient
import com.example.storynest.Follow.ResponseDTO.FollowResponse
import com.example.storynest.GlobalEvent.FollowEvent
import com.example.storynest.Profile.ProfileData
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

    private val profileService = ProfileService()
    private val profileController = ApiClient.getClient(token).create(ProfileApiController::class.java)
    private val memoryCache = mutableMapOf<Long, ProfileResponse>()

    fun getUserProfile(userId: Long): ProfileResponse?{
        return memoryCache[userId]
    }

    fun updateUserProfile(data: FollowResponse, event: FollowEvent): ProfileResponse?{
        val userId = data.userId
        when(event){
            FollowEvent.FOLLOW -> {

            }
            FollowEvent.UNFOLLOW -> {

            }
        }
        memoryCache[userId]?.let {
            memoryCache[userId] = it.copy(
                follower = data.follower,
                following = data.following,
                pending = data.pending,
                followerCount = if(event == FollowEvent.FOLLOW) it.followerCount + 1 else it.followerCount - 1
            )
        }

        return memoryCache[userId]
    }

    /*
    private fun updateMyUserData(event: FollowEvent){
        memoryCache[TestUserProvider.STATIC_USER_ID]?.let {
            memoryCache[TestUserProvider.STATIC_USER_ID] = it.copy(
                followers = if(event == FollowEvent.FOLLOW) it.following + 1 else it.following - 1
            )
        }
    }

     */

    fun loadProfile(
        userId: Long,
        profileMode: ProfileMode
    ): Flow<NetworkResult<ProfileResponse>> = flow {

        val cached = memoryCache[userId]

        if (cached != null) {
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
            is NetworkResult.Error -> {
                emit(fresh)
            }
            NetworkResult.Loading -> emit(fresh)
            is NetworkResult.Success -> {
                val data = fresh.data
                memoryCache[userId] = data
                emit(NetworkResult.Success(data))
            }
        }
    }

}