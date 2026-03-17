package com.example.storynest.Comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.R

class CommentLoadStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<CommentLoadStateAdapter.ViewHolder>() {

    class ViewHolder(view: View, retry: () -> Unit) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        val retryButton: Button = view.findViewById(R.id.retry_button)

        init {
            retryButton.setOnClickListener { retry() }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.progressBar.isVisible = loadState is LoadState.Loading
        holder.retryButton.isVisible = loadState is LoadState.Error
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_load_state, parent, false)
        return ViewHolder(view, retry)
    }
}