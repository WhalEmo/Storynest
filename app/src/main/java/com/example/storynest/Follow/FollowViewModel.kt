package com.example.storynest.Follow

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
import androidx.paging.map
import com.example.storynest.Follow.Paging.FollowPagingSource
import com.example.storynest.Follow.RequestDTO.FollowDTO
import com.example.storynest.Follow.ResponseDTO.FollowResponse
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import com.example.storynest.Notification.FollowRequestStatus
import com.example.storynest.Notification.FollowResponseDTO
import com.example.storynest.TestUserProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class FollowViewModel: ViewModel() {
    private val repository: FollowRepository = FollowRepository

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _params = MutableStateFlow<FollowParams?>(null)

    fun setParams(followType: FollowType, userId: Long?) {
        _params.value = FollowParams(followType, userId)
    }

    private val _actionUpdates =
        MutableStateFlow<Map<Long, FollowActionDataState>>(emptyMap())


    private var user = TestUserProvider

    private val followUpdates =
        MutableStateFlow<Map<Long, FollowResponseDTO>>(emptyMap())

    private val removedUserIds = MutableStateFlow<Set<Long>>(emptySet())

    val pagingFollowers = _params
        .filterNotNull()
        .flatMapLatest { params ->
            getFollowList(params.followType, params.userId)
        }


    init {
        viewModelScope.launch {
            pagingFollowers.first()
        }
    }

    val followers: Flow<PagingData<FollowRow>> =
        combine(
            pagingFollowers,
            _actionUpdates
        ) { pagindata, actionUpdates ->
            pagindata
                .map {
                    row ->
                    if (row is FollowRow.FollowUserItem){
                        val updatedState = actionUpdates[row.id]
                        if(updatedState!=null){
                            row.copy(
                                visibleViews = updatedState.actionState.toVisibleViews()
                            )
                        }else row
                    }else row
                }
        }


    private fun getFollowList(
        followType: FollowType,
        userId: Long?
    ): Flow<PagingData<FollowRow>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FollowPagingSource(
                    repository,
                    followType,
                    userId
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { dto ->
               mapToFollowRow(dto, followType) as FollowRow
            }
        }.cachedIn(viewModelScope)
    }

    fun followUser(userId: Long, followType: FollowType){
        viewModelScope.launch {
            try {
                val response = repository.follow(userId)
                if (response.isSuccessful){
                    response.body().let { followResponse ->
                        if(followResponse != null){
                            val action = dtoToAction(
                                dto = followResponse,
                                followType = followType
                            )
                            updateUserActionState(userId, action)
                        }
                    }
                }
            }
            catch (e: HttpException){
                _error.value = "Sunucuya bağlanılamadı (HTTP ${e.code()})"
            }
            catch (e: IOException) {
                _error.value = "İnternet bağlantısı yok!"
            }
            catch (e: Exception) {
                _error.value = "Beklenmeyen bir hata oluştu"
            }
        }
    }


    fun removeFollower(
        userId: Long,
        followType: FollowType,
        onRemoved: () -> Unit
    ){
        viewModelScope.launch {
            try {
                val response = repository.removeFollower(userId)
                Log.e("response", response.toString())
                if(response.isSuccessful){
                    val responseBody = response.body()
                    if (responseBody != null){
                        val action = FollowActionState.REMOVE_FOLLOWER
                        updateUserActionState(userId, action)
                    }
                    onRemoved.invoke()
                }
            }
            catch (e: HttpException){
                _error.value = "Sunucuya bağlanılamadı (HTTP ${e.code()})"
            }
            catch (e: IOException) {
                _error.value = "İnternet bağlantısı yok!"
            }
            catch (e: Exception) {
                _error.value = "Beklenmeyen bir hata oluştu"
            }
        }
    }

    fun unFollow(
        userId: Long,
        followType: FollowType
    ){
        viewModelScope.launch {
            try {
                val response = repository.unfollow(userId)
                response.body()?.let { dto ->
                    val action = dtoToAction(dto, followType)
                    updateUserActionState(userId, action)
                }
            }
            catch (e: HttpException){
                _error.value = "Sunucuya bağlanılamadı (HTTP ${e.code()})"
            }
            catch (e: IOException) {
                _error.value = "İnternet bağlantısı yok!"
            }
            catch (e: Exception) {
                _error.value = "Beklenmeyen bir hata oluştu"
            }
        }
    }

    private fun updateUserActionState(
        userId: Long,
        newState: FollowActionState
    ) {
        _actionUpdates.value =
            _actionUpdates.value.toMutableMap().apply {
                put(
                    userId,
                    FollowActionDataState(
                        actionState = newState
                    )
                )
            }
    }

    private fun mapToFollowRow(
        dto: FollowResponse,
        followType: FollowType
    ): FollowRow.FollowUserItem {

        val action = dtoToAction(
            dto = dto,
            followType = followType
        )
        return FollowRow.FollowUserItem(
            id = dto.userId,
            username = dto.username,
            biography = dto.bio,
            profile = dto.profileUrl,
            visibleViews = action.toVisibleViews()
        )
    }

    private fun dtoToAction(
        dto: FollowResponse,
        followType: FollowType
    ): FollowActionState {
        return when(followType) {
            FollowType.MY_FOLLOWERS ->{
                myFollowersActionUiState(dto)
            }
            FollowType.MY_FOLLOWING -> myFollowingActionUiState(dto)
            FollowType.USER_FOLLOWERS -> otherUserFollowersActionUiState(dto)
            FollowType.USER_FOLLOWING -> otherUserFollowingActionUiState(dto)
        }
    }

    private fun myFollowersActionUiState(
        dto: FollowResponse
    ): FollowActionState{
        return when {
            dto.follower && !dto.following ->
                FollowActionState.FOLLOWER_ACCEPT

            dto.follower && dto.following ->
                FollowActionState.FOLLOWER_MESSAGE

            dto.pending && !dto.following ->
                FollowActionState.FOLLOWER_PENDING
            else ->
                FollowActionState.FOLLOWER_ACCEPT
        }
    }

    private fun myFollowingActionUiState(
        dto: FollowResponse
    ): FollowActionState{
        return when{
            dto.following ->
                FollowActionState.FOLLOWING
            dto.follower && !dto.following ->
                FollowActionState.FOLLOWING_YOUR_FOLLOW
            dto.pending && !dto.following ->
                FollowActionState.FOLLOWING_PENDING
            else ->
                FollowActionState.REMOVE_FOLLOWING
        }
    }

    private fun otherUserFollowersActionUiState(
        dto: FollowResponse,
    ): FollowActionState{
        Log.e("userFollowDto", dto.toString())
        return when{
            dto.userId == user.STATIC_USER_ID ->{
                FollowActionState.MY_USER_ITEM
            }
            dto.follower && !dto.following ->{
                FollowActionState.OTHER_USER_ACCEPT
            }
            dto.follower && dto.following ->{
                FollowActionState.OTHER_USER_MESSAGE
            }
            dto.pending && !dto.following ->{
                FollowActionState.OTHER_USER_PENDING
            }
            else ->
                FollowActionState.OTHER_USER_ITEM
        }
    }
    private fun otherUserFollowingActionUiState(
        dto: FollowResponse
    ): FollowActionState{
        Log.e("userFollowDto", dto.toString())
        return when{
            dto.userId == user.STATIC_USER_ID ->{
                FollowActionState.MY_USER_ITEM
            }
            dto.follower && !dto.following ->{
                FollowActionState.OTHER_USER_ACCEPT
            }
            dto.follower && dto.following ->{
                FollowActionState.OTHER_USER_MESSAGE
            }
            dto.pending && !dto.following ->{
                FollowActionState.OTHER_USER_PENDING
            }
            else ->
                FollowActionState.OTHER_USER_ITEM
        }
    }

    private fun FollowActionState.toVisibleViews(): Set<FollowViewType> {
        return when (this) {
            FollowActionState.FOLLOWER_ACCEPT ->
                setOf(FollowViewType.UNFOLLOW, FollowViewType.ACCEPT)

            FollowActionState.FOLLOWER_MESSAGE ->
                setOf(FollowViewType.UNFOLLOW, FollowViewType.MESSAGE)

            FollowActionState.FOLLOWER_PENDING ->
                setOf(FollowViewType.UNFOLLOW, FollowViewType.PENDING)

            FollowActionState.FOLLOWING ->
                setOf(FollowViewType.DOT_MENU, FollowViewType.MESSAGE)

            FollowActionState.MY_USER_ITEM ->
                setOf()

            FollowActionState.OTHER_USER_ITEM ->
                setOf(FollowViewType.FOLLOW)

            FollowActionState.OTHER_USER_PENDING ->
                setOf(FollowViewType.PENDING)

            FollowActionState.OTHER_USER_MESSAGE ->
                setOf(FollowViewType.MESSAGE)

            FollowActionState.OTHER_USER_ACCEPT ->
                setOf(FollowViewType.ACCEPT)

            FollowActionState.REMOVE_FOLLOWER ->
                setOf(FollowViewType.REMOVE_FOLLOWER)

            FollowActionState.REMOVE_FOLLOWING ->
                setOf(FollowViewType.FOLLOW, FollowViewType.DOT_MENU)

            FollowActionState.FOLLOWING_PENDING ->
                setOf(FollowViewType.PENDING, FollowViewType.DOT_MENU)

            FollowActionState.FOLLOWING_YOUR_FOLLOW ->
                setOf(FollowViewType.ACCEPT, FollowViewType.DOT_MENU)
        }
    }


}