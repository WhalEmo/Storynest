package com.example.storynest.Comments

import android.os.Build
import android.util.Log
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
import com.example.storynest.Comments.viewModelhelper.CommentFormatter.createMentionText
import com.example.storynest.Comments.viewModelhelper.ReplyAction
import com.example.storynest.R
import kotlinx.coroutines.delay
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
        fun onLikeClicked(comment:commentUiItem)
        fun onLongClicked(commentId: Long,commentContents:String,userId: Long,postUserId:Long,isPin:Boolean,anchorView: View)
        fun onReplyClicked(comment: commentUiItem)
        fun onViewReplys(
            commentId: Long,
            totalSubComment:Long,
            reset: Boolean
        )
        fun hideRepyls(
            commentId:Long,
            totalSubComment:Long
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

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = getItem(position) ?: return

        when (item) {
            is CommentsUiModel.CommentItem -> (holder as CommentViewHolder).bind(item.comment)
            is CommentsUiModel.ReplyItem -> (holder as ReplyViewHolder).bind(item.reply)
            is CommentsUiModel.ViewRepliesItem -> (holder as ViewRepliesViewHolder).bind(item.replyView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position) ?: return

        if (payloads.isEmpty()) {
            when (item) {
                is CommentsUiModel.CommentItem -> (holder as CommentViewHolder).bind(item.comment)
                is CommentsUiModel.ReplyItem -> (holder as ReplyViewHolder).bind(item.reply)
                is CommentsUiModel.ViewRepliesItem -> (holder as ViewRepliesViewHolder).bind(item.replyView)
            }
        } else {
            val changes = payloads[0] as Set<String>
            when (holder) {
                is CommentViewHolder -> holder.updateWithPayload((item as CommentsUiModel.CommentItem).comment, changes)
                is ReplyViewHolder -> holder.updateWithPayload((item as CommentsUiModel.ReplyItem).reply, changes)
                is ViewRepliesViewHolder -> holder.updateWithPayload((item as CommentsUiModel.ViewRepliesItem).replyView, changes)
            }
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

        fun bind(comment: commentUiItem) {
            Glide.with(itemView.context)
                .load(comment.profileUrl)
                .placeholder(R.drawable.account_circle_24)
                .error(R.drawable.account_circle_24)
                .circleCrop()
                .into(ppfoto)

            txtUsername.text = comment.userName
            txtComment.text = comment.contents
            txtTime.text = comment.date
            txtLikeCount.text = comment.number_of_like
            btnLike.setImageResource(comment.likeIconRes)
            imgPin.visibility=comment.pinVisibility
            txtEdited.visibility=comment.editedVisibility
            txtEditDate.visibility=comment.editDateVisibility
            txtEditDate.text=comment.updateDate

            btnLike.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener

                val item = getItem(position)
                if (item is CommentsUiModel.CommentItem) {
                    listener.onLikeClicked(item.comment)
                }
            }

            txtReply.setOnClickListener {
                listener.onReplyClicked(comment)
            }


            layout.setOnLongClickListener {
                listener.onLongClicked(comment.commentId,comment.contents,comment.userId,comment.postUserId,comment.isPin,layout)
                true
            }
        }
        fun updateWithPayload(comment: commentUiItem, changes: Set<String>) {
            if (changes.contains("LIKE_COUNT")) txtLikeCount.text = comment.number_of_like
            if (changes.contains("LIKE_ICON")) btnLike.setImageResource(comment.likeIconRes)
            if (changes.contains("PIN_STATUS")) imgPin.visibility = comment.pinVisibility
            if (changes.contains("CONTENT")) txtComment.text = comment.contents
            if (changes.contains("EDIT_STATUS")) txtEdited.visibility = comment.editedVisibility
            if (changes.contains("EDIT_DATE_VISIBILITY"))txtEditDate.visibility = comment.editDateVisibility
            if (changes.contains("PIN_STATUS")) {
                imgPin.visibility = comment.pinVisibility
                layout.setOnLongClickListener {
                    listener.onLongClicked(
                        comment.commentId,
                        comment.contents,
                        comment.userId,
                        comment.postUserId,
                        comment.isPin,
                        layout
                    )
                    true
                }
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
        val imgPinreply: ImageView=itemView.findViewById(R.id.imgPinreply)

        fun bind(comment: commentUiItem) {
            Glide.with(itemView.context)
                .load(comment.profileUrl)
                .placeholder(R.drawable.account_circle_24)
                .error(R.drawable.account_circle_24)
                .circleCrop()
                .into(ppfotoreply)

            txtUsernamereply.text=comment.userName
            txtCommentreply.text = createMentionText(comment.parentCommentUsername,comment.contents)
            txtTimereply.text = comment.date
            txtLikeCountreply.text = comment.number_of_like
            btnLikereply.setImageResource(comment.likeIconRes)
            imgPinreply.visibility=comment.pinVisibility
            txtEditedreply.visibility=comment.editedVisibility
            txtEditDatereply.visibility=comment.editDateVisibility
            txtEditDatereply.text=comment.updateDate

            btnLikereply.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener

                val item = getItem(position)
                if (item is CommentsUiModel.ReplyItem) {
                    listener.onLikeClicked(item.reply)
                }
            }


            layoutreply.setOnLongClickListener {
                listener.onLongClicked(comment.commentId,comment.contents,comment.userId,comment.postUserId,comment.isPin,layoutreply)
                true
            }

        }
        fun updateWithPayload(comment: commentUiItem, changes: Set<String>) {
            if (changes.contains("LIKE_COUNT")) txtLikeCountreply.text = comment.number_of_like
            if (changes.contains("LIKE_ICON")) btnLikereply.setImageResource(comment.likeIconRes)
            if (changes.contains("PIN_STATUS")) imgPinreply.visibility = comment.pinVisibility
            if (changes.contains("CONTENT")) txtCommentreply.text = createMentionText(comment.parentCommentUsername,comment.contents)
            if (changes.contains("EDIT_STATUS")) txtEditedreply.visibility = comment.editedVisibility
            if (changes.contains("EDIT_DATE_VISIBILITY"))txtEditDatereply.visibility = comment.editDateVisibility
            if (changes.contains("PIN_STATUS")) {
                imgPinreply.visibility = comment.pinVisibility
                layoutreply.setOnLongClickListener {
                    listener.onLongClicked(
                        comment.commentId,
                        comment.contents,
                        comment.userId,
                        comment.postUserId,
                        comment.isPin,
                        layoutreply
                    )
                    true
                }
            }
        }

    }
    inner class ViewRepliesViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val txtViewReplies: TextView = itemView.findViewById(R.id.txtViewReplies)
            fun bind(item: viewReplysUiItem) {
                txtViewReplies.text = item.displayText
                setupClick(item)

        }
        fun updateWithPayload(item: viewReplysUiItem, changes: Set<String>) {
            if (changes.contains("REPLY_TEXT")) txtViewReplies.text = item.displayText
            if (changes.contains("REPLY_ACTION")) setupClick(item)
        }
        private fun setupClick(item: viewReplysUiItem) {
            txtViewReplies.setOnClickListener {
                when (item.nextAction) {
                    ReplyAction.LOAD_MORE -> listener.onViewReplys(item.parentCommentId, item.totalSubCount, !item.isLoadMore)
                    ReplyAction.HIDE -> listener.hideRepyls(item.parentCommentId, item.totalSubCount)
                }
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
                    oldItem is CommentsUiModel.CommentItem && newItem is CommentsUiModel.CommentItem -> oldItem.comment.commentId == newItem.comment.commentId
                    oldItem is CommentsUiModel.ReplyItem && newItem is CommentsUiModel.ReplyItem -> oldItem.reply.commentId == newItem.reply.commentId
                    oldItem is CommentsUiModel.ViewRepliesItem && newItem is CommentsUiModel.ViewRepliesItem -> oldItem.replyView.parentCommentId == newItem.replyView.parentCommentId
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: CommentsUiModel,
                newItem: CommentsUiModel
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: CommentsUiModel,
                newItem: CommentsUiModel
            ): Any? {
                val diffBundle = mutableSetOf<String>()
                if (oldItem is CommentsUiModel.CommentItem && newItem is CommentsUiModel.CommentItem) {
                    if (oldItem.comment.number_of_like != newItem.comment.number_of_like) diffBundle.add(
                        "LIKE_COUNT"
                    )

                    if (oldItem.comment.editedVisibility != newItem.comment.editedVisibility) diffBundle.add("EDIT_STATUS")
                    if (oldItem.comment.editDateVisibility != newItem.comment.editedVisibility) diffBundle.add("EDIT_DATE_VISIBILITY")

                    if (oldItem.comment.likeIconRes != newItem.comment.likeIconRes) diffBundle.add("LIKE_ICON")
                    if (oldItem.comment.pinVisibility != newItem.comment.pinVisibility) diffBundle.add(
                        "PIN_STATUS"
                    )
                    if (oldItem.comment.isPin != newItem.comment.isPin) diffBundle.add("PIN_STATUS")
                    if (oldItem.comment.contents != newItem.comment.contents) diffBundle.add("CONTENT")

                } else if (oldItem is CommentsUiModel.ReplyItem && newItem is CommentsUiModel.ReplyItem) {
                    if (oldItem.reply.number_of_like != newItem.reply.number_of_like) diffBundle.add(
                        "LIKE_COUNT"
                    )
                    if (oldItem.reply.editedVisibility != newItem.reply.editedVisibility) diffBundle.add("EDIT_STATUS")
                    if (oldItem.reply.editDateVisibility != newItem.reply.editedVisibility) diffBundle.add("EDIT_DATE_VISIBILITY")
                    if (oldItem.reply.likeIconRes != newItem.reply.likeIconRes) diffBundle.add("LIKE_ICON")
                    if (oldItem.reply.pinVisibility != newItem.reply.pinVisibility) diffBundle.add("PIN_STATUS")
                    if (oldItem.reply.isPin != newItem.reply.isPin) diffBundle.add("PIN_STATUS")
                    if (oldItem.reply.contents != newItem.reply.contents) diffBundle.add("CONTENT")

                } else if (oldItem is CommentsUiModel.ViewRepliesItem && newItem is CommentsUiModel.ViewRepliesItem) {
                    if (oldItem.replyView.displayText != newItem.replyView.displayText) diffBundle.add(
                        "REPLY_TEXT"
                    )
                    if (oldItem.replyView.nextAction != newItem.replyView.nextAction) diffBundle.add(
                        "REPLY_ACTION"
                    )
                }
                return if (diffBundle.isEmpty()) null else diffBundle
            }
        }
    }
}