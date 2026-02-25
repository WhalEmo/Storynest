package com.example.storynest.Comments.viewModelhelper

import com.example.storynest.Comments.commentResponse
import com.example.storynest.Comments.commentUiItem

data class CommentUiState(
    val removedIds: Set<Long>,
    val updates: Map<Long, commentResponse>,
    val newItems: List<commentResponse>,
    val pinnedItems: List<commentResponse>,
    val replyThreads: Map<Long, ReplyThread>
)
data class ReplyThread(
    val replies: List<commentResponse> = emptyList(),
    val visibleCount: Int = 0,
    val totalCount: Long = 0,
    val currentPage: Int = 0,
    val isLoading: Boolean = false
)
    data class BaseState(
        val removedIds: Set<Long>,
        val updates: Map<Long, commentResponse>,
        val newItems: List<commentResponse>,
        val pinnedIds: List<commentResponse>
    )


