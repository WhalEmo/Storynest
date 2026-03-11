package com.example.storynest.HomePage

import com.example.storynest.Comments.userResponseDto

sealed class HomePageUiModel {
    data class HeaderItem(
        val title:String
    ): HomePageUiModel()
    data class PostItem(
        val post: postUiItem
    ): HomePageUiModel()

    data class AdvertItem(
        val itemAdvert: postUiItem
    ): HomePageUiModel()

    data class SuggestedUserItem(
        val suggestedUser: userResponseDto
    ): HomePageUiModel()


}