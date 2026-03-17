package com.example.storynest

import androidx.appcompat.app.AppCompatActivity
import com.example.storynest.Follow.FollowListFragment
import com.example.storynest.Follow.FollowType
import com.example.storynest.Notification.NotificationFragment
import com.example.storynest.Profile.EditProfile.EditProfileFragment
import com.example.storynest.Profile.ProfileFragment
import com.example.storynest.Profile.ProfileMode
import com.example.storynest.Settings.SettingsFragment

object Navigator {
    fun openProfile(activity: AppCompatActivity, id: Long, mode: ProfileMode) {

        val tag = "profile_$id"
        val manager = activity.supportFragmentManager

        var fragment = manager.findFragmentByTag(tag)

        if (fragment == null) {
            fragment = ProfileFragment.newInstance(
                mode = mode,
                userId = id
            )
        }

        activity.openFragment(fragment, tag)
    }

    fun openSettings(activity: AppCompatActivity) {
        val tag = "settings"
        val manager = activity.supportFragmentManager

        var fragment = manager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = SettingsFragment()
        }

        activity.openFragment(fragment, tag)
    }

    fun openNotification(activity: AppCompatActivity) {
        val tag = "notification"
        val manager = activity.supportFragmentManager

        var fragment = manager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = NotificationFragment()
        }

        activity.openFragment(fragment, tag)
    }

    fun openFollowList(activity: AppCompatActivity, type: FollowType, userId: Long) {
        val tag = "${type.name}_$userId"
        val manager = activity.supportFragmentManager

        var fragment = manager.findFragmentByTag(tag)

        if (fragment == null) {
            fragment = FollowListFragment.newInstance(
                type = type,
                userId = userId
            )
        }

        activity.openFragment(fragment, tag)
    }

    fun openEditProfile(
        activity: AppCompatActivity,
        name: String,
        surname: String,
        bio: String,
        imageUrl: String
    ){
        val tag = "edit_profile"
        val manager = activity.supportFragmentManager
        var fragment = manager.findFragmentByTag(tag)
        if(fragment == null){
            fragment = EditProfileFragment.newInstance(
                name = name,
                surname = surname,
                bio = bio,
                imageUrl = imageUrl
            )
        }

        activity.openFragment(fragment, tag)
    }

}

