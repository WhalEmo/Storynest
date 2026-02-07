package com.example.storynest.HomePage


import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storynest.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.log
class PostAdapter(
    private val listener: OnPostInteractionListener
) : ListAdapter<postResponse, PostAdapter.PostViewHolder>(DIFF_CALLBACK) {

    interface OnPostInteractionListener {
        fun onLikeClicked(Id: Long)
        fun onReadMoreClicked(post: postResponse)

        fun getLikeUsers(Id: Long)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<postResponse>() {
            override fun areItemsTheSame(oldItem: postResponse, newItem: postResponse) =
                oldItem.post_id == newItem.post_id

            override fun areContentsTheSame(oldItem: postResponse, newItem: postResponse) =
                oldItem == newItem
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgUserProfile: ImageView = itemView.findViewById(R.id.imgUserProfile)
        val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        val txtCategory: TextView = itemView.findViewById(R.id.txtCategory)
        val coverImage: ImageView = itemView.findViewById(R.id.coverImage)
        val txtPostName: TextView = itemView.findViewById(R.id.txtPostName)
        val txtPostDate: TextView = itemView.findViewById(R.id.txtPostDate)
        val txtContents: TextView = itemView.findViewById(R.id.txtContents)
        val btnLike: ImageView = itemView.findViewById(R.id.btnLike)
        val btnComment: ImageView = itemView.findViewById(R.id.btnComment)
        val txtLikeCount: TextView = itemView.findViewById(R.id.txtLikeCount)
        val txtReadMore: TextView = itemView.findViewById(R.id.txtReadMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false))

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)

        // Profil ve cover image yükleme (Glide)
        Glide.with(holder.itemView.context)
            .load(post.user.profile)
            .placeholder(R.drawable.account_circle_24)
            .error(R.drawable.account_circle_24)
            .circleCrop()
            .into(holder.imgUserProfile)

        Glide.with(holder.itemView.context)
            .load(post.coverImage)
            .placeholder(R.drawable.outline_broken_image_24)
            .error(R.drawable.outline_broken_image_24)
            .into(holder.coverImage)

        holder.txtUsername.text = post.user.username
        holder.txtCategory.text = post.categories
        holder.txtPostName.text = post.postName
        holder.txtPostDate.text = formatPostDate(post.postDate)
        holder.txtContents.text = post.contents
        holder.txtLikeCount.text = post.numberof_likes.toString()

        holder.btnLike.setImageResource(
            if (post.liked) R.drawable.baseline_favorite_24
            else R.drawable.baseline_favorite_border_24
        )

        holder.btnLike.setOnClickListener {
            val newList = currentList.toMutableList()
            val post = newList[holder.bindingAdapterPosition]

            if (post.liked) {
                post.liked = false
                post.numberof_likes--
            } else {
                post.liked = true
                post.numberof_likes++
            }
            submitList(newList)
            listener.onLikeClicked(post.post_id)
        }

        holder.txtReadMore.setOnClickListener {
            listener.onReadMoreClicked(post)
        }
        holder.txtLikeCount.setOnClickListener {
            listener.getLikeUsers(post.post_id)
        }
        holder.btnComment.setOnClickListener {

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatPostDate(postDate: String): String {

        // Backend formatı: 2025-12-20T09:08:24.056984
        val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")

        // 1️⃣ String → LocalDateTime
        val postUtc = LocalDateTime.parse(postDate, parser)
            .atZone(ZoneOffset.UTC)

        // 2️⃣ UTC → Türkiye saati
        val postTr = postUtc.withZoneSameInstant(ZoneId.of("Europe/Istanbul"))

        // 3️⃣ Şu anki zamanı da Türkiye saatinde al
        val nowTr = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"))

        val days = ChronoUnit.DAYS.between(postTr, nowTr)
        val hours = ChronoUnit.HOURS.between(postTr, nowTr)
        val minutes = ChronoUnit.MINUTES.between(postTr, nowTr)

        return when {
            days >= 7 -> {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                postTr.format(formatter)
            }
            days >= 1 -> "$days gün önce"
            hours >= 1 -> "$hours saat önce"
            minutes >= 1 -> "$minutes dakika önce"
            else -> "Şimdi"
        }
    }



}