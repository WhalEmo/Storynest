package com.example.storynest.HomePage


import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storynest.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit



class PostAdapter(
    private var postList: List<postResponse>,
    private val listener: OnPostInteractionListener
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnPostInteractionListener {
        fun onLikeClicked(Id:Long)
        fun onReadMoreClicked(post: postResponse)
    }

    fun updateList(newList: List<postResponse>) {
        postList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postList.size
    }
    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgUserProfile: ImageView = itemView.findViewById(R.id.imgUserProfile)
        val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        val txtCategory: TextView=itemView.findViewById(R.id.txtCategory)
        val coverImage: ImageView=itemView.findViewById(R.id.coverImage)
        val txtPostName: TextView=itemView.findViewById(R.id.txtPostName)
        val txtPostDate: TextView=itemView.findViewById(R.id.txtPostDate)
        val txtContents: TextView=itemView.findViewById(R.id.txtContents)
        val btnLike: ImageView=itemView.findViewById(R.id.btnLike)
        val btnComment: TextView=itemView.findViewById(R.id.btnComment)
        val txtLikeCount: TextView=itemView.findViewById(R.id.txtLikeCount)
        val txtReadMore: TextView=itemView.findViewById(R.id.txtReadMore)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        val profileUrl = post.user.profile

        if (!profileUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(profileUrl)
                .placeholder(R.drawable.account_circle_24)
                .error(R.drawable.account_circle_24)
                .circleCrop()
                .into(holder.imgUserProfile)
        }else{
            holder.imgUserProfile.setImageResource(R.drawable.account_circle_24)
        }

        val coverImageUrl=post.coverImage
        if (!coverImageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(coverImageUrl)
                .placeholder(R.drawable.outline_broken_image_24)
                .error(R.drawable.outline_broken_image_24)
                .into(holder.coverImage)
        } else {
            holder.coverImage.setImageResource(R.drawable.outline_broken_image_24)
        }
        holder.txtUsername.text=post.user.username
        holder.txtCategory.text=post.categories
        holder.txtPostName.text=post.postName

        holder.txtPostDate.text = formatPostDate(post.postDate)
        holder.txtContents.text=post.contents
        holder.txtLikeCount.text=post.numberOfLikes.toString()

        var currentDrawable = holder.btnLike.drawable.constantState
        var likedDrawable = holder.itemView.context.getDrawable(R.drawable.baseline_favorite_24)?.constantState

        holder.btnLike.setOnClickListener {
            if (currentDrawable != null && currentDrawable == likedDrawable) {
                holder.txtLikeCount.text=(post.numberOfLikes+1).toString()
                holder.btnLike.setImageResource(R.drawable.baseline_favorite_border_24)
                listener.onLikeClicked(post.postId)
            } else {
                holder.txtLikeCount.text=(post.numberOfLikes-1).toString()
                holder.btnLike.setImageResource(R.drawable.baseline_favorite_24)
                listener.onLikeClicked(post.postId)
            }
        }
        holder.txtReadMore.setOnClickListener {
            listener.onReadMoreClicked(post)
        }

        holder.btnComment.setOnClickListener {

        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatPostDate(postDate: LocalDateTime): String {
        val now = LocalDateTime.now()

        val days = ChronoUnit.DAYS.between(postDate, now)
        val hours = ChronoUnit.HOURS.between(postDate, now)
        val minutes = ChronoUnit.MINUTES.between(postDate, now)

        return when {
            days >= 7 -> {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                postDate.format(formatter)
            }
            days >= 1 -> "${days} gün önce"
            hours >= 1 -> "${hours} saat önce"
            minutes >= 1 -> "${minutes} dakika önce"
            else -> "Şimdi"
        }
    }

}