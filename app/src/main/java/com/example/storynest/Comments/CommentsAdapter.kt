package com.example.storynest.Comments

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storynest.HomePage.PostAdapter.Companion.DIFF_CALLBACK
import com.example.storynest.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CommentsAdapter(
    private val listener: OnCommentInteractionListener
) : PagingDataAdapter<commentResponse, CommentsAdapter.CommentViewHolder>(DIFF_CALLBACK){


    interface OnCommentInteractionListener {
        fun onLikeClicked(commentId: Long)
        fun onReplyClicked(comment: commentResponse)
        fun onViewReplys(
            commentId: Long,
            reset: Boolean,
            onResult: (List<commentResponse>) -> Unit
        )
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position) ?: return

        Glide.with(holder.itemView.context)
            .load(comment.user.profile)
            .placeholder(R.drawable.account_circle_24)
            .error(R.drawable.account_circle_24)
            .circleCrop()
            .into(holder.ppfoto)

        holder.txtUsername.text = comment.user.username
        holder.txtComment.text = comment.contents
        holder.txtTime.text = formatPostDate(comment.date)
        holder.txtLikeCount.text = comment.number_of_like.toString()

        Log.d("LIKE_DEBUG", "Yorum: ${comment.contents} - isLiked Verisi: ${comment.isLiked}")
        if(comment.isLiked){
            holder.btnLike.setImageResource(R.drawable.baseline_favorite_24)
        }else{
            holder.btnLike.setImageResource(R.drawable.baseline_favorite_border_24)
        }
        holder.btnLike.setOnClickListener {
            val current = getItem(holder.bindingAdapterPosition) ?: return@setOnClickListener

            current.isLiked = !current.isLiked
            if (current.isLiked) {
                current.number_of_like++
                holder.btnLike.setImageResource(R.drawable.baseline_favorite_24)
            } else {
                current.number_of_like--
                holder.btnLike.setImageResource(R.drawable.baseline_favorite_border_24)
            }
            holder.txtLikeCount.text = current.number_of_like.toString()

            // 2. Arka planda API'ye haber ver
            listener.onLikeClicked(current.comment_id)
        }

        holder.txtReply.setOnClickListener {
            listener.onReplyClicked(comment)
        }


        holder.txtViewReplies.setOnClickListener {


        }


    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ppfoto: ImageView = itemView.findViewById(R.id.ppfoto)
        val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        val txtComment: TextView = itemView.findViewById(R.id.txtComment)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val txtReply: TextView = itemView.findViewById(R.id.txtReply)
        val btnLike: ImageView = itemView.findViewById(R.id.btnLike)
        val txtLikeCount: TextView = itemView.findViewById(R.id.txtLikeCount)
        val txtViewReplies: TextView = itemView.findViewById(R.id.txtViewReplies)
        val rvSubComments: RecyclerView = itemView.findViewById(R.id.rvSubComments)

        val subAdapter = SubCommentsAdapter(listener)
        private val layoutManager = LinearLayoutManager(itemView.context)


        init {
            rvSubComments.apply {
                adapter = subAdapter
                layoutManager = this@CommentViewHolder.layoutManager
                visibility = View.GONE
            }
        }


    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<commentResponse>() {
            override fun areItemsTheSame(
                oldItem: commentResponse,
                newItem: commentResponse
            ): Boolean = oldItem.comment_id == newItem.comment_id

            override fun areContentsTheSame(
                oldItem: commentResponse,
                newItem: commentResponse
            ): Boolean = oldItem == newItem
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatPostDate(postDate: String): String {

        val postUtc = LocalDateTime.parse(
            postDate,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        ).atZone(ZoneOffset.UTC)

        val postTr = postUtc.withZoneSameInstant(ZoneId.of("Europe/Istanbul"))
        val nowTr = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"))

        val days = ChronoUnit.DAYS.between(postTr, nowTr)
        val hours = ChronoUnit.HOURS.between(postTr, nowTr)
        val minutes = ChronoUnit.MINUTES.between(postTr, nowTr)

        return when {
            days >= 7 -> postTr.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            days >= 1 -> "$days gün"
            hours >= 1 -> "$hours saat"
            minutes >= 1 -> "$minutes dakika"
            else -> "Şimdi"
        }
    }
}
