package com.example.storynest.Comments

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
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
    val comment_id:Long,
    val parentCommentUsername:String?,
    val post_id:Long,
    val postUserId:Long,
    val user: userResponseDto,
    val contents:String,
    var number_of_like: Int,
    val date: String,
    var updateDate: String?,
    var parentCommentId:Long,
    @SerializedName(value = "isLiked", alternate = ["liked"])
    var isLiked: Boolean = false,
    @SerializedName(value = "isEdited", alternate = ["edited"])
    var isEdited: Boolean=false,
    @SerializedName(value = "isPinned", alternate = ["pinned"])
    var isPinned: Boolean=false,
    var replyCount: Long
)
data class commentUiItem(
    val commentId:Long,
    val parentCommentUsername:String?,
    val postUserId:Long,
    val userId: Long,
    val userName:String,
    val profileUrl:String?,
    val contents:String,
    var number_of_like: String,
    val date: String,
    var updateDate: String?,
    var parentCommentId:Long,
    val likeIconRes: Int,
    val editedVisibility: Int,
    val pinVisibility:Int,
    val isPin: Boolean=false,
    val editDateVisibility:Int,
    var replyCount: Long
)
data class userResponseDto(
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
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null
)


interface CMController{
    @POST("/api/comments/addComment")
    suspend fun addComment(@Body request: commentRequest): commentResponse

    @POST("/api/comments/addSubComment")
     suspend fun addSubComment(@Body request: commentRequest): commentResponse

    @GET("/api/comments/commentsGet")
    suspend fun commentsGet(
        @Query("postId") postId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<commentResponse>>


    @GET("/api/comments/subCommentsGet")
     suspend fun subCommentsGet(
        @Query("parentCommentId") postId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<commentResponse>>

    @POST("/api/comments/{commentId}/like")
     suspend fun toggleLike(@Path("commentId") commentId: Long): commentResponse

    @GET("/api/comments/{commentId}/getUsersWhoLike")
     suspend fun getUsersWhoLike(
        @Path("commentId") commentId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): List<userResponseDto>

    @DELETE("/api/comments/{commentId}/deleteComment")
     suspend fun deleteComment(
        @Path("commentId") commentId: Long
    ): StringResponse

    @PUT("/api/comments/{commentId}/updateComment")
     suspend fun updateComment(
        @Path("commentId") commentId: Long,
        @Body request: update
    ): commentResponse


    @PATCH("/api/comments/{commentId}/pin")
    suspend fun pinComment(
        @Path("commentId") commentId: Long
    ): commentResponse

    @PATCH("/api/comments/{commentId}/removePin")
    suspend fun removePin(
        @Path("commentId") commentId: Long
    ): commentResponse


}