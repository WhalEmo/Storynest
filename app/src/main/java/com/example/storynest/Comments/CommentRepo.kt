package com.example.storynest.Comments

import com.example.storynest.ResultWrapper
import com.example.storynest.safeApiCall

class CommentRepo(
    private val api: CMController
) {
    suspend fun addComment(request: commentRequest): ResultWrapper<commentResponse> =
        safeApiCall {
            api.addComment(request).execute()
        }

    suspend fun addSubComment(request: commentRequest): ResultWrapper<commentResponse> =
        safeApiCall {
            api.addSubComment(request).execute()
        }

    suspend fun commentsGet(
        postId: Long,
        page: Int = 0,
        size: Int = 10
    ): ResultWrapper<List<commentResponse>> =
        safeApiCall {
            api.commentsGet(postId,page,size).execute()
        }

    suspend fun subCommentsGet(
        parentCommentId: Long,
        page: Int = 0,
        size: Int = 10
    ): ResultWrapper<List<commentResponse>> =
        safeApiCall {
            api.subCommentsGet(parentCommentId,page,size).execute()
        }

    suspend fun toggleLike(
        commentId: Long
    ): ResultWrapper<StringResponse> =
        safeApiCall {
            api.toggleLike(commentId).execute()
        }

    suspend fun getUsersWhoLike(
        commentId: Long,
        page: Int = 0,
        size: Int = 10
    ):ResultWrapper<List<UserResponse>> =
        safeApiCall {
            api.getUsersWhoLike(commentId, page, size).execute()
        }

    suspend fun deleteComment(
        commentId: Long
    ): ResultWrapper<StringResponse> =
        safeApiCall {
            api.deleteComment(commentId).execute()
        }

    suspend fun updateComment(
        commentId: Long,
        request: update
    ): ResultWrapper<StringResponse> =
        safeApiCall {
            api.updateComment(commentId,request).execute()
        }

}