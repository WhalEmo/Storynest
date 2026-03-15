package com.example.storynest.HomePage
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storynest.R

class PostAdapter(
    private val listener: OnPostInteractionListener
) : PagingDataAdapter<HomePageUiModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    interface OnPostInteractionListener {
        fun onLikeClicked(post: postUiItem, likeView: View)
        fun onReadMoreClicked(post: postUiItem)

        fun getLikeUsers(Id: Long)
        fun clickComment(Id: Long, commentsPinned: Long);
        fun clickMenu(post: postUiItem,anchorView:View)

    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is HomePageUiModel.PostItem -> TYPE_POST
           // is HomePageUiModel.AdvertItem -> TYPE_ADVERT
            //is HomePageUiModel.SuggestedUserItem -> TYPE_SUGGESTED
            //is HomePageUiModel.SectionHeader -> TYPE_HEADER
            else -> throw IllegalArgumentException("Unknown type")
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_POST -> {
                val view = inflater.inflate(R.layout.item_post, parent, false)
                PostViewHolder(view)
            }
            /*

            TYPE_ADVERT -> {
                val view = inflater.inflate(R.layout., parent, false)
                AdvertViewHolder(view)
            }

            TYPE_SUGGESTED -> {
                val view = inflater.inflate(R.layout., parent, false)
                SuggestedViewHolder(view)
            }
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout., parent, false)
                HeaderViewHolder(view)
            }

             */

            else -> throw IllegalArgumentException("Invalid view type")
        }

    }
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = getItem(position) ?: return

        when (item) {
            is HomePageUiModel.PostItem -> (holder as PostViewHolder).bind(item.post)
           // is CommentsUiModel.ReplyItem -> (holder as ReplyViewHolder).bind(item.reply)
          //  is CommentsUiModel.ViewRepliesItem -> (holder as ViewRepliesViewHolder).bind(item.replyView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position) ?: return
        if (payloads.isEmpty()) {
            when (item) {
                is HomePageUiModel.PostItem -> (holder as PostViewHolder).bind(item.post)
                //is HomePageUiModel.SuggestedUserItem -> (holder as SuggestedViewHolder).bind(item.suggestedUser)
                //is HomePageUiModel.AdvertItem -> (holder as AdvertViewHolder).bind(item.itemAdvert)
                //is HomePageUiModel.SectionHeader -> (holder as HeaderViewHolder).bind(item.title, item.type)
            }
        } else {
            val changes = payloads[0] as Set<String>
            when (holder) {
                is PostViewHolder -> holder.updateWithPayload((item as HomePageUiModel.PostItem).post, changes)
                //is SuggestedViewHolder -> holder.updateWithPayload((item as HomePageUiModel.SuggestedUserItem).suggestedUser, changes)
                //is AdvertViewHolder -> holder.updateWithPayload((item as HomePageUiModel.AdvertItem).itemAdvert, changes)
                //is HeaderViewHolder -> holder.updateWithPayload((item as HomePageUiModel.SectionHeader).title, changes)
            }
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
        val btnPostMenu: ImageButton = itemView.findViewById(R.id.btnPostMenu)

        init {
            btnLike.setOnClickListener {
                val item=getPostAtCurrentPosition();
                item?.let { listener.onLikeClicked(it, btnLike) }
            }

            txtReadMore.setOnClickListener {
                val item=getPostAtCurrentPosition();
                item?.let { listener.onReadMoreClicked(it)}

            }
            txtLikeCount.setOnClickListener {
                val item=getPostAtCurrentPosition();
                item?.let {  listener.getLikeUsers(it.postId)}
            }
            btnComment.setOnClickListener {
                val item=getPostAtCurrentPosition();
                item?.let {  listener.clickComment(it.postId,it.pinnedCount)}
            }
            btnPostMenu.setOnClickListener {
                val item=getPostAtCurrentPosition()
                item?.let { listener.clickMenu(it, btnPostMenu) }
            }

        }

        private fun getPostAtCurrentPosition(): postUiItem? {
            val position = bindingAdapterPosition
            if(position == RecyclerView.NO_POSITION) return null
            val uiModel = getItem(position)
            return (uiModel as? HomePageUiModel.PostItem)?.post
        }

        fun bind(post: postUiItem) {
            Glide.with(itemView.context)
                .load(post.profileUrl)
                .placeholder(R.drawable.account_circle_24)
                .error(R.drawable.account_circle_24)
                .circleCrop()
                .into(imgUserProfile)

            Glide.with(itemView.context)
                .load(post.coverImage)
                .placeholder(R.drawable.outline_broken_image_24)
                .error(R.drawable.outline_broken_image_24)
                .into(coverImage)

            txtUsername.text = post.userName
            txtCategory.text = post.categories
            txtPostName.text = post.postName
            txtPostDate.text = post.postDate
            btnLike.setImageResource(post.likeIconRes)
            txtContents.text = post.contents
            txtLikeCount.text = post.numberof_likes
        }
        fun updateWithPayload(post: postUiItem, changes: Set<String>) {
            if (changes.contains("LIKE_COUNT")) { txtLikeCount.text = post.numberof_likes }
            if (changes.contains("LIKE_ICON")) { btnLike.setImageResource(post.likeIconRes) }
            if (changes.contains("CONTENT")) { txtContents.text = post.contents }
            if (changes.contains("POST_NAME")) { txtPostName.text = post.postName }
            if (changes.contains("COVER_IMAGE")) {
                Glide.with(itemView.context)
                    .load(post.coverImage)
                    .placeholder(R.drawable.outline_broken_image_24)
                    .into(coverImage)
            }
            if (changes.contains("CATEGORY")) { txtCategory.text = post.categories }

        }
    }

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_ADVERT = 1
        private const val TYPE_SUGGESTED= 2
        private const val TYPE_HEADER= 3
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HomePageUiModel>() {
            override fun areItemsTheSame(
                oldItem: HomePageUiModel,
                newItem: HomePageUiModel
            ) : Boolean {
                return when {
                    oldItem is HomePageUiModel.PostItem && newItem is HomePageUiModel.PostItem -> oldItem.post.postId == newItem.post.postId
                    //oldItem is HomePageUiModel.ReplyItem && newItem is CommentsUiModel.ReplyItem -> oldItem.reply.commentId == newItem.reply.commentId
                    // oldItem is HomePageUiModel.ViewRepliesItem && newItem is CommentsUiModel.ViewRepliesItem -> oldItem.replyView.parentCommentId == newItem.replyView.parentCommentId
                    else -> false
                }
            }
            override fun areContentsTheSame(
                oldItem: HomePageUiModel,
                newItem: HomePageUiModel
            ): Boolean=oldItem == newItem

            override fun getChangePayload(
                oldItem: HomePageUiModel,
                newItem: HomePageUiModel
            ): Any? {
                val diffBundle = mutableSetOf<String>()
                if (oldItem is HomePageUiModel.PostItem && newItem is HomePageUiModel.PostItem) {
                    if (oldItem.post.numberof_likes != newItem.post.numberof_likes) diffBundle.add("LIKE_COUNT")
                    if (oldItem.post.likeIconRes != newItem.post.likeIconRes) diffBundle.add("LIKE_ICON")
                    if (oldItem.post.contents != newItem.post.contents) diffBundle.add("CONTENT")
                    if (oldItem.post.postName != newItem.post.postName) diffBundle.add("POST_NAME")
                    if (oldItem.post.coverImage != newItem.post.coverImage) diffBundle.add("COVER_IMAGE")
                    if (oldItem.post.categories != newItem.post.categories) diffBundle.add("CATEGORY")

                }
                return if (diffBundle.isEmpty()) null else diffBundle
            }
        }
    }




}