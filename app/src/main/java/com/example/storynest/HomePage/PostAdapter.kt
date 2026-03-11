package com.example.storynest.HomePage


import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storynest.Comments.commentUiItem
import com.example.storynest.R

class PostAdapter(
    private val listener: OnPostInteractionListener
) : PagingDataAdapter<HomePageUiModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    interface OnPostInteractionListener {
        fun onLikeClicked(Id: Long)
        fun onReadMoreClicked(post: postResponse)

        fun getLikeUsers(Id: Long)
        fun clickComment(Id: Long,commentsPinned:Long);
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is HomePageUiModel.PostItem -> TYPE_POST
            is HomePageUiModel.AdvertItem -> TYPE_ADVERT
            is HomePageUiModel.SuggestedUserItem -> TYPE_SUGGESTED
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

            TYPE_ADVERT -> {
                val view = inflater.inflate(R.layout., parent, false)
                AdvertViewHolder(view)
            }

            TYPE_SUGGESTED -> {
                val view = inflater.inflate(R.layout., parent, false)
                SuggestedViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }

    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when (item){
            is HomePageUiModel.PostItem -> (holder as PostViewHolder).bind(item.post)
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


        fun bind(post: postUiItem) {


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false))

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)

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
            val index = holder.bindingAdapterPosition
            val oldPost = currentList[index]

            val updatedPost = oldPost.copy(
                liked = !oldPost.liked,
                numberof_likes = if (oldPost.liked)
                    oldPost.numberof_likes - 1
                else
                    oldPost.numberof_likes + 1
            )

            val newList = currentList.toMutableList()
            newList[index] = updatedPost

            submitList(newList)
            listener.onLikeClicked(updatedPost.post_id)
        }


        holder.txtReadMore.setOnClickListener {
            listener.onReadMoreClicked(post)
        }
        holder.txtLikeCount.setOnClickListener {
            listener.getLikeUsers(post.post_id)
        }
        holder.btnComment.setOnClickListener {
            listener.clickComment(post.post_id,post.pinnedCount)
        }
    }

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_ADVERT = 1
        private const val TYPE_SUGGESTED= 2
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HomePageUiModel>() {
            override fun areItemsTheSame(oldItem: HomePageUiModel, newItem: HomePageUiModel) =
                oldItem.post_id == newItem.post_id

            override fun areContentsTheSame(oldItem: HomePageUiModel, newItem: HomePageUiModel) =
                oldItem == newItem
        }
    }




}