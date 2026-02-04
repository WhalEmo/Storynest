package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import com.example.storynest.ApiClient
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersRow
import com.example.storynest.Follow.RequestDTO.FollowRequestDTO
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import com.example.storynest.Notification.FollowResponseDTO
import com.example.storynest.TestUserProvider
import retrofit2.Response

class MyFollowersService {
    private lateinit var token: String

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
}