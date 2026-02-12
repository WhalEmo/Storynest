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
import com.example.storynest.Notification.FollowResponseDTO
import com.example.storynest.TestUserProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

class FollowRepository {
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

    fun getFollowers(): Flow<PagingData<FollowRow>> {
        return Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20,
                prefetchDistance = 10
            )
        ) {
            FollowPagingSource(
                loadAction = { page, size ->
                    followApiController.getUserFollowed(page, size).body().orEmpty()
                }
            )
                .also { pagingSource = it }
        }.flow.map { pagingData ->
            pagingData
                .map {
                    FollowRow.FollowUserItem(it)
                }
                .insertSeparators { before, after ->
                    if (before == null && after != null) {
                        FollowRow.FollowHeaderItem("Takipçi")
                    } else {
                        null
                    }
                }
        }
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