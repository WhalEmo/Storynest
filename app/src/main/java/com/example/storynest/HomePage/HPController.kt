package com.example.storynest.HomePage

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class postRequest(
    val user_id: Long?,
    val postName: String,
    val contents: String,
    val categories: String,
    val coverImage: String?
)
data class updatePost(
    val postName: String,
    val contents: String,
    val categories: String,
    val coverImage: String?
)
data class postResponse(
    val post_id:Long,
    val user: UserResponse,
    val postName:String,
    val contents:String,
    val categories:String,
    val coverImage:String?,
    @SerializedName("numberof_likes")
    var numberof_likes: Int,
    val postDate: String,
    val updateDate: String?,
    @SerializedName(value = "isLiked", alternate = ["liked"])
    var isLiked: Boolean,
    @SerializedName(value = "isEdited", alternate = ["edited"])
    val isEdited: Boolean,
    var pinnedCount:Long
)

@Parcelize
data class postUiItem(
    val postId: Long,
    val userId: Long,
    val userName: String,
    val profileUrl: String?,
    val postName: String,
    val contents: String,
    val categories: String,
    val coverImage: String?,
    val rawLikeCount: Int,
    var numberof_likes: String,
    val postDate: String,
    val updateDate: String?,
    val likeIconRes: Int,
    var liked: Boolean,
    var edited: Boolean,
    var pinnedCount: Long
) : Parcelable

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

data class ToggleLikeResponse(
    val message: String? = null,
    val error: String? = null
)

interface HPController {
    @POST("/api/posts/addPost")
    suspend fun addPost(@Body request: postRequest): postResponse

    @GET("/api/posts/getUserPosts")
    suspend fun getUserPosts(
        @Query("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): List<postResponse>


    @POST("/api/posts/{postId}/like")
    suspend fun toggleLike(@Path("postId") postId: Long): postResponse


    @GET("/api/posts/{postId}/getUsersWhoLike")
    suspend fun getUsersWhoLike(
        @Path("postId") postId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ):List<UserResponse>


    @GET("/api/posts/HomePagePosts")
    suspend fun HomePagePosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): List<postResponse>


    @DELETE("/api/posts/{postId}/deletePost")
    suspend fun deletePosts(
        @Path("postId") postId:Long
    ):retrofit2.Response<String>


    @PUT("/api/posts/{postId}/updatePost")
    suspend fun updatePosts(
        @Path("postId") postId: Long,
        @Body request: updatePost
    ):postResponse


}


