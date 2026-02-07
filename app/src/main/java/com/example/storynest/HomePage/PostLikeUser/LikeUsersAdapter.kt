package com.example.storynest.HomePage.PostLikeUser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storynest.Comments.UserResponse
import com.example.storynest.R
import com.google.android.material.button.MaterialButton


class LikeUsersAdapter ( private val listener: OnUserInteractionListener):
    ListAdapter<UserResponse, LikeUsersAdapter.LikeUserViewHolder>(DiffCallback) {
    interface OnUserInteractionListener {
        fun onFollowClicked(Id: Long)
        fun onMessageClicked(User: UserResponse)
        fun onSendingClicked(Id: Long)
        fun onLayoutClicked(Id: Long)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LikeUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_like_user, parent, false)
        return LikeUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: LikeUserViewHolder, position: Int) {
        val user = getItem(position)

        Glide.with(holder.itemView.context)
            .load(user.profile)
            .placeholder(R.drawable.account_circle_24)
            .error(R.drawable.account_circle_24)
            .circleCrop()
            .into(holder.imgUserProfile)

        holder.txtUsername.text=user.username

        if(user.isFollowing){
            holder.yourFollowMe.visibility=View.GONE
            holder.sendMessage.visibility= View.VISIBLE
        }else{
            holder.yourFollowMe.visibility=View.VISIBLE
            holder.sendMessage.visibility= View.GONE
        }

        holder.yourFollowMe.setOnClickListener {
            holder.yourFollowMe.visibility=View.GONE
            holder.sendingRequest.visibility= View.VISIBLE
            listener.onFollowClicked(user.id)
        }
        holder.sendMessage.setOnClickListener {
            listener.onMessageClicked(user)
        }
        holder.sendingRequest.setOnClickListener {
            holder.yourFollowMe.visibility=View.VISIBLE
            holder.sendingRequest.visibility= View.GONE
            listener.onSendingClicked(user.id)
        }
        holder.likeUserItemRoot.setOnClickListener {
            listener.onLayoutClicked(user.id)
        }


    }

    inner class LikeUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val imgUserProfile: ImageView=itemView.findViewById(R.id.imgUserProfile)
         val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
         val yourFollowMe: MaterialButton =itemView.findViewById(R.id.yourFollowMe)

         val sendingRequest: MaterialButton=itemView.findViewById(R.id.sendingRequest)
         val sendMessage: MaterialButton=itemView.findViewById(R.id.sendMessage)
         val likeUserItemRoot: LinearLayout=itemView.findViewById(R.id.likeUserItemRoot)

    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<UserResponse>() {
            override fun areItemsTheSame(
                oldItem: UserResponse,
                newItem: UserResponse
            )=oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: UserResponse,
                newItem: UserResponse
            )= oldItem == newItem
        }
    }
}
