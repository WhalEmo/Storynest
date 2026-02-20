package com.example.storynest.Comments

sealed class CommentsUiModel {
    data class CommentItem(
        val comment: commentUiItem
    ): CommentsUiModel()

    data class ReplyItem(
        val reply: commentUiItem,
    ) : CommentsUiModel()

    data class ViewRepliesItem(
        val parentCommentId: Long,
        val remainingCount: Long,
        val isLoadMore: Boolean
    ) : CommentsUiModel()

}