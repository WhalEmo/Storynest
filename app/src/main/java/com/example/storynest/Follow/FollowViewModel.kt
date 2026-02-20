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
                                visibleViews = updatedState.actionState.toVisibleViews(),
                                requestId = updatedState.requestId
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

    fun sendFollowRequest(userId: Long, followType: FollowType){
        viewModelScope.launch {
            try {
                val response = repository.followMyFollower(user.STATIC_USER_ID.toLong(), userId)
                if (response.isSuccessful){
                    response.body().let { followResponseDTO ->
                        if(followResponseDTO != null){
                            val action = dtoToAction(
                                dto = followResponseDTO,
                                followType = followType
                            )
                            Log.e("action", action.toString())
                            Log.e("action", followResponseDTO.toString())
                            updateUserActionState(userId, action, followResponseDTO.id)
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
    
    fun cancelFollowRequest(followId: Long?, followType: FollowType){
        viewModelScope.launch {
            try {
                if(followId == null) return@launch
                val response = repository.cancelFollowRequest(followId)
                if (response.isSuccessful){
                    val responseBody = response.body()
                    if(responseBody?.status == FollowRequestStatus.CANCEL){
                        val action = dtoToAction(
                            dto = responseBody,
                            followType = followType
                        )
                        updateUserActionState(responseBody.requested.userId, action, responseBody.id)
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
                        updateUserActionState(userId, action, -1)
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

    private fun updateUserActionState(
        userId: Long,
        newState: FollowActionState,
        newRequestId: Long
    ) {
        _actionUpdates.value =
            _actionUpdates.value.toMutableMap().apply {
                put(
                    userId,
                    FollowActionDataState(
                        actionState = newState,
                        requestId = newRequestId
                    )
                )
            }
    }

    private fun mapToFollowRow(
        dto: FollowUserResponseDTO,
        followType: FollowType
    ): FollowRow.FollowUserItem {

        val action = dtoToAction(
            dto = dto.followInfo,
            followType = followType,
            userFollowDto = dto
        )
        return FollowRow.FollowUserItem(
            id = dto.id,
            username = dto.username,
            biography = dto.biography,
            profile = dto.profile,
            visibleViews = action.toVisibleViews(),
            requestId = dto.followInfo?.id
        )
    }

    private fun dtoToAction(
        dto: FollowResponseDTO?,
        followType: FollowType,
        userFollowDto: FollowUserResponseDTO? = null
    ): FollowActionState {
        return when(followType) {
            FollowType.MY_FOLLOWERS ->{
                myFollowersActionUiState(dto)
            }
            FollowType.MY_FOLLOWING -> myFollowingActionUiState(dto)
            FollowType.USER_FOLLOWERS -> otherUserFollowersActionUiState(dto, userFollowDto?.id)
            FollowType.USER_FOLLOWING -> otherUserFollowingActionUiState(dto, userFollowDto?.id)
        }
    }

    private fun myFollowersActionUiState(
        dto: FollowResponseDTO?
    ): FollowActionState{
        return when {
            dto == null ->{
                FollowActionState.FOLLOWER_ACCEPT
            }
            dto.myFollower && !dto.followingYou ->
                FollowActionState.FOLLOWER_ACCEPT

            dto.status == FollowRequestStatus.ACCEPTED
                    && dto.requester.userId == user.STATIC_USER_ID ->
                FollowActionState.FOLLOWER_MESSAGE

            dto.status == FollowRequestStatus.PENDING
                    && dto.requester.userId == user.STATIC_USER_ID ->
                FollowActionState.FOLLOWER_PENDING
            else ->
                FollowActionState.FOLLOWER_ACCEPT
        }
    }

    private fun myFollowingActionUiState(
        dto: FollowResponseDTO?
    ): FollowActionState{
        return FollowActionState.FOLLOWING
    }

    private fun otherUserFollowersActionUiState(
        followInfo: FollowResponseDTO?,
        userId: Long?
    ): FollowActionState{
        Log.e("userFollowDto", followInfo.toString())
        return when{
            userId == user.STATIC_USER_ID ->{
                FollowActionState.MY_USER_ITEM
            }
            followInfo == null -> {
                FollowActionState.OTHER_USER_ITEM
            }
            followInfo.myFollower && !followInfo.followingYou ->{
                FollowActionState.OTHER_USER_ACCEPT
            }
            followInfo.requester.userId == user.STATIC_USER_ID ->{
                requesterFollowAction(followInfo)
            }
            followInfo.requested.userId == user.STATIC_USER_ID ->{
                requestedFollowAction(followInfo)
            }
            else ->
                FollowActionState.OTHER_USER_ITEM
        }
    }
    private fun otherUserFollowingActionUiState(
        followInfo: FollowResponseDTO?,
        userId: Long?
    ): FollowActionState{
        Log.e("userFollowDto", followInfo.toString())
        return when{
            userId == user.STATIC_USER_ID ->{
                FollowActionState.MY_USER_ITEM
            }
            followInfo == null -> {
                FollowActionState.OTHER_USER_ITEM
            }
            followInfo.myFollower && !followInfo.followingYou ->{
                FollowActionState.OTHER_USER_ACCEPT
            }
            followInfo.requester.userId == user.STATIC_USER_ID ->{
                requesterFollowAction(followInfo)
            }
            followInfo.requested.userId == user.STATIC_USER_ID ->{
                requestedFollowAction(followInfo)
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
        }
    }

    private fun requesterFollowAction(
        dto: FollowResponseDTO
    ): FollowActionState{
        when{
            dto.status == FollowRequestStatus.ACCEPTED ->{
                return FollowActionState.OTHER_USER_MESSAGE
            }
            dto.status == FollowRequestStatus.PENDING ->{
                return FollowActionState.OTHER_USER_PENDING
            }
            dto.status == FollowRequestStatus.CANCEL ->{
                return FollowActionState.OTHER_USER_ITEM
            }
            else ->{
                return FollowActionState.MY_USER_ITEM
            }
        }
    }

    private fun requestedFollowAction(
        dto: FollowResponseDTO
    ): FollowActionState{
        when{
            dto.myFollower && !dto.followingYou ->{
                return FollowActionState.OTHER_USER_ACCEPT
            }
            dto.status == FollowRequestStatus.PENDING ->{
                return FollowActionState.OTHER_USER_PENDING
            }
            dto.status == FollowRequestStatus.CANCEL ->{
                return FollowActionState.OTHER_USER_ITEM
            }
            else ->{
                return FollowActionState.MY_USER_ITEM
            }
        }
    }

}