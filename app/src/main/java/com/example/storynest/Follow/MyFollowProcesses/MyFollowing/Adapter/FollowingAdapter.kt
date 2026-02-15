package com.example.storynest.Follow.MyFollowProcesses.MyFollowing.Adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.Follow.Base.HeaderViewHolder
import com.example.storynest.Follow.FollowRow
import com.example.storynest.R

class FollowingAdapter(
    private val onAccept: (FollowRow.FollowUserItem) -> Unit,
    private val onReject: (FollowRow.FollowUserItem) -> Unit,
    private val onCancelRequest: (FollowRow.FollowUserItem) -> Unit,
    private val onUnFollowMy: (FollowRow.FollowUserItem) -> Unit
) : PagingDataAdapter<FollowRow, RecyclerView.ViewHolder>(DIFF) {

    companion object {

        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1

        val DIFF = object : DiffUtil.ItemCallback<FollowRow>() {

            override fun areItemsTheSame(
                oldItem: FollowRow,
                newItem: FollowRow
            ): Boolean {
                return when {
                    oldItem is FollowRow.FollowUserItem &&
                            newItem is FollowRow.FollowUserItem ->
                        oldItem.followUserResponseDTO.id ==
                                newItem.followUserResponseDTO.id


                    oldItem is FollowRow.FollowHeaderItem &&
                            newItem is FollowRow.FollowHeaderItem ->
                        oldItem.title == newItem.title

                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: FollowRow,
                newItem: FollowRow
            ): Boolean {
                return when {
                    oldItem is FollowRow.FollowUserItem &&
                            newItem is FollowRow.FollowUserItem ->
                        oldItem.followUserResponseDTO.followInfo.status ==
                                newItem.followUserResponseDTO.followInfo.status

                    oldItem is FollowRow.FollowHeaderItem &&
                            newItem is FollowRow.FollowHeaderItem ->
                        oldItem.title == newItem.title

                    else -> true
                }
            }

        }
    }


    override fun getItemViewType(position: Int): Int {
        return when(peek(position)){
            is FollowRow.FollowUserItem -> TYPE_ITEM
            is FollowRow.FollowHeaderItem -> TYPE_HEADER
            else -> TYPE_ITEM
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
                    R.layout.following_item,
                    parent,
                    false
                )
                FollowingUserViewHolder(view, onAccept, onReject)
            }
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when(val item = getItem(position)){

            is FollowRow.FollowHeaderItem -> {
                (holder as HeaderViewHolder).bind(item)
            }

            is FollowRow.FollowUserItem -> {
                (holder as FollowingUserViewHolder).bind(item)
            }
            null -> {

            }
        }

    }

}