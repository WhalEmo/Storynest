package com.example.storynest.Comments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.insertHeaderItem
import androidx.paging.map
import com.example.storynest.ApiClient
import com.example.storynest.ResultWrapper
import com.example.storynest.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class CommentsViewModel(
    private val repo: CommentRepo
) : ViewModel() {

    private val _commentAddState = MutableSharedFlow<Boolean>()
    val commentAddState = _commentAddState.asSharedFlow()

    private val _addSubCommentResult = MutableLiveData<UiState<commentResponse>>()
    val addSubCommentResult: LiveData<UiState<commentResponse>> = _addSubCommentResult

    private val _postComments = MutableLiveData<UiState<List<commentResponse>>>()
    val postComments: LiveData<UiState<List<commentResponse>>> = _postComments

    private val _subCommentsState =
        MutableStateFlow<Map<Long, UiState<List<commentResponse>>>>(emptyMap())

    val subCommentsState: StateFlow<Map<Long, UiState<List<commentResponse>>>> = _subCommentsState

    private val _removedCommentIds = MutableStateFlow<Set<Long>>(emptySet())
    val removedCommentIds: StateFlow<Set<Long>> = _removedCommentIds

    private val _updatedComments = MutableStateFlow<Map<Long, commentResponse>>(emptyMap())
    val updatedComments: StateFlow<Map<Long, commentResponse>> = _updatedComments.asStateFlow()


    private val _postId = MutableStateFlow<Long?>(null)
    val postId: StateFlow<Long?> = _postId

    fun setPostId(id: Long) {
        _postId.value = id
    }

    val pagingComments: Flow<PagingData<commentResponse>> =
        postId
            .filterNotNull()
            .flatMapLatest { id ->
                getComments(id)
            }


    private val _commentsLike = MutableLiveData<UiState<StringResponse>>()
    val postsLike: LiveData<UiState<StringResponse>> = _commentsLike

    private val _usersWhoLike = MutableLiveData<UiState<List<userResponseDto>>>()
    val usersWhoLike: LiveData<UiState<List<userResponseDto>>> = _usersWhoLike


    fun addComment(
        postId: Long,
        userId: Long?,
        contents: String,
        parentCommentId: Long?
    ) {
        val request = commentRequest(postId, userId, contents, parentCommentId)
        viewModelScope.launch {
            val result = repo.addComment(request)
            when (result) {

                is ResultWrapper.Success -> {
                    _commentAddState.emit(true)
                    _newComments.update { currentList ->
                        listOf(result.data) + currentList
                    }
                }

                is ResultWrapper.Error -> {
                    _commentAddState.emit(false)
                }
            }
        }
    }

    fun addSubComment(
        postId: Long,
        userId: Long?,
        contents: String,
        parentCommentId: Long
    ) {
        val request = commentRequest(postId, userId, contents, parentCommentId)
        _addSubCommentResult.value = UiState.Loading

        viewModelScope.launch {
            val result = repo.addSubComment(request)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _addSubCommentResult.value = UiState.Success(body)
                }

                is ResultWrapper.Error -> _addSubCommentResult.value = UiState.Error(result.message)
            }
        }
    }

    private val api = ApiClient.commentApi


    fun getComments(postId: Long): Flow<PagingData<commentResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 3
            ),
            pagingSourceFactory = {
                CommentPagingSource { page, size ->
                    api.commentsGet(postId, page, size)
                }
            }
        ).flow.cachedIn(viewModelScope)
    }

    private val _newComments = MutableStateFlow<List<commentResponse>>(emptyList())
    val newComments = _newComments.asStateFlow()



    val comments: Flow<PagingData<commentResponse>> =
        combine(
            pagingComments,
            removedCommentIds,
            updatedComments,
            newComments
        ) { pagingData, removedIds, updates, newItems ->

            var baseData = pagingData
                .filter { !removedIds.contains(it.comment_id) }
                .map { comment ->
                    updates[comment.comment_id]?.let { updated ->
                        comment.copy(
                            contents = updated.contents,
                            isEdited = updated.isEdited,
                            updateDate = updated.updateDate
                        )
                    } ?: comment
                }

            newItems.reversed().forEach { newItem ->
                baseData = baseData.insertHeaderItem(item = newItem)
            }

            baseData
        }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            val result = repo.deleteComment(commentId)
            when (result) {
                is ResultWrapper.Success -> {
                    removeComment(commentId)
                }
                is ResultWrapper.Error ->{
                    UiState.Error(result.message)
                }
            }
        }
    }
    fun removeComment(commentId: Long) {
        _removedCommentIds.value = _removedCommentIds.value + commentId
    }

    fun updateComment(comment: commentResponse) {
        _updatedComments.value = _updatedComments.value + (comment.comment_id to comment)
    }

    fun updateComment(commentId: Long, contents: String) {
        val request = update(contents)
        viewModelScope.launch {
            val result = repo.updateComment(commentId, request)
            when (result) {
                is ResultWrapper.Success -> {
                    val updatedComment = result.data
                    updateComment(updatedComment)
                }
                is ResultWrapper.Error -> {
                    UiState.Error(result.message)
                }
            }
        }
    }


    fun toggleLike(
        commentId: Long
    ) {
        _commentsLike.value= UiState.Loading
        viewModelScope.launch {
            val result=repo.toggleLike(commentId)
            Log.d("API_CHECK", "İstek atılan Comment ID: $commentId")
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _commentsLike.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _commentsLike.value = UiState.Error(result.message)
            }
        }
    }

    private var currentPageUser = 0
    private val pageSizeUser = 10
    var isLoadingUser = false
    var isLastPageUser = false
    fun getUsersWhoLike(
        commentId: Long,
        reset: Boolean = false
    ){
        if(isLoadingUser || isLastPageUser)return
        if(reset) currentPageUser = 0

        _usersWhoLike.value= UiState.Loading
        isLoadingUser=true

        viewModelScope.launch {
            val result=repo.getUsersWhoLike(commentId,currentPageUser,pageSizeUser)
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