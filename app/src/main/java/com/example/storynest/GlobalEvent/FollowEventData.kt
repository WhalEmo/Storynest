package com.example.storynest.GlobalEvent

import com.example.storynest.Follow.ResponseDTO.FollowResponse
import com.example.storynest.Profile.Data.UpdateProfileData

data class FollowEventData(
    val updateFollow: FollowResponse,
    val updateProfile: UpdateProfileData
)
