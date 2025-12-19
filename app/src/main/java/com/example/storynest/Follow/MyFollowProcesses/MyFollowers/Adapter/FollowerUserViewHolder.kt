package com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.Notification.Adapter.NotificationRow

class FollowerUserViewHolder(
    itemView: View,
    private val onAccept: (FollowersRow.FollowerUserItem) -> Unit,
    private val onSendMessage: (FollowersRow.FollowerUserItem) -> Unit
): RecyclerView.ViewHolder(itemView) {


    fun bind(item: FollowersRow.FollowerUserItem) {
        val resource = item.followUserResponseDTO


    }

}