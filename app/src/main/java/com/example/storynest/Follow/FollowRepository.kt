package com.example.storynest.Follow

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.storynest.ApiClient
import com.example.storynest.Follow.Paging.FollowPagingSource
import com.example.storynest.Follow.RequestDTO.FollowDTO
import com.example.storynest.Follow.ResponseDTO.FollowResponse
import com.example.storynest.GlobalEvent.FollowEvent
import com.example.storynest.GlobalEvent.EventCapsule
import com.example.storynest.GlobalEvent.FollowEventData
import com.example.storynest.Profile.Data.UpdateProfileData
import com.example.storynest.Profile.MVC.ProfileRepository
import com.example.storynest.TestUserProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import retrofit2.Response

object FollowRepository {
    private lateinit var token: String

    init {
        token = TestUserProvider.STATIC_TOKEN
    }
    val followApiController = ApiClient.getClient(token).create(FollowApiController::class.java)

    private val profileRepo: ProfileRepository = ProfileRepository

    private val _globalFollowEvents =
        MutableSharedFlow<Pair<Long, EventCapsule<FollowEventData>>>(extraBufferCapacity = 1)

    val globalFollowEvents = _globalFollowEvents

    fun getFollowPager(
        followType: FollowType,
        userId: Long?
    ): Pager<Int, FollowResponse>
    {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FollowPagingSource(
                    this,
                    followType,
                    userId
                )
            }
        )
    }


    suspend fun getOtherUserFollowing(userId: Long, page: Int = 0, size: Int = 20): List<FollowResponse>{
        val response = followApiController.getOtherUserFollowing(userId, page, size)
        return response.body() ?: emptyList()
    }
    suspend fun getOtherUserFollowers(userId: Long, page: Int = 0, size: Int = 20): List<FollowResponse>{
        val response = followApiController.getOtherUserFollowers(userId, page, size)
        return response.body() ?: emptyList()
    }


    suspend fun removeFollower(userId: Long): Response<FollowResponse> {
        Log.e("userId", userId.toString())
        val request = FollowDTO(
            followingId = TestUserProvider.STATIC_USER_ID,
            followerId = userId,
            followed = true
        )
        Log.e("request", request.toString())
        return followApiController.removeFollower(request)
    }

    suspend fun unfollow(userId: Long): Response<FollowResponse> {
        val request = FollowDTO(
            followerId = TestUserProvider.STATIC_USER_ID,
            followingId = userId,
            followed = true
        )
        return addGlobalFollowEvent(
            userId = userId,
            response = followApiController.unfollow(request),
            FollowEvent.UNFOLLOW
        )
    }

    suspend fun follow(userId: Long): Response<FollowResponse> {
        val request = FollowDTO(
            followerId = TestUserProvider.STATIC_USER_ID,
            followingId = userId,
            followed = true
        )
        return addGlobalFollowEvent(
            userId = userId,
            response = followApiController.follow(request),
            FollowEvent.FOLLOW
        )
    }

    private fun addGlobalFollowEvent(
        userId: Long,
        response: Response<FollowResponse>,
        followEvent: FollowEvent
    ) : Response<FollowResponse>
    {
        response.body()?.let {
            val profileData = profileRepo.updateUserProfile(it, followEvent)
            val followersCount = profileData?.followerCount ?: 0
            val followingCount = profileData?.followedCount ?: 0
            val updateProfileData = it.toUpdateProfileData(
                followersCount = followersCount,
                followingCount = followingCount
            )
            val eventCapsule = EventCapsule(
                data = updateProfileData,
                event = followEvent
            )
            _globalFollowEvents.tryEmit(
                userId to eventCapsule
            )
        }
        return response
    }

    private fun FollowResponse.toUpdateProfileData(
        followersCount: Int,
        followingCount: Int
    ): FollowEventData {
        val updateProfileData = UpdateProfileData(
            userId = userId,
            follower = follower,
            following = following,
            pending = pending,
            followersCount = followersCount,
            followingCount = followingCount
        )
        return FollowEventData(
            updateFollow = this,
            updateProfile = updateProfileData
        )
    }

}