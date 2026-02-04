package com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.R

class FollowersAdapter(
    private val onAccept: (FollowersRow.FollowerUserItem) -> Unit,
    private val onReject: (FollowersRow.FollowerUserItem) -> Unit,
    private val onCancelRequest: (FollowersRow.FollowerUserItem) -> Unit
) : ListAdapter<FollowersRow, RecyclerView.ViewHolder>(DIFF) {

    companion object {

        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1

        val DIFF = object : DiffUtil.ItemCallback<FollowersRow>() {

            override fun areItemsTheSame(
                oldItem: FollowersRow,
                newItem: FollowersRow
            ): Boolean {
                return when {
                    oldItem is FollowersRow.FollowerUserItem &&
                            newItem is FollowersRow.FollowerUserItem ->
                        oldItem.followUserResponseDTO.id == newItem.followUserResponseDTO.id

                    oldItem is FollowersRow.FollowersHeaderItem &&
                            newItem is FollowersRow.FollowersHeaderItem ->
                        oldItem.title == newItem.title

                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: FollowersRow,
                newItem: FollowersRow
            ): Boolean = oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is FollowersRow.FollowerUserItem -> TYPE_ITEM
            is FollowersRow.FollowersHeaderItem -> TYPE_HEADER
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when(viewType){
            TYPE_HEADER -> {
                val view = inflater.inflate(
                    R.layout.item_notification_header,
                    parent,
                    false
                )
                HeaderViewHolder(view)
            }
            TYPE_ITEM -> {
                val view = inflater.inflate(
                    R.layout.followers_item,
                    parent,
                    false
                )
                FollowerUserViewHolder(view, onAccept, onReject, onCancelRequest)
            }
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when(val item = getItem(position)){

            is FollowersRow.FollowersHeaderItem -> {
                (holder as HeaderViewHolder).bind(item)
            }

            is FollowersRow.FollowerUserItem -> {
                (holder as FollowerUserViewHolder).bind(item)
            }
        }

    }

}