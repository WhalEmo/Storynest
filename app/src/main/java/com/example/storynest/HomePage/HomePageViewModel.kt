package com.example.storynest.HomePage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.ApiClient
import com.example.storynest.RegisterLogin.LoginResponse
import com.example.storynest.RegisterLogin.UserStaticClass

import com.example.storynest.ResultWrapper
import com.example.storynest.UiState

import kotlinx.coroutines.launch


class HomePageViewModel(
    private val repo: HomePageRepo
) : ViewModel() {

    private val _addPostResult = MutableLiveData<UiState<postResponse>>()
    val addPostResult: LiveData<UiState<postResponse>> = _addPostResult

    private val _userPosts = MutableLiveData<UiState<List<postResponse>>>()
    val userPosts: LiveData<UiState<List<postResponse>>> = _userPosts

    private val _postsLike = MutableLiveData<UiState<ToggleLikeResponse>>()
    val postsLike: LiveData<UiState<ToggleLikeResponse>> = _postsLike

    private val _usersWhoLike = MutableLiveData<UiState<List<UserResponse>>>()
    val usersWhoLike: LiveData<UiState<List<UserResponse>>> = _usersWhoLike

    private val _homepagePosts = MutableLiveData<UiState<List<postResponse>>>()
    val homepagePosts: LiveData<UiState<List<postResponse>>> = _homepagePosts
    private val _deletePosts = MutableLiveData<UiState<String>>()
    val deletePosts: LiveData<UiState<String>> = _deletePosts

    private val _updatePost = MutableLiveData<UiState<String>>()
    val updatePost: LiveData<UiState<String>> = _updatePost


    fun addPost(
        postName: String,
        contents: String,
        categories: String,
        coverImage: String
    ) {
        val request = postRequest(UserStaticClass.userId, postName, contents, categories, coverImage)
        _addPostResult.value = UiState.Loading

        viewModelScope.launch {
            val result=repo.addPost(request)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _addPostResult.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _addPostResult.value = UiState.Error(result.message)
            }
        }
    }

    fun getUserPosts(
        userId: Long,
        page: Int = 0,
        size: Int = 10
    ) {
        _userPosts.value= UiState.Loading

        viewModelScope.launch {
            val result = repo.getUserPosts(userId, page, size)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _userPosts.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _userPosts.value = UiState.Error(result.message)
            }
        }
    }
    fun toggleLike(
        postId: Long
    ) {
        _postsLike.value= UiState.Loading
        viewModelScope.launch {
            val result=repo.toggleLike(postId)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _postsLike.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _postsLike.value = UiState.Error(result.message)
            }
        }
    }

    fun getUsersWhoLike(
        postId: Long,
        page: Int = 0,
        size: Int = 10
    ){
        _usersWhoLike.value= UiState.Loading
        viewModelScope.launch {
            val result=repo.getUsersWhoLike(postId,page,size)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _usersWhoLike.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _usersWhoLike.value = UiState.Error(result.message)
            }
        }
    }

    private var currentPageHome = 0
    private val pageSizeHome = 10
    var isLoadingHome = false
    var isLastPageHome = false
    fun homePagePosts(
        reset: Boolean = false
    ){
        if (isLoadingHome || isLastPageHome) return
        if (reset) currentPageHome = 0

        _homepagePosts.value = UiState.Loading
        isLoadingHome = true

        viewModelScope.launch {
            val result=repo.HomePagePosts(currentPageHome,pageSizeHome)
            when (result) {
                is ResultWrapper.Success -> {
                    val currentList = (_homepagePosts.value as? UiState.Success)?.data ?: emptyList()
                    val newList = if (reset) result.data else currentList + result.data
                    _homepagePosts.value = UiState.Success(newList)
                    isLastPageHome = result.data.size < pageSizeHome
                    if (!isLastPageHome) currentPageHome++
                }
                is ResultWrapper.Error -> _homepagePosts.value = UiState.Error(result.message)
            }
            isLoadingHome = false
        }
    }
    fun deletePosts(
        postId: Long
    ){
        _deletePosts.value= UiState.Loading
        viewModelScope.launch {
            val result=repo.deletePost(postId)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _deletePosts.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _deletePosts.value = UiState.Error(result.message)
            }
        }
    }
    fun updatePost(
        postId: Long,
        request: PostUpdateRequest
    ){
        _updatePost.value= UiState.Loading
        viewModelScope.launch {
        val result=repo.updatePost(postId,request)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _updatePost.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _updatePost.value = UiState.Error(result.message)
            }
        }
    }
}