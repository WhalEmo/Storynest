package com.example.storynest.Comments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.ResultWrapper
import com.example.storynest.UiState
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

    private val _postSubComments= MutableLiveData<UiState<List<commentResponse>>>()
    val postSubComments: LiveData<UiState<List<commentResponse>>> = _postSubComments

    private val _commentsLike = MutableLiveData<UiState<StringResponse>>()
    val postsLike: LiveData<UiState<StringResponse>> = _commentsLike

    private val _usersWhoLike = MutableLiveData<UiState<List<UserResponse>>>()
    val usersWhoLike: LiveData<UiState<List<UserResponse>>> = _usersWhoLike

    private val _deleteComments = MutableLiveData<UiState<StringResponse>>()
    val deleteComment: LiveData<UiState<StringResponse>> = _deleteComments

    private val _updateComments = MutableLiveData<UiState<StringResponse>>()
    val updateComment: LiveData<UiState<StringResponse>> = _updateComments

    fun addComment(
        commentId: Long,
        postId: Long,
        userId: Long,
        contents: String,
        parentCommentId: Long
    ){
        val request= commentRequest(commentId,postId,userId,contents,parentCommentId)
        _addCommentResult.value= UiState.Loading

        viewModelScope.launch {
            val result=repo.addComment(request)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    _addCommentResult.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _addCommentResult.value = UiState.Error(result.message)
            }
        }
    }

    fun addSubComment(
        commentId: Long,
        postId: Long,
        userId: Long,
        contents: String,
        parentCommentId: Long
    ){
        val request= commentRequest(commentId,postId,userId,contents,parentCommentId)
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

    private var currentPageComment = 0
    private val pageSizeComment = 10
    var isLoadingComment = false
    var isLastPageComment = false

    fun commentsGet(
        postId: Long,
        reset: Boolean = false
    ){
        if(isLoadingComment || isLastPageComment)return
        if(reset) currentPageComment = 0

        _postComments.value= UiState.Loading
        isLoadingComment=true

        viewModelScope.launch {
            val result=repo.commentsGet(postId,currentPageComment,pageSizeComment)
            when(result){
                is ResultWrapper.Success -> {

                    val currentList = (_postComments.value as? UiState.Success)?.data ?: emptyList()
                    val newList = if (reset) result.data else currentList + result.data
                    _postComments.value = UiState.Success(newList)
                    isLastPageComment = result.data.size < pageSizeComment
                    if (!isLastPageComment) currentPageComment++
                }
                is ResultWrapper.Error -> _postComments.value = UiState.Error(result.message)
            }
            isLoadingComment=false
        }
    }


    private var currentPageSubComment = 0
    private val pageSizeSubComment = 10
    var isLoadingSubComment = false
    var isLastPageSubComment = false
    fun subCommentsGet(
        postId: Long,
        reset: Boolean = false
    ){
        if(isLoadingSubComment || isLastPageSubComment)return
        if(reset) currentPageSubComment = 0

        _postSubComments.value= UiState.Loading
        isLoadingSubComment=true

        viewModelScope.launch {
            val result=repo.subCommentsGet(postId,currentPageSubComment,pageSizeSubComment)
            when(result){
                is ResultWrapper.Success -> {

                    val currentList = (_postSubComments.value as? UiState.Success)?.data ?: emptyList()
                    val newList = if (reset) result.data else currentList + result.data
                    _postSubComments.value = UiState.Success(newList)
                    isLastPageSubComment = result.data.size < pageSizeSubComment
                    if (!isLastPageSubComment) currentPageSubComment++
                }
                is ResultWrapper.Error -> _postSubComments.value = UiState.Error(result.message)
            }
            isLoadingSubComment=false
        }
    }
    fun toggleLike(
        commentId: Long
    ) {
        _commentsLike.value= UiState.Loading
        viewModelScope.launch {
            val result=repo.toggleLike(commentId)
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