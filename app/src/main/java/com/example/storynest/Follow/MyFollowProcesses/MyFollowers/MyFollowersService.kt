package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.storynest.ApiClient
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersRow
import com.example.storynest.Follow.RequestDTO.FollowDTO
import com.example.storynest.Follow.RequestDTO.FollowRequestDTO
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import com.example.storynest.Notification.FollowResponseDTO
import com.example.storynest.TestUserProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

class MyFollowersService {
    private lateinit var token: String

    private var pagingSource: FollowersPagingSource? = null

    init {
        token = TestUserProvider.STATIC_TOKEN
    }
    val followersApiController = ApiClient.getClient(token).create(FollowersApiController::class.java)

    suspend fun getMyFollowers(page: Int = 0, size: Int = 20): List<FollowersRow> {
        val response = followersApiController.getUserFollowed(page, size)

        if (response.isSuccessful) {
            val body = response.body() ?: emptyList()
            return myFollowersList(body)
        }

        return listOf(
            FollowersRow.FollowersHeaderItem("Takipçiler yüklenemedi")
        )
    }

    suspend fun followMyFollower(myId: Long, userId: Long): Response<FollowResponseDTO>{
        return followersApiController.sendFollowRequest(
            FollowRequestDTO(
                requesterId = myId,
                requestedId = userId
            )
        )
    }

    private fun myFollowersList(responseList: List<FollowUserResponseDTO>): List<FollowersRow>{
        val rows = mutableListOf<FollowersRow>()
        if (responseList.isEmpty()){
            rows.add(
                FollowersRow.FollowersHeaderItem("Henüz hiç takipçin yok!")
            )
            return rows
        }
        rows.add(
            FollowersRow.FollowersHeaderItem("Takipçi")
        )
        for (item in responseList) {
            rows.add(
                FollowersRow.FollowerUserItem(item)
            )
        }
        return rows
    }

    suspend fun cancelFollowRequest(followId: Long): Response<FollowResponseDTO>{
        return followersApiController.cancelFollow(followId)
    }

    fun getFollowers(): Flow<PagingData<FollowersRow>> {
        return Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20,
                prefetchDistance = 10
            )
        ) {
            FollowersPagingSource(followersApiController)
                .also { pagingSource = it }
        }.flow.map { pagingData ->
            pagingData
                .map {
                    FollowersRow.FollowerUserItem(it)
                }
                .insertSeparators { before, after ->
                    if (before == null && after != null) {
                        FollowersRow.FollowersHeaderItem("Takipçi")
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
        return followersApiController.removeFollower(request)
    }

}