package com.example.storynest.Profile

import java.io.Serializable

data class ProfileUiData(
    var id : Long,
    var username : String,
    var email : String,
    var name : String,
    var surname : String,
    var profile : String,
    var biography : String,
    var followers : Int,
    var following : Int,
    val isFollowing : Boolean,
    val isOwnProfile : Boolean,
    val isFollower: Boolean,
    val isPending: Boolean
) : Serializable
