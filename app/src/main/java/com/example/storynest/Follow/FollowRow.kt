package com.example.storynest.Follow

import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO

sealed class FollowRow {

    data class FollowHeaderItem(
        val title: String
    ): FollowRow()

    data class FollowUserItem(
        val followUserResponseDTO: FollowUserResponseDTO
    ): FollowRow()
}