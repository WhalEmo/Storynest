package com.example.storynest.Notification.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.R

class NotificationAdapter(
    private val onItemShown: (Long) -> Unit,
    private val onAccept: (NotificationRow.NotificationItem) -> Unit,
    private val onReject: (NotificationRow.NotificationItem) -> Unit
) : ListAdapter<NotificationRow, RecyclerView.ViewHolder>(DIFF) {

    companion object {

        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1

        val DIFF = object : DiffUtil.ItemCallback<NotificationRow>() {

            override fun areItemsTheSame(
                oldItem: NotificationRow,
                newItem: NotificationRow
            ): Boolean {
                return when {
                    oldItem is NotificationRow.NotificationItem &&
                            newItem is NotificationRow.NotificationItem ->
                        oldItem.notification.id == newItem.notification.id

                    oldItem is NotificationRow.NotificationHeader &&
                            newItem is NotificationRow.NotificationHeader ->
                        oldItem.title == newItem.title

                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: NotificationRow,
                newItem: NotificationRow
            ): Boolean = oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NotificationRow.NotificationHeader -> TYPE_HEADER
            is NotificationRow.NotificationItem -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
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
                    R.layout.notification_item,
                    parent,
                    false
                )
                NotificationViewHolder(view, onAccept, onReject)
            }

            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {

            is NotificationRow.NotificationHeader -> {
                (holder as HeaderViewHolder).bind(item)
            }

            is NotificationRow.NotificationItem -> {
                (holder as NotificationViewHolder).bind(item)
                onItemShown(item.notification.id)
            }
        }
    }
}
