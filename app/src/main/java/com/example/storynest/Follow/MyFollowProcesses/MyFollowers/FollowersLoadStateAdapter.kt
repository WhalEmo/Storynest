package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class FollowersLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<FollowersLoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): FollowersLoadStateViewHolder {
        return FollowersLoadStateViewHolder.create(parent, retry)
    }

    override fun onBindViewHolder(
        holder: FollowersLoadStateViewHolder,
        loadState: LoadState
    ) {
        holder.bind(loadState)
    }
}
