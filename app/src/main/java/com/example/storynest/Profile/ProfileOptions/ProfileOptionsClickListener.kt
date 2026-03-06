package com.example.storynest.Profile.ProfileOptions

interface ProfileOptionsClickListener {
    fun onUnFollow(userId: Long)
    fun onBlock(userId: Long)
    fun onMessage(userId: Long)
    fun onShare(userId: Long)
    fun onUnBlock(userId: Long)
}