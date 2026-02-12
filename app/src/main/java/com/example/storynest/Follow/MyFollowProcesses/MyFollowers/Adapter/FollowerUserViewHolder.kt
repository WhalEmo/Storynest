package com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.storynest.Follow.Base.BaseFollowViewHolder
import com.example.storynest.Follow.FollowRow
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import com.example.storynest.Notification.FollowRequestStatus
import com.example.storynest.R
import com.google.android.material.imageview.ShapeableImageView


class FollowerUserViewHolder(
    itemView: View,
    private val onAccept: (FollowRow.FollowUserItem) -> Unit,
    private val onSendMessage: (FollowRow.FollowUserItem) -> Unit,
    private val onCancelRequest: (FollowRow.FollowUserItem) -> Unit,
    private val onUnFollowMy: (FollowRow.FollowUserItem) -> Unit,
): BaseFollowViewHolder(itemView) {


    private val yourFollowMe =
        itemView.findViewById<TextView>(R.id.yourFollowMe)
    private val sendingRequest =
        itemView.findViewById<TextView>(R.id.sendingRequest)
    private val sendMessage =
        itemView.findViewById<TextView>(R.id.sendMessage)
    private val unFollow =
        itemView.findViewById<ImageButton>(R.id.unFollow)


    private var lastItemId: Long? = null
    private var lastTargetViewId: Int? = null


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
    }

    override fun bindSpecific(resource: FollowRow.FollowUserItem) {
        val itemBody = resource.followUserResponseDTO
        val itemId = itemBody.id

        val target = resolveTargetView(itemBody)
        val targetId = target.id

        if (lastItemId != itemId) {
            setDirectState(target)
            lastItemId = itemId
            lastTargetViewId = targetId
            return
        }

        if (lastTargetViewId == targetId) return

        animateSwitchTo(target)

        lastTargetViewId = targetId
    }

    fun bind(resource: FollowRow.FollowUserItem) {
        this.bindBase(resource)
    }



    private fun animateSwitchTo(target: View) {
        val allViews = listOf(yourFollowMe, sendMessage, sendingRequest)
        val current = allViews.firstOrNull { it.visibility == View.VISIBLE }

        if (current == target) return

        current?.animate()?.cancel()
        target.animate().cancel()

        current?.animate()
            ?.alpha(0f)
            ?.setDuration(120)
            ?.withEndAction {
                current.visibility = View.GONE

                target.apply {
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate()
                        .alpha(1f)
                        .setDuration(120)
                        .start()
                }
            }
            ?.start()
    }


    private fun resolveTargetView(resource: FollowUserResponseDTO): View {
        return when {
            resource.myFollower && !resource.followingYou -> yourFollowMe
            resource.followInfo.status == FollowRequestStatus.ACCEPTED -> sendMessage
            resource.myFollower && resource.followInfo.status != FollowRequestStatus.PENDING -> sendMessage
            resource.followInfo.status == FollowRequestStatus.PENDING -> sendingRequest
            else -> yourFollowMe
        }
    }


    private fun setDirectState(target: View) {
        listOf(yourFollowMe, sendMessage, sendingRequest).forEach {
            it.animate().cancel()
            it.alpha = 1f
            it.visibility = if (it == target) View.VISIBLE else View.GONE
        }
    }




}
