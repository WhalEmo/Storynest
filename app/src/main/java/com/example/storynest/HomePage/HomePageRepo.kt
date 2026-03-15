package com.example.storynest.HomePage

import android.util.Log
import com.example.storynest.ErrorType
import com.example.storynest.ResultWrapper
import com.example.storynest.safeApiCall
import javax.inject.Inject


class HomePageRepo @Inject constructor(
    private val api: HPController
) {
    suspend fun addPost(request: postRequest): ResultWrapper<postResponse> =
        safeApiCall {
            api.addPost(request)
        }

    suspend fun toggleLike(
        postId: Long
    ): ResultWrapper<postResponse> =
        safeApiCall {
            api.toggleLike(postId)
        }
    suspend fun getUsersWhoLike(
        postId: Long,
        page: Int = 0,
        size: Int = 10
    ):ResultWrapper<List<UserResponse>> =
        safeApiCall {
            api.getUsersWhoLike(postId, page, size)
        }


    suspend fun HomePagePosts(
        page: Int = 0,
        size: Int = 10
    ): ResultWrapper<List<postResponse>> =
        safeApiCall {
            api.HomePagePosts(page ,size)
        }

    suspend fun deletePosts(
        postId:Long
    ): ResultWrapper<retrofit2.Response<String>> =
        safeApiCall {
            api.deletePosts(postId)
        }

}