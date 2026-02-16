package com.example.storynest.Follow.Adapter

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.storynest.Follow.Base.BaseFollowViewHolder
import com.example.storynest.Follow.FollowRow
import com.example.storynest.Follow.FollowViewType

import com.example.storynest.R


class FollowUserViewHolder(
    itemView: View,
    private val onAccept: (FollowRow.FollowUserItem) -> Unit,
    private val onSendMessage: (FollowRow.FollowUserItem) -> Unit,
    private val onCancelRequest: (FollowRow.FollowUserItem) -> Unit,
    private val onUnFollowMy: (FollowRow.FollowUserItem) -> Unit,
    private val onProfileClick: (FollowRow.FollowUserItem) -> Unit
): BaseFollowViewHolder(itemView) {


    private val yourFollowMe =
        itemView.findViewById<TextView>(R.id.yourFollowMe)
    private val sendingRequest =
        itemView.findViewById<TextView>(R.id.sendingRequest)
    private val sendMessage =
        itemView.findViewById<TextView>(R.id.sendMessage)
    private val unFollow =
        itemView.findViewById<ImageButton>(R.id.unFollow)
    private val dotMenu =
        itemView.findViewById<ImageButton>(R.id.dotMenu)

    private val actionButtons by lazy {
        listOf(yourFollowMe, sendMessage, sendingRequest)
    }



    init {
        yourFollowMe.setOnClickListener {
            currentItem?.let(onAccept)
        }
        sendMessage.setOnClickListener {
            currentItem?.let(onSendMessage)
        }
        sendingRequest.setOnClickListener {
            currentItem?.let(onCancelRequest)
        }
        unFollow.setOnClickListener {
            currentItem?.let(onUnFollowMy)
        }
        profileImage.setOnClickListener {
            currentItem?.let(onProfileClick)
        }
    }

    override fun bindSpecific(resource: FollowRow.FollowUserItem) {
        render(resource.visibleViews)
    }

    fun bind(resource: FollowRow.FollowUserItem) {
        super.bindBase(resource)
    }


    private fun render(visibleViews: Set<FollowViewType>) {
        yourFollowMe.isVisible = FollowViewType.ACCEPT in visibleViews
        sendMessage.isVisible = FollowViewType.MESSAGE in visibleViews
        sendingRequest.isVisible = FollowViewType.PENDING in visibleViews
        unFollow.isVisible = FollowViewType.UNFOLLOW in visibleViews
        dotMenu.isVisible = FollowViewType.DOT_MENU in visibleViews
    }





    private fun animateSwitch(target: View?) {
        val current = actionButtons.firstOrNull { it.isVisible }
        if (current == target) return

        actionButtons.forEach {
            it.animate().cancel()
        }

        current?.animate()
            ?.alpha(0f)
            ?.setDuration(150)
            ?.withEndAction {
                current.visibility = View.GONE
                current.alpha = 1f
            }
            ?.start()

        target?.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(150)
                .start()
        }
    }


}
