package com.example.storynest.Follow

sealed class FollowRow {

    data class FollowHeaderItem(
        val title: String
    ): FollowRow()




    data class FollowUserItem(
        val id: Long,
        val username: String,
        val biography: String?,
        val profile: String?,
        val actionState: FollowActionUiState
    ) : FollowRow()
}