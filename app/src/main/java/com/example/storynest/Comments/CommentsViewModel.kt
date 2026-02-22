package com.example.storynest.Comments

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
import com.example.storynest.Comments.viewModelhelper.ReplyThread
import com.example.storynest.CustomViews.UiEvents
import com.example.storynest.ResultWrapper
import com.example.storynest.UiState
import kotlinx.coroutines.channels.Channel
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
import kotlin.text.toLong


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

    private val api = ApiClient


    private val _pinnedCount = MutableStateFlow(0L)
    val pinnedCount: StateFlow<Long> = _pinnedCount

    fun removeComment(commentId: Long) {
        _removedCommentIds.update { it + commentId }
    }

    fun updateComment(comment: commentResponse) {
        _updatedComments.value = _updatedComments.value + (comment.comment_id to comment)
    }
    private val MAX_PIN_COUNT = 5


    private val _postId = MutableStateFlow<Long?>(null)
    val postId: StateFlow<Long?> = _postId

    private val _newComments = MutableStateFlow<List<commentResponse>>(emptyList())
    val newComments = _newComments.asStateFlow()

    private val _pinnedComment = MutableStateFlow<List<commentResponse>>(emptyList())

    val pinnedComments = _pinnedComment.asStateFlow()



    fun setPostId(id: Long) {
        _postId.value = id
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
                         updateComment(result.data)
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


    fun getComments(postId: Long): Flow<PagingData<commentResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 3
            ),
            pagingSourceFactory = {
                CommentPagingSource(
                    postId = postId,
                    api = api,
                    pinnedCountState = _pinnedCount
                )
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

        val thread = uiState.replyThreads[comment.comment_id]
        if (thread == null) {
            if (comment.replyCount > 0) {
                add(CommentsUiModel.ViewRepliesItem(comment.comment_id, comment.replyCount, false))
            }
        } else {
            thread.replies.take(thread.visibleCount).forEach { reply ->
                if (reply.comment_id !in uiState.removedIds) {
                   // add(CommentsUiModel.ReplyItem(reply))
                }
            }
            val remaining = thread.totalCount - thread.visibleCount
            if (remaining > 0 || thread.isLoading) {
                //add(CommentsUiModel.ViewRepliesItem(comment.comment_id, remaining.coerceAtLeast(0), true, thread.isLoading))
            }
        }
    }

    private fun injectDynamicHeaders(
        data: PagingData<CommentsUiModel>,
        uiState: CommentUiState
    ): PagingData<CommentsUiModel> {
        var base = data
        uiState.newItems.filter { it.comment_id !in uiState.removedIds }.reversed().forEach {
            base = base.insertHeaderItem(item=CommentsUiModel.CommentItem(it.toUiItem()))
        }
        uiState.pinnedItems.filter { it.comment_id !in uiState.removedIds }.reversed().forEach {
            base = base.insertHeaderItem(item=CommentsUiModel.CommentItem(it.toUiItem().copy(isPin = true)))

        }
        return base
    }

    /*
    val comments: Flow<PagingData<CommentsUiModel>> =
        combine(
            pagingComments,
            commentUiState

        ) {pagingData, uiState->
            var baseData:PagingData<CommentsUiModel> =
                pagingData
                    .filter { pagingItem ->
                        !uiState.removedIds.contains(pagingItem.comment_id) &&
                                uiState.pinnedItems.none { it.comment_id == pagingItem.comment_id }
                    }
                    .flatMap { comment ->
                        val updatedComment = uiState.updates[comment.comment_id] ?: comment

                        buildList {
                            add(CommentsUiModel.CommentItem(updatedComment.toUiItem()))

                            val thread = uiState.replyThreads[comment.comment_id]

                            if (thread == null) {
                                if (comment.replyCount > 0) {
                                    add(
                                        CommentsUiModel.ViewRepliesItem(
                                            parentCommentId = comment.comment_id,
                                            remainingCount = comment.replyCount,
                                            isLoadMore = false
                                        )
                                    )
                                }
                            } else {

                                thread.replies.take(thread.visibleCount).forEach { reply ->
                                    add(CommentsUiModel.ReplyItem(reply))
                                }

                                val remaining = thread.totalCount - thread.visibleCount
                                if (remaining > 0) {
                                    add(
                                        CommentsUiModel.ViewRepliesItem(
                                            parentCommentId = comment.comment_id,
                                            remainingCount = remaining.toLong(),
                                            isLoadMore = true
                                        )
                                    )
                                }
                            }

                        }
                    }

            uiState.pinnedItems.take(5).reversed().forEach { pinnedItem ->
                if (pinnedItem.comment_id !in uiState.removedIds) {

                    val currentState = uiState.updates[pinnedItem.comment_id]
                        ?: pinnedItem
                        val mergedItem = currentState.copy(
                            isPinned = true,
                        )
                        val uiItem = mergedItem.toUiItem()
                        baseData = baseData.insertHeaderItem(
                            item = CommentsUiModel.CommentItem(uiItem)
                        )
                }
            }

            uiState.newItems
                .filter { newItem ->
                    newItem.comment_id !in uiState.removedIds &&
                            uiState.pinnedItems.none { it.comment_id == newItem.comment_id }
                }
                .reversed()
                .forEach { newItem ->
                    baseData = baseData.insertHeaderItem(
                        item = CommentsUiModel.CommentItem(newItem.toUiItem())
                    )
                }

            baseData
        }

     */
    /*
    fun fetchReplies(parentCommentId: Long) {
        val currentThread = _replyThreads.value[parentCommentId] ?: ReplyThread()
        if (currentThread.isLoading) return

        viewModelScope.launch {
            updateThread(parentCommentId) { it.copy(isLoading = true) }
            val result = repo.subCommentsGet(parentCommentId, currentThread.currentPage)
            when (result) {
                is ResultWrapper.Success -> {
                    updateThread(parentCommentId) { state ->

                        val totalReplies = state.replies + result.data
                        state.copy(
                            replies = totalReplies,
                            visibleCount = totalReplies.size,
                            totalCount = result.totalCount ?: totalReplies.size.toLong(),
                            currentPage = state.currentPage + 1,
                            isLoading = false
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


     */


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


