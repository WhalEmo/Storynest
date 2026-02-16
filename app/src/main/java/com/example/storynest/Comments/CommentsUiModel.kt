package com.example.storynest.Comments

sealed class CommentsUiModel {
    data class CommentItem(
        val comment: commentResponse
    ): CommentsUiModel()

    data class ReplyItem(
        val reply: commentResponse,
    ) : CommentsUiModel()

    data class ViewRepliesItem(
        val parentCommentId: Long,
        val remainingCount: Long,
        val isLoadMore: Boolean
    ) : CommentsUiModel()

}