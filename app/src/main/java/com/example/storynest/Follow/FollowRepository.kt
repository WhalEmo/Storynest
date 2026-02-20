package com.example.storynest.Follow

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.storynest.ApiClient
import com.example.storynest.Follow.Paging.FollowPagingSource
import com.example.storynest.Follow.RequestDTO.FollowDTO
import com.example.storynest.Follow.RequestDTO.FollowRequestDTO
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import com.example.storynest.Notification.FollowRequestStatus
import com.example.storynest.Notification.FollowResponseDTO
import com.example.storynest.TestUserProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

object FollowRepository {
    private lateinit var token: String

    private var pagingSource: FollowPagingSource? = null

    init {
        token = TestUserProvider.STATIC_TOKEN
    }
    val followApiController = ApiClient.getClient(token).create(FollowApiController::class.java)


    suspend fun followMyFollower(myId: Long, userId: Long): Response<FollowResponseDTO> {
        return followApiController.sendFollowRequest(
            FollowRequestDTO(
                requesterId = myId,
                requestedId = userId
            )
        )
    }

    suspend fun cancelFollowRequest(followId: Long): Response<FollowResponseDTO> {
        return followApiController.cancelFollow(followId)
    }

    suspend fun getFollowers(page: Int = 0, size: Int = 20): List<FollowUserResponseDTO>{
        val response = followApiController.getUserFollowed(page, size)
        return response.body() ?: emptyList()
    }

    suspend fun getFollowing(page: Int = 0, size: Int = 20) : List<FollowUserResponseDTO>{
        val response = followApiController.getUserFollowing(page, size)
        return response.body() ?: emptyList()
    }
    suspend fun getOtherUserFollowing(userId: Long, page: Int = 0, size: Int = 20): List<FollowUserResponseDTO>{
        val response = followApiController.getOtherUserFollowing(userId, page, size)
        return response.body() ?: emptyList()
    }
    suspend fun getOtherUserFollowers(userId: Long, page: Int = 0, size: Int = 20): List<FollowUserResponseDTO>{
        val response = followApiController.getOtherUserFollowers(userId, page, size)
        return response.body() ?: emptyList()
    }


    suspend fun removeFollower(userId: Long): Response<FollowDTO> {
        Log.e("userId", userId.toString())
        val request = FollowDTO(
            followingId = TestUserProvider.STATIC_USER_ID.toLong(),
            followedId = userId,
            followed = true
        )
        Log.e("request", request.toString())
        return followApiController.removeFollower(request)
    }

}