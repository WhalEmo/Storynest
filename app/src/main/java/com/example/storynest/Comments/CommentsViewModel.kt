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
import com.example.storynest.ApiClient
import com.example.storynest.ResultWrapper
import com.example.storynest.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class CommentsViewModel(
    private val repo: CommentRepo
) : ViewModel(){
    private val _addCommentResult = MutableLiveData<UiState<commentResponse>>()
    val addCommentResult: LiveData<UiState<commentResponse>> = _addCommentResult

    private val _addSubCommentResult = MutableLiveData<UiState<commentResponse>>()
    val addSubCommentResult: LiveData<UiState<commentResponse>> = _addSubCommentResult

    private val _postComments= MutableLiveData<UiState<List<commentResponse>>>()
    val postComments: LiveData<UiState<List<commentResponse>>> = _postComments

    private val pageSizeSubComment = 10
    private val _subCommentsState =
        MutableStateFlow<Map<Long, UiState<List<commentResponse>>>>(emptyMap())

    val subCommentsState:
            StateFlow<Map<Long, UiState<List<commentResponse>>>> =
        _subCommentsState


    private val _commentsLike = MutableLiveData<UiState<StringResponse>>()
    val postsLike: LiveData<UiState<StringResponse>> = _commentsLike

    private val _usersWhoLike = MutableLiveData<UiState<List<userResponseDto>>>()
    val usersWhoLike: LiveData<UiState<List<userResponseDto>>> = _usersWhoLike

    private val _deleteComments = MutableLiveData<UiState<StringResponse>>()
    val deleteComment: LiveData<UiState<StringResponse>> = _deleteComments

    private val _updateComments = MutableLiveData<UiState<StringResponse>>()
    val updateComment: LiveData<UiState<StringResponse>> = _updateComments



    fun addComment(
        postId: Long,
        userId: Long?,
        contents: String,
        parentCommentId: Long?
    ){
        val request= commentRequest(postId,userId,contents,parentCommentId)
        _addCommentResult.value= UiState.Loading

        viewModelScope.launch {
            val result=repo.addComment(request)
                when (result) {

                    is ResultWrapper.Success -> {

                        val newComment = result.data

                        val currentList =
                            (_postComments.value as? UiState.Success)?.data
                                ?: emptyList()

                        val updatedList = listOf(newComment) + currentList

                        _postComments.value = UiState.Success(updatedList)

                        _addCommentResult.value = UiState.Success(newComment)
                    }

                    is ResultWrapper.Error -> {
                        _addCommentResult.value = UiState.Error(result.message)
                    }
            }
        }
    }

    fun addSubComment(
        postId: Long,
        userId: Long?,
        contents: String,
        parentCommentId: Long
    ){
        val request= commentRequest(postId,userId,contents,parentCommentId)
        _addSubCommentResult.value= UiState.Loading

        viewModelScope.launch {
            val result=repo.addSubComment(request)
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

    fun deleteComment(
        commentId: Long
    ){
        _deleteComments.value= UiState.Loading
        viewModelScope.launch {
            val result=repo.deleteComment(commentId)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _deleteComments.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _deleteComments.value = UiState.Error(result.message)
            }
        }
    }

    fun updateComment(
        commentId: Long,
        contents:String
    ){
        val request= update(contents)
        _updateComments.value= UiState.Loading
        viewModelScope.launch {
            val result= repo.updateComment(commentId,request)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _updateComments.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _updateComments.value = UiState.Error(result.message)
            }
        }
    }


}