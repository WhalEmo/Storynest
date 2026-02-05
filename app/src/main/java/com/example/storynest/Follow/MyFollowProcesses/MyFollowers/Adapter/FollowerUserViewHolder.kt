package com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.storynest.Notification.FollowRequestStatus
import com.example.storynest.R
import com.google.android.material.imageview.ShapeableImageView

class FollowerUserViewHolder(
    itemView: View,
    private val onAccept: (FollowersRow.FollowerUserItem) -> Unit,
    private val onSendMessage: (FollowersRow.FollowerUserItem) -> Unit,
    private val onCancelRequest: (FollowersRow.FollowerUserItem) -> Unit
): RecyclerView.ViewHolder(itemView) {


    private val profileImage =
        itemView.findViewById<ShapeableImageView>(R.id.imgProfile)

    private val username =
        itemView.findViewById<TextView>(R.id.username)

    private val biography =
        itemView.findViewById<TextView>(R.id.biography)

    private val yourFollowMe =
        itemView.findViewById<TextView>(R.id.yourFollowMe)

    private val sendingRequest =
        itemView.findViewById<TextView>(R.id.sendingRequest)

    private val sendMessage =
        itemView.findViewById<TextView>(R.id.sendMessage)

    private val actionContainer =
        itemView.findViewById<LinearLayout>(R.id.actionContainer)


    fun bind(item: FollowersRow.FollowerUserItem) {
        val resource = item.followUserResponseDTO
        username.text = resource.username
        biography.text = resource.biography

        profileImage.load(resource.profile){
            crossfade(true)
        }
        if(resource.myFollower && !resource.followingYou){
            yourFollowMe.visibility = View.VISIBLE
            sendMessage.visibility = View.GONE
            actionContainer.visibility = View.VISIBLE
            sendingRequest.visibility = View.GONE
        }
        else if(resource.followInfo.status == FollowRequestStatus.ACCEPTED){
            yourFollowMe.visibility = View.GONE
            sendMessage.visibility = View.VISIBLE
            actionContainer.visibility = View.GONE
            sendingRequest.visibility = View.GONE
        }
        else if(resource.followInfo.status == FollowRequestStatus.PENDING){
            yourFollowMe.visibility = View.GONE
            sendMessage.visibility = View.GONE
            actionContainer.visibility = View.VISIBLE
            sendingRequest.visibility = View.VISIBLE
        }
        else{
            yourFollowMe.visibility = View.VISIBLE
            sendMessage.visibility = View.GONE
            actionContainer.visibility = View.VISIBLE
            sendingRequest.visibility = View.GONE
        }

        yourFollowMe.setOnClickListener {
            onAccept(item)
        }

        sendMessage.setOnClickListener {
            onSendMessage(item)
        }

        sendingRequest.setOnClickListener {
            onCancelRequest(item)
        }

    }

}