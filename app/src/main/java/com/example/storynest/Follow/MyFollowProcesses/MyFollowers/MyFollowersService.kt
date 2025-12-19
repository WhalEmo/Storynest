package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import com.example.storynest.ApiClient
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersRow
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import retrofit2.Response

class MyFollowersService {
    private lateinit var token: String

    init {
        token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbXJ1bGxhaDciLCJpYXQiOjE3NjYxNDQ2MDgsImV4cCI6MTc2NjIzMTAwOH0.PkUy6LqGBH2sbq3G5zpTlJX-x9_R-hzcuZqrksfrtCM"
    }

    val followersApiController = ApiClient.getClient(token).create(FollowersApiController::class.java)

    suspend fun getMyFollowers(page: Int = 0, size: Int = 20): Response<List<FollowUserResponseDTO>> {
        return followersApiController.getUserFollowed(page, size)
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
}