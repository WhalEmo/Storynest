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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storynest.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class CommentsAdapter(private val listener:OnCommentInteractionListener):
        ListAdapter<commentResponse, CommentsAdapter.CommentViewHolder>(DiffCallback) {

    interface OnCommentInteractionListener {
        fun onLikeClicked(commentId: Long)
        fun onReplyClicked(comment: commentResponse)
        fun onViewReplys(commentId: Long, reset: Boolean, onResult: (List<commentResponse>) -> Unit)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comments = getItem(position)

        Glide.with(holder.itemView.context)
            .load(comments.user.profile)
            .placeholder(R.drawable.account_circle_24)
            .error(R.drawable.account_circle_24)
            .circleCrop()
            .into(holder.ppfoto)

        holder.txtUsername.text=comments.user.username
        holder.txtComment.text=comments.contents
        holder.txtTime.text = formatPostDate(comments.date)
        holder.txtLikeCount.text=comments.numberof_likes.toString()

        holder.btnLike.setImageResource(
            if (comments.isLiked) R.drawable.baseline_favorite_24
            else R.drawable.baseline_favorite_border_24
        )

        holder.btnLike.setOnClickListener {
            val newList = currentList.toMutableList()
            val comments = newList[holder.bindingAdapterPosition]

            if (comments.isLiked) {
                comments.isLiked = false
                comments.numberof_likes--
            } else {
                comments.isLiked = true
                comments.numberof_likes++
            }
            submitList(newList)
            listener.onLikeClicked(comments.commentId)
        }

        holder.txtReply.setOnClickListener {
            listener.onReplyClicked(comments)
        }
        holder.txtViewReplies.setOnClickListener {

            val subAdapter = SubCommentsAdapter()
            val subLayoutManager = LinearLayoutManager(holder.itemView.context)

            holder.rvSubComments.apply {
                layoutManager = subLayoutManager
                adapter = subAdapter
                visibility = View.VISIBLE
            }

            listener.onViewReplys(comments.commentId, reset = true) { subComments ->
                subAdapter.submitList(subComments)
            }

            holder.rvSubComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)

                    val visibleItemCount = subLayoutManager.childCount
                    val totalItemCount = subLayoutManager.itemCount
                    val firstVisibleItemPosition =
                        subLayoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0
                    ) {
                        listener.onViewReplys(comments.commentId, reset = false) { newList ->
                            val merged =
                                subAdapter.currentList.toMutableList().apply {
                                    addAll(newList)
                                }
                            subAdapter.submitList(merged)
                        }
                    }
                }
            })
        }
    }
    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ppfoto: ImageView=itemView.findViewById(R.id.ppfoto)
        val txtUsername: TextView=itemView.findViewById(R.id.txtUsername)
        val txtComment: TextView=itemView.findViewById(R.id.txtComment)
        val txtTime: TextView=itemView.findViewById(R.id.txtTime)
        val txtReply: TextView=itemView.findViewById(R.id.txtReply)
        val btnLike: ImageView=itemView.findViewById(R.id.btnLike)
        val txtLikeCount: TextView=itemView.findViewById(R.id.txtLikeCount)
        val txtViewReplies: TextView=itemView.findViewById(R.id.txtViewReplies)
        val rvSubComments: RecyclerView=itemView.findViewById(R.id.rvSubComments)

    }
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<commentResponse>() {
            override fun areItemsTheSame(
                oldItem: commentResponse,
                newItem: commentResponse
            )=oldItem.commentId == newItem.commentId

            override fun areContentsTheSame(
                oldItem: commentResponse,
                newItem: commentResponse
            )= oldItem == newItem
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatPostDate(postDate: String): String {

        val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")

        val postUtc = LocalDateTime.parse(postDate, parser)
            .atZone(ZoneOffset.UTC)

        val postTr = postUtc.withZoneSameInstant(ZoneId.of("Europe/Istanbul"))

        val nowTr = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"))

        val days = ChronoUnit.DAYS.between(postTr, nowTr)
        val hours = ChronoUnit.HOURS.between(postTr, nowTr)
        val minutes = ChronoUnit.MINUTES.between(postTr, nowTr)

        return when {
            days >= 7 -> {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                postTr.format(formatter)
            }
            days >= 1 -> "$days gün"
            hours >= 1 -> "$hours saat"
            minutes >= 1 -> "$minutes dakika"
            else -> "Şimdi"
        }
    }
}