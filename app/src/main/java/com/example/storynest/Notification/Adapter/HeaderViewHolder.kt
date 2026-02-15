package com.example.storynest.Notification.Adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.R

class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val title: TextView =
        itemView.findViewById(R.id.headerTitle)

    fun bind(item: NotificationRow.NotificationHeader) {
        title.text = item.title
    }
}