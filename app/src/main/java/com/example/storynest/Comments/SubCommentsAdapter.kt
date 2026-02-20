package com.example.storynest.Comments

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storynest.Comments.CommentsAdapter.OnCommentInteractionListener
import com.example.storynest.R

class SubCommentsAdapter(private val listener:OnCommentInteractionListener) :
    ListAdapter<commentResponse, SubCommentsAdapter.SubCommentViewHolder>(DiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sub_comment, parent, false)
        return SubCommentViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: SubCommentViewHolder, position: Int) {
        val comment = getItem(position)

        val profileUrl = comment.user?.profile

        Glide.with(holder.itemView.context)
            .load(profileUrl)
            .placeholder(R.drawable.account_circle_24)
            .error(R.drawable.account_circle_24)
            .circleCrop()
            .into(holder.ppFoto)

        holder.txtUsername.text = comment.user.username
        holder.txtComment.text = comment.contents
        holder.txtTime.text = formatPostDate(comment.date)
        holder.txtLikeCount.text = comment.number_of_like.toString()
        holder.txtCommentTag.text=comment.parentCommentUsername

        holder.btnLike.setImageResource(
            if (comment.isLiked) R.drawable.baseline_favorite_24
            else R.drawable.baseline_favorite_border_24
        )

        holder.btnLike.setOnClickListener {
            val newList = currentList.toMutableList()
            val subComment = newList[holder.bindingAdapterPosition]

            if (subComment.isLiked) {
                subComment.isLiked = false
                subComment.number_of_like--
            } else {
                subComment.isLiked = true
                subComment.number_of_like++
            }
            submitList(newList)
            //listener.onLikeClicked(subComment.comment_id)
        }
/*
        holder.txtReply.setOnClickListener {
            listener.onReplyClicked(comment)
        }

 */
    }

    inner class SubCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ppFoto: ImageView = itemView.findViewById(R.id.ppfoto)
        val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        val txtComment: TextView = itemView.findViewById(R.id.txtComment)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val btnLike: ImageView = itemView.findViewById(R.id.btnLike)
        val txtLikeCount: TextView = itemView.findViewById(R.id.txtLikeCount)
        val txtCommentTag: TextView=itemView.findViewById(R.id.txtCommentTag)
        val txtReply: TextView=itemView.findViewById(R.id.txtReply)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<commentResponse>() {
            override fun areItemsTheSame(
                oldItem: commentResponse,
                newItem: commentResponse
            ) = oldItem.comment_id == newItem.comment_id

            override fun areContentsTheSame(
                oldItem: commentResponse,
                newItem: commentResponse
            ) = oldItem == newItem
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatPostDate(postDate: String): String {
        val parser = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        val postUtc = java.time.LocalDateTime.parse(postDate, parser)
            .atZone(java.time.ZoneOffset.UTC)
        val postTr = postUtc.withZoneSameInstant(java.time.ZoneId.of("Europe/Istanbul"))
        val nowTr = java.time.ZonedDateTime.now(java.time.ZoneId.of("Europe/Istanbul"))

        val days = java.time.temporal.ChronoUnit.DAYS.between(postTr, nowTr)
        val hours = java.time.temporal.ChronoUnit.HOURS.between(postTr, nowTr)
        val minutes = java.time.temporal.ChronoUnit.MINUTES.between(postTr, nowTr)

        return when {
            days >= 7 -> postTr.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            days >= 1 -> "$days gün"
            hours >= 1 -> "$hours saat"
            minutes >= 1 -> "$minutes dakika"
            else -> "Şimdi"
        }
    }
}
