package com.example.storynest.Follow.FollowOptions

import com.example.storynest.Profile.ProfileMode

interface FollowOptionClickListener {
    fun onViewProfile(userId: Long, profileMode: ProfileMode)
    fun onUnfollow(userId: Long)
    fun onBlock(userId: Long)
}