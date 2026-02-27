package com.example.storynest.Profile.MVC

import android.util.Log
import com.example.storynest.Follow.ResponseDTO.FollowResponse
import com.example.storynest.GlobalEvent.EventCapsule
import com.example.storynest.GlobalEvent.FollowEvent
import com.example.storynest.Profile.ProfileData
import com.example.storynest.Profile.ProfileMode
import com.example.storynest.Profile.ProfileMode.*
import com.example.storynest.Profile.ProfileUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


object ProfileRepository{
    private val profileService = ProfileService()

    private val memoryCache = mutableMapOf<Long, ProfileData>()

    fun getUserProfile(userId: Long): ProfileData?{
        return memoryCache[userId]
    }

    fun updateUserProfile(userId: Long, capsule: EventCapsule<FollowResponse>): ProfileData?{
        val data = capsule.data
        val event = capsule.event
        when(event){
            FollowEvent.FOLLOW -> {

            }
            FollowEvent.UNFOLLOW -> {

            }
        }
        memoryCache[userId]?.let {
            memoryCache[userId] = it.copy(
                isFollower = data.follower,
                isFollowing = data.following,
                isPending = data.pending,
                followers = if(event == FollowEvent.FOLLOW) it.followers + 1 else it.followers - 1,
            )
        }
        return memoryCache[userId]
    }

    fun loadProfile(
        userId: Long,
        profileMode: ProfileMode
    ): Flow<ProfileData> = flow {

        val cached = memoryCache[userId]

        if (cached != null) {
            emit(cached)
        }

        val fresh = when(profileMode){
            MY_PROFILE -> profileService.getMyProfile()
            USER_PROFILE -> profileService.getUserProfile(userId)
        }

        if (cached != fresh) {
            memoryCache[userId] = fresh
            emit(fresh)
        }
    }

}