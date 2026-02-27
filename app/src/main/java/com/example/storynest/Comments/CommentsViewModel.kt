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
import androidx.paging.flatMap
import androidx.paging.insertHeaderItem
import com.example.storynest.ApiClient
import com.example.storynest.Comments.viewModelhelper.BaseState
import com.example.storynest.Comments.viewModelhelper.CommentMapper.toUiItem
import com.example.storynest.Comments.viewModelhelper.CommentUiState
import com.example.storynest.Comments.viewModelhelper.ReplyAction
import com.example.storynest.Comments.viewModelhelper.ReplyThread
import com.example.storynest.CustomViews.UiEvents
import com.example.storynest.ResultWrapper
import com.example.storynest.UiState
import kotlinx.coroutines.channels.Channel
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class CommentsViewModel(
    private val repo: CommentRepo
) : ViewModel() {

    private val _commentAddState = MutableSharedFlow<Boolean>()
    val commentAddState = _commentAddState.asSharedFlow()

    private val _addSubCommentResult = MutableLiveData<UiState<commentResponse>>()
    val addSubCommentResult: LiveData<UiState<commentResponse>> = _addSubCommentResult


    private val _subCommentsState =
        MutableStateFlow<Map<Long, UiState<List<commentResponse>>>>(emptyMap())

    val subCommentsState: StateFlow<Map<Long, UiState<List<commentResponse>>>> = _subCommentsState

    private val _removedCommentIds = MutableStateFlow<Set<Long>>(emptySet())
    val removedCommentIds: StateFlow<Set<Long>> = _removedCommentIds

    private val _updatedComments = MutableStateFlow<Map<Long, commentResponse>>(emptyMap())
    val updatedComments: StateFlow<Map<Long, commentResponse>> = _updatedComments.asStateFlow()

    private val _pinState = MutableSharedFlow<Boolean>()
    val pinState = _pinState.asSharedFlow()

    private val _uiEvent = Channel<UiEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val api = ApiClient.commentApi


    fun removeComment(commentId: Long) {
        _removedCommentIds.update { it + commentId }
    }

    fun updateComment(comment: commentResponse) {
        _updatedComments.value = _updatedComments.value + (comment.comment_id to comment)
    }
    private val MAX_PIN_COUNT = 5


    private val _postId = MutableStateFlow<Long?>(null)
    val postId: StateFlow<Long?> = _postId

    private val _pinnedCount = MutableStateFlow(0L)
    val pinnedCount: StateFlow<Long> = _pinnedCount


    private val _newComments = MutableStateFlow<List<commentResponse>>(emptyList())
    val newComments = _newComments.asStateFlow()

    private val _pinnedComment = MutableStateFlow<List<commentResponse>>(emptyList())

    val pinnedComments = _pinnedComment.asStateFlow()



    fun setPostId(id: Long) {
        _postId.value = id
    }
    fun setPinnedCount(count: Long) {
        _pinnedCount.value =count
    }

    val pagingComments: Flow<PagingData<commentResponse>> =
        postId
            .filterNotNull()
            .flatMapLatest { id ->
                getComments(id)
            }



    fun addComment(
        postId: Long,
        userId: Long?,
        contents: String,
        parentCommentId: Long?
    ) {
        val request = commentRequest(postId, userId, contents, null,null)
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

    fun pinComments(commentId: Long) {
        if (pinnedCount.value >= MAX_PIN_COUNT) {
            _uiEvent.trySend(
                UiEvents.showInfoMessage("En fazla $MAX_PIN_COUNT yorum sabitleyebilirsiniz.")
            )
            return
        }

        viewModelScope.launch {
            val result = repo.pin(commentId)
            when (result) {
                is ResultWrapper.Success -> {
                    _pinnedCount.update { current ->
                        current + 1
                    }
                    _pinState.emit(true)
                    _pinnedComment.update { currentList ->
                        listOf(result.data) + currentList
                    }
                }
                is ResultWrapper.Error -> {
                    UiState.Error(result.message)
                }
            }
        }
    }

    fun removePin(commentId:Long){
        viewModelScope.launch {
            val result= repo.removePin(commentId)
                when(result){
                    is ResultWrapper.Success -> {
                        _pinnedCount.update { current ->
                            current - 1
                        }
                        // updateComment(result.data)
                        _pinnedComment.update { currentList ->
                            currentList.filterNot{ it.comment_id == result.data.comment_id }
                        }
                        _uiEvent.trySend(UiEvents.showInfoMessage("Sabitleme kaldırıldı."))
                    }
                    is ResultWrapper.Error -> {
                        UiState.Error(result.message)
                    }
                }
        }
    }


    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()
    fun addSubComment(
        postId: Long,
        userId: Long?,
        contents: String,
        parentUsername:String,
        parentCommentId: Long
    ) {
        val request = commentRequest(postId, userId, contents, parentUsername,parentCommentId)

        viewModelScope.launch {
            val result = repo.addSubComment(request)
            when (result) {
                is ResultWrapper.Success -> {
                    _commentAddState.emit(true)
                    _newComments.update { currentList ->
                        listOf(result.data) + currentList
                    }
                }
                is ResultWrapper.Error -> {
                    _commentAddState.emit(false)
                    _errorMessage.emit(result.toString() ?: "Bilinmeyen hata oluştu")
                }
            }
        }
    }


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


    private val _replyThreads =
        MutableStateFlow<Map<Long, ReplyThread>>(emptyMap())

    val replyThreads = _replyThreads.asStateFlow()

    private val baseState =
        combine(
            removedCommentIds,
            updatedComments,
            newComments,
            pinnedComments
        ) { removedIds, updates, newItems, pinnedIds ->

            BaseState(
                removedIds = removedIds,
                updates = updates,
                newItems = newItems,
                pinnedIds = pinnedIds
            )
        }

    private val commentUiState =
        combine(baseState, replyThreads) { base, replies ->
            CommentUiState(
                removedIds = base.removedIds,
                updates = base.updates,
                newItems = base.newItems,
                pinnedItems = base.pinnedIds,
                replyThreads = replies
            )
        }


    val comments: Flow<PagingData<CommentsUiModel>> = pagingComments
        .combine(commentUiState) { pagingData, uiState ->
            pagingData
                .filter { it.comment_id !in uiState.removedIds && uiState.pinnedItems.none { p -> p.comment_id == it.comment_id } }
                .flatMap { comment ->
                    val updated = uiState.updates[comment.comment_id] ?: comment
                    transformCommentToFlatList(updated, uiState)
                }
                .let { injectDynamicHeaders(it, uiState) }
        }

    private fun transformCommentToFlatList(
        comment: commentResponse,
        uiState: CommentUiState
    ): List<CommentsUiModel> = buildList {
        add(CommentsUiModel.CommentItem(comment.toUiItem()))

        uiState.newItems
            .filter { it.parentCommentId == comment.comment_id && it.comment_id !in uiState.removedIds }
            .forEach{ newReply ->
                val updatedNewReply = uiState.updates[newReply.comment_id] ?: newReply
                add(CommentsUiModel.ReplyItem(updatedNewReply.toUiItem()))
            }

        val thread = uiState.replyThreads[comment.comment_id]

       if (thread == null|| !thread.isExpanded) {
            if (comment.subCommentsCount > 0) {
              //  add(CommentsUiModel.ViewRepliesItem(comment.comment_id, comment.subCommentsCount, comment.subCommentsCount,false,false))
                add(
                    CommentsUiModel.ViewRepliesItem(
                        replyView = viewReplysUiItem(
                            nextAction = ReplyAction.LOAD_MORE,
                            parentCommentId = comment.comment_id,
                            remainingCount = comment.subCommentsCount,
                            totalSubCount = comment.subCommentsCount,
                            isLoadMore = false,
                            isLoading = false
                        )
                    )
                )
            }
       } else {
            thread.replies.take(thread.visibleCount).forEach{ reply ->
                if (reply.comment_id !in uiState.removedIds) {
                    val updatedReply = uiState.updates[reply.comment_id] ?: reply
                    add(CommentsUiModel.ReplyItem(updatedReply.toUiItem()))
                }
            }
            val remaining = thread.totalCount - thread.visibleCount

            if (remaining > 0 || thread.isLoading) {
                add(CommentsUiModel.ViewRepliesItem(
                    replyView = viewReplysUiItem(
                        nextAction = ReplyAction.LOAD_MORE,
                        parentCommentId = comment.comment_id,
                        remainingCount = remaining,
                        totalSubCount = thread.totalCount,
                        isLoadMore = true,
                        isLoading = thread.isLoading
                    )
                  )
                )
            }else if (remaining == 0L) {
                add(
                    CommentsUiModel.ViewRepliesItem(
                        replyView = viewReplysUiItem(
                            nextAction = ReplyAction.HIDE,
                            parentCommentId = comment.comment_id,
                            remainingCount = 0,
                            totalSubCount = thread.totalCount,
                            isLoadMore = true,
                            isLoading = false
                        )
                    )
                )
            }
        }
    }
    private fun injectDynamicHeaders(
        data: PagingData<CommentsUiModel>,
        uiState: CommentUiState
    ): PagingData<CommentsUiModel> {
        var base = data

        uiState.pinnedItems.filter { it.comment_id !in uiState.removedIds }.reversed().forEach {
            val pinnedWithStatus = it.copy(isPinned = true)
            val flatItems = transformCommentToFlatList(pinnedWithStatus, uiState)
            flatItems.reversed().forEach { item ->
                base = base.insertHeaderItem(item = item)
            }

        }
        uiState.newItems.filter {
            it.comment_id !in uiState.removedIds && it.parentCommentId == null&&
                    uiState.pinnedItems.none { p -> p.comment_id == it.comment_id }
        }.reversed().forEach {
            val flatItems = transformCommentToFlatList(it, uiState)
            flatItems.reversed().forEach { item ->
                base = base.insertHeaderItem(item = item)
            }
        }
        return base
    }


    fun fetchReplies(parentCommentId: Long, totalComments:Long, reset:Boolean) {
        val currentThread = _replyThreads.value[parentCommentId] ?: ReplyThread()

        if (currentThread.isLoading) return
        if (currentThread.replies.isNotEmpty()) {
            if (!currentThread.isExpanded) {
                updateThread(parentCommentId) { it.copy(isExpanded = true) }
                return
            }

            if (currentThread.replies.size.toLong() >= totalComments && !reset) {
                return
            }
        }

        viewModelScope.launch {
            updateThread(parentCommentId) {
                if (reset) ReplyThread(isLoading = true, isExpanded = true)
                else it.copy(isLoading = true, isExpanded = true)
            }

            val pageToFetch = if (reset) 0 else currentThread.currentPage
            val result = repo.subCommentsGet(parentCommentId, pageToFetch)

            when (result) {
                is ResultWrapper.Success -> {
                    delay(500)
                    val newReplies = result.data.orEmpty()

                    updateThread(parentCommentId) { state ->
                        val totalReplies = state.replies + newReplies
                        val allFetched = totalReplies.size.toLong() >= totalComments
                        state.copy(
                            replies = totalReplies,
                            visibleCount = totalReplies.size,
                            totalCount = totalComments,
                            currentPage = state.currentPage + 1,
                            isLoadMore=!allFetched,
                            isLoading = false,
                            isExpanded = true
                        )
                    }
                }
                is ResultWrapper.Error -> {
                    updateThread(parentCommentId) { it.copy(isLoading = false) }
                    _uiEvent.trySend(UiEvents.showInfoMessage("Hata: ${result.message}"))
                }
            }
        }
    }
    fun onHideReplies(parentCommentId: Long) {
        updateThread(parentCommentId) { state ->
            state.copy(isExpanded = false)
        }
    }



    private fun updateThread(commentId: Long, update: (ReplyThread) -> ReplyThread) {
        _replyThreads.update { currentMap ->
            val thread = currentMap[commentId] ?: ReplyThread()
            currentMap + (commentId to update(thread))
        }
    }


    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            val result = repo.deleteComment(commentId)
            when (result) {
                is ResultWrapper.Success -> {
                    removeComment(commentId)
                    val isPinned = _pinnedComment.value.any { it.comment_id == commentId }
                    if (isPinned) {
                        _pinnedCount.update { (it - 1).coerceAtLeast(0) }
                        _pinnedComment.update { list ->
                            list.filterNot { it.comment_id == commentId }
                        }
                    }
                }
                is ResultWrapper.Error ->{
                    UiState.Error(result.message)
                }
            }
        }
    }




    fun updateComment(commentId: Long, contents: String) {
        val request = update(contents)
        viewModelScope.launch {
            val result = repo.updateComment(commentId, request)
            when (result) {
                is ResultWrapper.Success -> {
                    val updatedComment = result.data
                    updateComment(updatedComment)
                    _newComments.update { currentList ->
                        currentList.map {
                            if (it.comment_id == updatedComment.comment_id) updatedComment else it
                        }
                    }
                    _pinnedComment.update { currentList ->
                        currentList.map {
                            if (it.comment_id == updatedComment.comment_id) updatedComment else it
                        }
                    }
                }
                is ResultWrapper.Error -> {
                    UiState.Error(result.message)
                }
            }
        }
    }


    fun toggleLike(uiItem: commentUiItem) {
        val commentId = uiItem.commentId
        viewModelScope.launch {
            when ( val result=repo.toggleLike(commentId)) {
                is ResultWrapper.Success -> {
                    val updatedComment = result.data
                    updateComment(updatedComment)
                    _newComments.update { currentList ->
                        currentList.map {
                            if (it.comment_id == updatedComment.comment_id) updatedComment else it
                        }
                    }
                    _pinnedComment.update { currentList ->
                        currentList.map {
                            if (it.comment_id == updatedComment.comment_id) updatedComment else it
                        }
                    }
                }
                is ResultWrapper.Error -> {
                }
            }
        }
    }

}


