package com.example.storynest.Follow

sealed class FollowRow {

    data class FollowHeaderItem(
        val title: String
    ): FollowRow()

    data class FollowUserItem(
        val id: Long,
        val requestId: Long?,
        val username: String,
        val biography: String?,
        val profile: String?,
        val visibleViews: Set<FollowViewType>
    ) : FollowRow()
}