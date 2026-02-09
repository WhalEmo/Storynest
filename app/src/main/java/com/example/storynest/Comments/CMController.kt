package com.example.storynest.Comments

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


data class commentRequest(
    val postId: Long,
    val userId: Long?,
    val contents: String,
    val parentCommentId: Long?
)
data class commentResponse(
    val commentId:Long,
    val parentCommentUsername:String?,
    val postId:Long,
    val user: UserResponse,
    val contents:String,
    var numberof_likes: Int,
    val date: String,
    var parentCommentId:Long,
    var isLiked: Boolean
)
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val surname: String,
    val profile: String?,
    val date: String?,
    val biography: String?,
    val emailVerified: Boolean,
    val isFollowing: Boolean
)
data class update(
    val contents:String
)
data class StringResponse(
    val message: String? = null,
    val error: String? = null
)
data class SubCommentPagingState(
    var currentPage: Int = 0,
    var isLoading: Boolean = false,
    var isLastPage: Boolean = false
)


interface CMController{
    @POST("/api/comments/addComment")
     fun addComment(@Body request: commentRequest): Call<commentResponse>

    @POST("/api/comments/addSubComment")
     fun addSubComment(@Body request: commentRequest): Call<commentResponse>

    @GET("/api/comments/commentsGet")
     fun commentsGet(
        @Query("postId") postId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<List<commentResponse>>

    @GET("/api/comments/subCommentsGet")
     fun subCommentsGet(
        @Query("parentCommentId") postId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<List<commentResponse>>

    @POST("/api/comments/{commentId}/like")
     fun toggleLike(@Path("commentId") commentId: Long): Call<StringResponse>

    @GET("/api/comments/{commentId}/getUsersWhoLike")
     fun getUsersWhoLike(
        @Path("commentId") commentId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<List<UserResponse>>

    @DELETE("/api/comments/{commentId}/deleteComment")
     fun deleteComment(
        @Path("commentId") commentId: Long
    ): Call<StringResponse>

    @PUT("/api/comments/{commentId}/updateComment")
     fun updateComment(
        @Path("commentId") commentId: Long,
        @Body request: update
    ): Call<StringResponse>

}