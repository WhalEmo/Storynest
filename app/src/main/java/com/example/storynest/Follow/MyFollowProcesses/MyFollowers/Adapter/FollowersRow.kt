package com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter

import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO

sealed class FollowersRow {

    data class FollowersHeaderItem(
        val title: String
    ): FollowersRow()

    data class FollowerUserItem(
        val followUserResponseDTO: FollowUserResponseDTO
    ): FollowersRow()
}