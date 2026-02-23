package com.example.storynest.Follow

import android.util.Log
import com.example.storynest.ApiClient
import com.example.storynest.Follow.Paging.FollowPagingSource
import com.example.storynest.Follow.RequestDTO.FollowDTO
import com.example.storynest.Follow.RequestDTO.FollowRequestDTO
import com.example.storynest.Follow.ResponseDTO.FollowResponse
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import com.example.storynest.Notification.FollowResponseDTO
import com.example.storynest.TestUserProvider
import retrofit2.Response

object FollowRepository {
    private lateinit var token: String

    private var pagingSource: FollowPagingSource? = null

    init {
        token = TestUserProvider.STATIC_TOKEN
    }
    val followApiController = ApiClient.getClient(token).create(FollowApiController::class.java)



    suspend fun getOtherUserFollowing(userId: Long, page: Int = 0, size: Int = 20): List<FollowResponse>{
        val response = followApiController.getOtherUserFollowing(userId, page, size)
        return response.body() ?: emptyList()
    }
    suspend fun getOtherUserFollowers(userId: Long, page: Int = 0, size: Int = 20): List<FollowResponse>{
        val response = followApiController.getOtherUserFollowers(userId, page, size)
        return response.body() ?: emptyList()
    }


    suspend fun removeFollower(userId: Long): Response<FollowDTO> {
        Log.e("userId", userId.toString())
        val request = FollowDTO(
            followingId = TestUserProvider.STATIC_USER_ID.toLong(),
            followerId = userId,
            followed = true
        )
        Log.e("request", request.toString())
        return followApiController.removeFollower(request)
    }

    suspend fun unfollow(userId: Long): Response<FollowResponse> {
        val request = FollowDTO(
            followerId = TestUserProvider.STATIC_USER_ID.toLong(),
            followingId = userId,
            followed = true
        )
        return followApiController.unfollow(request)
    }

    suspend fun follow(userId: Long): Response<FollowResponse> {
        val request = FollowDTO(
            followerId = TestUserProvider.STATIC_USER_ID.toLong(),
            followingId = userId,
            followed = true
        )
        return followApiController.follow(request)

    }

}