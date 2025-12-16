package com.example.storynest.HomePage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.storynest.ResultWrapper

import kotlinx.coroutines.launch


class HomePageViewModel(
    private val repo: HomePageRepo
) : ViewModel() {

    val addPostResult = MutableLiveData<ResultWrapper<postResponse>>()
    val userPosts = MutableLiveData<ResultWrapper<List<postResponse>>>()
    val PostsLike = MutableLiveData<ResultWrapper<String>>()
    val userswhoLike= MutableLiveData<ResultWrapper<List<UserResponse>>>()
    val homepageposts= MutableLiveData<ResultWrapper<List<postResponse>>>()
    val deletePosts= MutableLiveData<ResultWrapper<String>>()
    val updatePost= MutableLiveData<ResultWrapper<String>>()
    fun addPost(
        postName: String,
        contents: String,
        categories: String,
        coverImage: String
    ) {
        val request = postRequest(postName, contents, categories, coverImage)

        viewModelScope.launch {
            addPostResult.value = repo.addPost(request)
        }
    }

    fun getUserPosts(
        userId: Long,
        page: Int = 0,
        size: Int = 10
    ) {
        viewModelScope.launch {
            userPosts.value = repo.getUserPosts(userId, page, size)
        }
    }

    fun toggleLike(
        postId: Long
    ) {
        viewModelScope.launch {
            PostsLike.value=repo.toggleLike(postId)
        }
    }
    fun getUsersWhoLike(
        postId: Long,
        page: Int = 0,
        size: Int = 10
    ){
        viewModelScope.launch {
            userswhoLike.value=repo.getUsersWhoLike(postId,page,size)
        }
    }
    fun homePagePosts(
        page: Int = 0,
        size: Int = 10
    ){
        viewModelScope.launch {
            homepageposts.value=repo.HomePagePosts(page,size)
        }
    }
    fun deletePosts(
        postId: Long
    ){
        viewModelScope.launch {
            deletePosts.value=repo.deletePost(postId)
        }
    }
    fun updatePost(
        postId: Long,
        request: PostUpdateRequest
    ){
        viewModelScope.launch {
         updatePost.value=repo.updatePost(postId,request)
        }
    }
}