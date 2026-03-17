package com.example.storynest.HomePage

import com.example.storynest.Comments.userResponseDto

sealed class HomePageUiModel {

    data class PostItem(
        val post: postUiItem,
        val position: Int
    ): HomePageUiModel()
    /*data class SectionHeader(
        val title:String,
        val type: HeaderType
    ): HomePageUiModel()

    data class AdvertItem(
        val itemAdvert: postUiItem
    ): HomePageUiModel()

    data class SuggestedUserItem(
        val suggestedUser: userResponseDto
    ): HomePageUiModel()

    enum class HeaderType {
        FEED_START,
        SUGGESTED_USERS,
        SPONSORED
    }

     */

}