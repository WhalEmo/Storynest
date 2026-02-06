package com.example.storynest.HomePage

import retrofit2.Call
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
    val coverImage: String
)
data class postResponse(
    val post_id:Long,
    val user: UserResponse,
    val postName:String,
    val contents:String,
    val categories:String,
    val coverImage:String,
    var numberof_likes: Int,
    val postDate: String,
    var liked:Boolean
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

data class ToggleLikeResponse(
    val message: String? = null,
    val error: String? = null
)

interface HPController {
    @POST("/api/posts/addPost")
    fun addPost(@Body request: postRequest): Call<postResponse>

    @GET("/api/posts/getUserPosts")
    fun getUserPosts(
        @Query("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<List<postResponse>>


    @POST("/api/posts/{postId}/like")
    fun toggleLike(@Path("postId") postId: Long): Call<ToggleLikeResponse>


    @GET("/api/posts/{postId}/getUsersWhoLike")
    fun getUsersWhoLike(
        @Path("postId") postId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<List<UserResponse>>


    @GET("/api/posts/HomePagePosts")
    fun HomePagePosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<List<postResponse>>
}


