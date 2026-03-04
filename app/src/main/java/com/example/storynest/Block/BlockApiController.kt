package com.example.storynest.Block

import com.example.storynest.Api.ApiResponse
import okhttp3.Response
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface BlockApiController {

    @POST("block/{targetId}")
    suspend fun block(
        @Path("targetId") targetId: Long
    ): Response<ApiResponse<String>>
    @DELETE("block/{targetId}")
    suspend fun unblock(
        @Path("targetId") targetId: Long
    ): Response<ApiResponse<String>>
}