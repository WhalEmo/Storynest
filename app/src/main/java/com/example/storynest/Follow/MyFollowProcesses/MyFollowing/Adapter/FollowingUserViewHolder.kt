package com.example.storynest.Follow.MyFollowProcesses.MyFollowing.Adapter

import android.view.View
import com.example.storynest.Follow.Base.BaseFollowViewHolder
import com.example.storynest.Follow.FollowRow
import com.example.storynest.R

class FollowingUserViewHolder(
    itemView: View,
    private val onSendMessage: (FollowRow.FollowUserItem) -> Unit,
    private val onUnFollow: (FollowRow.FollowUserItem) -> Unit,
) : BaseFollowViewHolder(itemView) {

    private val sendMessage =
        itemView.findViewById<View>(R.id.sendMessage)

    private val unFollow =
        itemView.findViewById<View>(R.id.unFollow)

    init {
        sendMessage.setOnClickListener {
            currentItem?.let(onSendMessage)
        }

        unFollow.setOnClickListener {
            currentItem?.let(onUnFollow)
        }
    }

    fun bind(resource: FollowRow.FollowUserItem) {
        this.bindBase(resource)
    }


    override fun bindSpecific(resource: FollowRow.FollowUserItem) {
        sendMessage.visibility = View.VISIBLE
        unFollow.visibility = View.VISIBLE
    }

}
