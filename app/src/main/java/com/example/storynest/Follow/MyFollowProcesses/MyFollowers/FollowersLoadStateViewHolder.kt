package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.databinding.ItemLoadStateBinding

class FollowersLoadStateViewHolder(
    private val binding: ItemLoadStateBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener {
            retry()
        }
    }

    fun bind(loadState: LoadState) {
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState is LoadState.Error
        binding.errorText.isVisible = loadState is LoadState.Error
    }

    companion object {
        fun create(
            parent: ViewGroup,
            retry: () -> Unit
        ): FollowersLoadStateViewHolder {

            val binding = ItemLoadStateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return FollowersLoadStateViewHolder(binding, retry)
        }
    }
}
