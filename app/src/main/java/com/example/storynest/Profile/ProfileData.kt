package com.example.storynest.Profile

import java.io.Serializable

data class ProfileData(
    var id : Int,
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
    val isFollower: Boolean
) : Serializable
