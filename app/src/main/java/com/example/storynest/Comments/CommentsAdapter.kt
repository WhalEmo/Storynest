package com.example.storynest.Comments

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingDataAdapter
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
) :PagingDataAdapter<CommentsUiModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {


    interface OnCommentInteractionListener {
        fun onLikeClicked(commentId: Long)
        fun onLongClicked(commentId: Long,commentContents:String,userId: Long,postUserId:Long,anchorView: View,onPinnedAlready: (() -> Unit)? = null)
        fun onReplyClicked(comment: commentResponse)
        fun onViewReplys(
            commentId: Long,
            reset: Boolean,
            onResult: (List<commentResponse>) -> Unit
        )
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CommentsUiModel.CommentItem -> TYPE_COMMENT
            is CommentsUiModel.ReplyItem -> TYPE_REPLY
            is CommentsUiModel.ViewRepliesItem -> TYPE_VIEW_REPLIES
            else -> throw IllegalArgumentException("Unknown type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_COMMENT -> {
                val view = inflater.inflate(R.layout.item_comment, parent, false)
                CommentViewHolder(view)
            }

            TYPE_REPLY -> {
                val view = inflater.inflate(R.layout.item_reply, parent, false)
                ReplyViewHolder(view)
            }

            TYPE_VIEW_REPLIES -> {
                val view = inflater.inflate(R.layout.item_view_replies, parent, false)
                ViewRepliesViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = getItem(position)) {

            is CommentsUiModel.CommentItem -> {
                (holder as CommentViewHolder).bind(item.comment)
            }

            is CommentsUiModel.ReplyItem -> {
                (holder as ReplyViewHolder).bind(item.reply)
            }

            is CommentsUiModel.ViewRepliesItem -> {
                (holder as ViewRepliesViewHolder).bind(item)
            }

            null -> {}
        }
    }

    inner class CommentViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val ppfoto: ImageView = itemView.findViewById(R.id.ppfoto)
        val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        val txtComment: TextView = itemView.findViewById(R.id.txtComment)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val txtReply: TextView = itemView.findViewById(R.id.txtReply)
        val btnLike: ImageView = itemView.findViewById(R.id.btnLike)
        val txtLikeCount: TextView = itemView.findViewById(R.id.txtLikeCount)

        val layout: ConstraintLayout = itemView.findViewById(R.id.layout)
        val txtEdited: TextView=itemView.findViewById(R.id.txtEdited)
        val txtEditDate: TextView=itemView.findViewById(R.id.txtEditDate)
        val imgPin: ImageView=itemView.findViewById(R.id.imgPin)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(comment: commentResponse) {
            Glide.with(itemView.context)
                .load(comment.user.profile)
                .placeholder(R.drawable.account_circle_24)
                .error(R.drawable.account_circle_24)
                .circleCrop()
                .into(ppfoto)

            txtUsername.text = comment.user.username
            txtComment.text = comment.contents
            txtTime.text = formatPostDate(comment.date)
            txtLikeCount.text = formatLike(comment.number_of_like)

            if (comment.isLiked) {
                btnLike.setImageResource(R.drawable.baseline_favorite_24)
            } else {
                btnLike.setImageResource(R.drawable.baseline_favorite_border_24)
            }

            if(comment.isPinned){
                imgPin.visibility=View.VISIBLE
            }else{
                imgPin.visibility=View.GONE
            }

            if(comment.isEdited){
                txtEdited.visibility=View.VISIBLE
                txtEditDate.visibility= View.VISIBLE
                txtEditDate.text=formatPostDate(comment.updateDate)
            }else{
                txtEdited.visibility= View.GONE
                txtEditDate.visibility= View.GONE
            }

            btnLike.setOnClickListener {
                val item = getItem(bindingAdapterPosition) ?: return@setOnClickListener
                when(item){
                    is CommentsUiModel.CommentItem->{
                        val current = item.comment
                        current.isLiked = !current.isLiked
                        if (current.isLiked) {
                            current.number_of_like++
                            btnLike.setImageResource(R.drawable.baseline_favorite_24)
                        } else {
                            current.number_of_like--
                            btnLike.setImageResource(R.drawable.baseline_favorite_border_24)
                        }
                        txtLikeCount.text = current.number_of_like.toString()
                        listener.onLikeClicked(current.comment_id)

                    }
                    is CommentsUiModel.ReplyItem -> {

                    }
                    else -> {

                    }
                }
            }

            txtReply.setOnClickListener {
                listener.onReplyClicked(comment)
            }


            layout.setOnLongClickListener {
                listener.onLongClicked(comment.comment_id,comment.contents,comment.user.id,comment.postUserId,layout){
                    imgPin.visibility= View.VISIBLE
                }
                true
            }
        }
    }

    inner class ReplyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val ppfotoreply: ImageView = itemView.findViewById(R.id.ppfotoreply)
        val txtUsernamereply: TextView = itemView.findViewById(R.id.txtUsernamereply)
        val txtCommentreply: TextView = itemView.findViewById(R.id.txtCommentreply)
        val txtTimereply: TextView = itemView.findViewById(R.id.txtTimereply)
        val txtReplyreply: TextView = itemView.findViewById(R.id.txtReplyreply)
        val btnLikereply: ImageView = itemView.findViewById(R.id.btnLikereply)
        val txtLikeCountreply: TextView = itemView.findViewById(R.id.txtLikeCountreply)

        val layoutreply: ConstraintLayout = itemView.findViewById(R.id.replylayout)
        val txtEditedreply: TextView=itemView.findViewById(R.id.txtEditedreply)
        val txtEditDatereply: TextView=itemView.findViewById(R.id.txtEditDatereply)
        val imgPinreply: ImageView=itemView.findViewById(R.id.imgPin)

        fun bind(comment: commentResponse) {

        }

    }
    inner class ViewRepliesViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val txtViewReplies: TextView = itemView.findViewById(R.id.txtViewReplies)
            fun bind(item: CommentsUiModel.ViewRepliesItem) {

                val text = if (item.isLoadMore) {
                    "${item.remainingCount} yanıt daha yükle"
                } else {
                    "${item.remainingCount} yanıtı gör"
                }

                txtViewReplies.text = text

                txtViewReplies.setOnClickListener {
                    listener.onViewReplys(
                        item.parentCommentId,
                        reset = false
                    ) {}
                }
        }

    }

    companion object {

        private const val TYPE_COMMENT = 0
        private const val TYPE_REPLY = 1
        private const val TYPE_VIEW_REPLIES = 2

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CommentsUiModel>() {

            override fun areItemsTheSame(
                oldItem: CommentsUiModel,
                newItem: CommentsUiModel
            ): Boolean {
                return when {
                    oldItem is CommentsUiModel.CommentItem &&
                            newItem is CommentsUiModel.CommentItem ->
                        oldItem.comment.comment_id == newItem.comment.comment_id

                    oldItem is CommentsUiModel.ReplyItem &&
                            newItem is CommentsUiModel.ReplyItem ->
                        oldItem.reply.comment_id == newItem.reply.comment_id

                    oldItem is CommentsUiModel.ViewRepliesItem &&
                            newItem is CommentsUiModel.ViewRepliesItem ->
                        oldItem.parentCommentId == newItem.parentCommentId

                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: CommentsUiModel,
                newItem: CommentsUiModel
            ): Boolean = oldItem == newItem
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatPostDate(postDate: String?): String {

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

    private fun formatLike(likeCount: Int): String {
        return when {
            likeCount < 9_999 -> {
                likeCount.toString()
            }

            likeCount < 1_000_000 -> {
                val value = likeCount / 1_000.0
                formatDecimal(value) + "Bin"
            }

            else -> {
                val value = likeCount / 1_000_000.0
                formatDecimal(value) + "M"
            }
        }
    }

    private fun formatDecimal(value: Double): String {
        val formatted = String.format("%.1f", value)
        return formatted.replace(".0", "").replace(".", ",")
    }
}