package com.example.storynest.Follow.Base

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.storynest.Follow.FollowRow
import com.example.storynest.R
import com.google.android.material.imageview.ShapeableImageView

abstract class BaseFollowViewHolder(
    itemView: View
): RecyclerView.ViewHolder(itemView) {

    protected val profileImage: ShapeableImageView =
        itemView.findViewById(R.id.imgProfile)

    protected val username: TextView =
        itemView.findViewById(R.id.username)

    protected val biography: TextView =
        itemView.findViewById(R.id.biography)

    protected var currentItem: FollowRow.FollowUserItem? = null

    fun bindBase(resource: FollowRow.FollowUserItem) {
        currentItem = resource
        val body = resource.followUserResponseDTO

        username.text = body.username
        biography.text = body.biography

        profileImage.load(body.profile) {
            size(64)
            crossfade(true)
            placeholder(R.drawable.placeholder)
        }

        bindSpecific(resource)
    }

    protected abstract fun bindSpecific(resource: FollowRow.FollowUserItem)
}