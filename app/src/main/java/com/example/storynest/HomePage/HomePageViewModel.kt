package com.example.storynest.HomePage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.room.withTransaction
import com.example.storynest.CustomViews.UiEvents
import com.example.storynest.HomePage.viewModelHpHelper.PostMapper
import com.example.storynest.Posts.AppDatabase
import com.example.storynest.Posts.PostEntity
import com.example.storynest.Posts.PostRemoteMediator
import com.example.storynest.dataLocal.UserStaticClass

import com.example.storynest.ResultWrapper
import com.example.storynest.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

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

    private val _uiEvent = Channel<UiEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _usersWhoLike = MutableLiveData<UiState<List<UserResponse>>>()
    val usersWhoLike: LiveData<UiState<List<UserResponse>>> = _usersWhoLike



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
        .map { pagingData ->
            val mappedData = pagingData.map { entity ->
                HomePageUiModel.PostItem(
                    post = with(PostMapper) { entity.toUiItem() },
                    position =entity.orderIndex
                )
            }

            mappedData.insertSeparators { before, after ->
                /*
                when {
                    before == null -> {
                        HomePageUiModel.SectionHeader("Bugünün Akışı", HomePageUiModel.HeaderType.FEED_START)
                    }

                    before is HomePageUiModel.PostItem && before.position == 4 -> {
                        HomePageUiModel.SectionHeader("Tanıyabileceğiniz Kişiler", HomePageUiModel.HeaderType.SUGGESTED_USERS)
                    }
                    before is HomePageUiModel.SectionHeader && before.type == HomePageUiModel.HeaderType.SUGGESTED_USERS -> {
                        HomePageUiModel.SuggestedUserItem(emptyList())
                    }

                    before is HomePageUiModel.PostItem && before.position == 9 -> {
                        HomePageUiModel.SectionHeader("Sponsorlu İçerik", HomePageUiModel.HeaderType.SPONSORED)
                    }
                    before is HomePageUiModel.SectionHeader && before.type == HomePageUiModel.HeaderType.SPONSORED -> {
                        HomePageUiModel.AdvertItem("ad_unit_001")
                    }

                    else -> null
                }

                 */
                null
            }
        }

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

    fun toggleLike(post: postUiItem, isCurrentlyLiked: Boolean, currentLikeCount: Int) {
        viewModelScope.launch {
            val targetLiked = !isCurrentlyLiked
            val targetCount = if (targetLiked) currentLikeCount + 1 else currentLikeCount - 1
            database.postDao().updateLikeStatus(post.postId, targetLiked, targetCount)

            val result = repo.toggleLike(post.postId)

            when (result) {
                is ResultWrapper.Success -> {
                }
                is ResultWrapper.Error -> {
                    database.postDao().updateLikeStatus(post.postId, isCurrentlyLiked, currentLikeCount)
                    _uiEvent.trySend(UiEvents.showInfoMessage("Bir hata oluştu.Bağlantınızı kontrol ediniz."))
                }
            }
        }
    }

    private var undoJob: Job? = null
    private var recentlyDeletedPostId: Long? = null // Sadece ID tutmak yeterli

    fun deletePosts(postUi: postUiItem) {
        viewModelScope.launch {
            recentlyDeletedPostId = postUi.postId
            database.postDao().softDeletePost(postUi.postId)
            _uiEvent.trySend(UiEvents.ShowUndoSnackbar("Post silindi"))
        }
    }

    fun undoDelete() {
        undoJob?.cancel()
        undoJob = viewModelScope.launch {
            recentlyDeletedPostId?.let { postId ->
                try {
                    database.postDao().undoSoftDelete(postId)
                    android.util.Log.d("UNDO_DEBUG", "Soft Undo başarılı. ID: $postId")
                    recentlyDeletedPostId = null
                    _uiEvent.trySend(UiEvents.showInfoMessage("İşlem geri alındı"))
                } catch (e: Exception) {
                    android.util.Log.e("UNDO_DEBUG", "Undo hatası", e)
                }
            }
        }
    }

    fun confirmDelete() {
        val postId = recentlyDeletedPostId ?: return
        recentlyDeletedPostId = null

        viewModelScope.launch {
            val result = repo.deletePosts(postId)

            if (result is ResultWrapper.Success) {
                database.postDao().deletePost(postId)
            } else {
                database.postDao().undoSoftDelete(postId)
                _uiEvent.trySend(UiEvents.showInfoMessage("Hata: Sunucudan silinemedi."))
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