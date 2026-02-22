package com.example.storynest.Comments

import android.util.Log
import com.example.storynest.ResultWrapper
import com.example.storynest.safeApiCall
import retrofit2.Response

class CommentRepo(
    private val api: CMController
) {

    suspend fun addComment(request: commentRequest)
            : ResultWrapper<commentResponse> =
        safeApiCall {
            api.addComment(request)
        }

    suspend fun addSubComment(request: commentRequest)
            : ResultWrapper<commentResponse> =
        safeApiCall {
            api.addSubComment(request)
        }

    suspend fun commentsGet(
        postId: Long,
        page: Int = 0,
        size: Int = 10
    ): ResultWrapper<Response<List<commentResponse>>> =
        safeApiCall {
            api.commentsGet(postId, page, size)
        }

    suspend fun subCommentsGet(
        parentCommentId: Long,
        page: Int = 0,
        size: Int = 10
    ): ResultWrapper<Response<List<commentResponse>>> =
        safeApiCall {
            api.subCommentsGet(parentCommentId, page, size)
        }

    suspend fun toggleLike(
        commentId: Long
    ): ResultWrapper<commentResponse> =
        safeApiCall {
            api.toggleLike(commentId)
        }

    suspend fun getUsersWhoLike(
        commentId: Long,
        page: Int = 0,
        size: Int = 10
    ): ResultWrapper<List<userResponseDto>> =
        safeApiCall {
            api.getUsersWhoLike(commentId, page, size)
        }

    suspend fun deleteComment(
        commentId: Long
    ): ResultWrapper<StringResponse> =
        safeApiCall {
            api.deleteComment(commentId)
        }

    suspend fun updateComment(
        commentId: Long,
        request: update
    ): ResultWrapper<commentResponse> =
        safeApiCall {
            api.updateComment(commentId, request)
        }


    suspend fun pin(
        commentId: Long
    ): ResultWrapper<commentResponse> =
        safeApiCall {
            api.pinComment(commentId)
        }

    suspend fun removePin(
        commentId: Long
    ): ResultWrapper<commentResponse> =
        safeApiCall {
            api.removePin(commentId)
        }


}
