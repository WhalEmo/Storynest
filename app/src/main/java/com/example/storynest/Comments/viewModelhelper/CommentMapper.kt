package com.example.storynest.Comments.viewModelhelper

import android.view.View
import com.example.storynest.Comments.commentResponse
import com.example.storynest.Comments.commentUiItem
import com.example.storynest.R

object CommentMapper {
    fun commentResponse.toUiItem(): commentUiItem {

        val isEditedVisible = if (isEdited) View.VISIBLE else View.GONE
        val isPinnedVisible = if (isPinned) View.VISIBLE else View.GONE

        return commentUiItem(
            commentId = comment_id,
            parentCommentUsername = parentCommentUsername,
            postUserId = postUserId,
            userId = user.id,
            userName = user.username,
            profileUrl = user.profile,
            contents = contents,
            number_of_like = CommentFormatter.formatLike(number_of_like),
            date = CommentFormatter.formatCommentDate(date),
            updateDate = if (isEdited) CommentFormatter.formatCommentDate(updateDate) else null,
            parentCommentId = parentCommentId,
            likeIconRes = if (isLiked)
                R.drawable.baseline_favorite_24
            else
                R.drawable.baseline_favorite_border_24,
            editedVisibility = isEditedVisible,
            pinVisibility = isPinnedVisible,
            isPin = isPinned,
            editDateVisibility = isEditedVisible,
            replyCount = replyCount
        )
    }
}