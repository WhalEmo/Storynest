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

class CommentsAdapter(
    private val listener: OnCommentInteractionListener
) : ListAdapter<commentResponse, CommentsAdapter.CommentViewHolder>(DiffCallback) {

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
        val comment = getItem(position)

        Glide.with(holder.itemView.context)
            .load(comment.user.profile)
            .placeholder(R.drawable.account_circle_24)
            .error(R.drawable.account_circle_24)
            .circleCrop()
            .into(holder.ppfoto)

        holder.txtUsername.text = comment.user.username
        holder.txtComment.text = comment.contents
        holder.txtTime.text = formatPostDate(comment.date)
        holder.txtLikeCount.text = comment.numberof_likes.toString()

        holder.btnLike.setImageResource(
            if (comment.isLiked)
                R.drawable.baseline_favorite_24
            else
                R.drawable.baseline_favorite_border_24
        )

        holder.btnLike.setOnClickListener {
            val position = holder.bindingAdapterPosition
            val oldItem = currentList[position]

            val newItem = oldItem.copy(
                isLiked = !oldItem.isLiked,
                numberof_likes = if (oldItem.isLiked)
                    oldItem.numberof_likes - 1
                else
                    oldItem.numberof_likes + 1
            )

            val newList = currentList.toMutableList()
            newList[position] = newItem

            submitList(newList)
            listener.onLikeClicked(oldItem.commentId)
        }

        holder.txtReply.setOnClickListener {
            listener.onReplyClicked(comment)
        }

        holder.rvSubComments.visibility =
            if (comment.isRepliesVisible) View.VISIBLE else View.GONE

        comment.replies?.let {
            holder.subAdapter.submitList(it)
        }

        holder.txtViewReplies.setOnClickListener {

            val position = holder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return@setOnClickListener

            val oldItem = currentList[position]

            val toggledItem = oldItem.copy(
                isRepliesVisible = !oldItem.isRepliesVisible
            )

            val newList = currentList.toMutableList()
            newList[position] = toggledItem

            submitList(newList)

            if (toggledItem.replies != null) {
                holder.subAdapter.submitList(toggledItem.replies)
                return@setOnClickListener
            }
            listener.onViewReplys(oldItem.commentId, reset = true) { subComments ->

                val updatedItem = toggledItem.copy(
                    replies = subComments
                )

                val updatedList = newList.toMutableList()
                updatedList[position] = updatedItem

                submitList(updatedList)
                holder.subAdapter.submitList(subComments)
            }
        }


        holder.bindScrollListener(comment.commentId)

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

        fun bindScrollListener(commentId: Long) {
            rvSubComments.clearOnScrollListeners()

            rvSubComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem =
                        layoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + firstVisibleItem >= totalItemCount &&
                        firstVisibleItem >= 0
                    ) {
                        listener.onViewReplys(commentId, reset = false) { newList ->

                            val position = bindingAdapterPosition
                            if (position == RecyclerView.NO_POSITION) return@onViewReplys

                            val oldItem = currentList[position]

                            val merged = oldItem.replies.orEmpty().toMutableList().apply {
                                addAll(newList)
                            }

                            val updatedItem = oldItem.copy(
                                replies = merged
                            )

                            val updatedList = currentList.toMutableList()
                            updatedList[position] = updatedItem

                            submitList(updatedList)
                            subAdapter.submitList(merged)
                        }

                    }
                }
            })
        }

    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<commentResponse>() {
            override fun areItemsTheSame(
                oldItem: commentResponse,
                newItem: commentResponse
            ) = oldItem.commentId == newItem.commentId

            override fun areContentsTheSame(
                oldItem: commentResponse,
                newItem: commentResponse
            ) = oldItem == newItem
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
            days >= 7 -> postTr.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            days >= 1 -> "$days gün"
            hours >= 1 -> "$hours saat"
            minutes >= 1 -> "$minutes dakika"
            else -> "Şimdi"
        }
    }
}
