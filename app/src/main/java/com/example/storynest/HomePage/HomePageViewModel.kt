package com.example.storynest.HomePage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.flatMap
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.storynest.Comments.CommentsUiModel
import com.example.storynest.GenericPagingSource
import com.example.storynest.HomePage.viewModelHpHelper.PostMapper
import com.example.storynest.Posts.AppDatabase
import com.example.storynest.Posts.PostEntity
import com.example.storynest.Posts.PostRemoteMediator
import com.example.storynest.RegisterLogin.LoginResponse
import com.example.storynest.dataLocal.UserStaticClass

import com.example.storynest.ResultWrapper
import com.example.storynest.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomePageViewModel  @Inject constructor(
    private val repo: HomePageRepo,
    private val database: AppDatabase
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

    @OptIn(ExperimentalPagingApi::class)
    val pagingPosts: Flow<PagingData<PostEntity>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 3
            ),
            remoteMediator = PostRemoteMediator(
                apiCall = { page, size -> repo.HomePagePosts(page, size) },
                database = database
            ),
            pagingSourceFactory = {
                database.postDao().getAllPosts()
            }
        ).flow.cachedIn(viewModelScope)

    val posts: Flow<PagingData<HomePageUiModel>> = pagingPosts
        .map { pagingData: PagingData<PostEntity> ->
            val mappedData: PagingData<HomePageUiModel> = pagingData.map { entity ->
                HomePageUiModel.PostItem(with(PostMapper) { entity.toUiItem() })
            }

            mappedData.insertSeparators { before: HomePageUiModel?, after: HomePageUiModel? ->
                when {
                    before == null -> {
                        HomePageUiModel.HeaderItem("Bugünün Akışı")
                    }

                    before is HomePageUiModel.PostItem && before.post.postId % 5 == 0L -> {
                        HomePageUiModel.SuggestedUserItem(emptyList())
                    }

                    before is HomePageUiModel.PostItem && before.post.postId % 10 == 0L -> {
                        HomePageUiModel.AdvertItem("reklam_123")
                    }

                    else -> null
                }
            }
        }
        .cachedIn(viewModelScope)
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

    private var currentPageUser = 0
    private val pageSizeUser = 10
    var isLoadingUser = false
    var isLastPageUser = false
    fun getUsersWhoLike(
        postId: Long,
        reset: Boolean = false
    ){
        if(isLoadingUser || isLastPageUser)return
        if(reset) currentPageUser = 0

        _usersWhoLike.value= UiState.Loading
        isLoadingUser=true

        viewModelScope.launch {
            val result=repo.getUsersWhoLike(postId,currentPageUser,pageSizeUser)
            when (result) {
                is ResultWrapper.Success -> {

                    val currentList = (_usersWhoLike.value as? UiState.Success)?.data ?: emptyList()
                    val newList = if (reset) result.data else currentList + result.data
                    _usersWhoLike.value = UiState.Success(newList)
                    isLastPageUser = result.data.size < pageSizeUser
                    if (!isLastPageUser) currentPageUser++
                }
                is ResultWrapper.Error -> _usersWhoLike.value = UiState.Error(result.message)
            }
            isLoadingUser=false
        }
    }



}