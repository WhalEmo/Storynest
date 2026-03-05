package com.example.storynest.Profile.Data

import com.example.storynest.Block.BlockStatus
import com.example.storynest.Profile.ProfileResponse

sealed class ProfileData {


    data class CreateProfileData(
        val data: ProfileResponse
    ):ProfileData()

    data class UpdateProfileData(
        val userId: Long,
        val follower: Boolean,
        val following: Boolean,
        val pending: Boolean,
        val followersCount: Int,
        val followingCount: Int
    ):ProfileData()

    data class BlockProfileData(
        val userId: Long,
        val blockStatus: BlockStatus
    ):ProfileData()
}