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
    private val repository: FollowRepository = FollowRepository()

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _params = MutableStateFlow<FollowParams?>(null)

    fun setParams(followType: FollowType, userId: Long?) {
        _params.value = FollowParams(followType, userId)
    }

    private val _actionUpdates =
        MutableStateFlow<Map<Long, FollowActionUiState>>(emptyMap())


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
            followUpdates,
            removedUserIds
        ) { pagindata, followMap, removedIds ->
            pagindata
                .filter { row ->
                    if (row is FollowRow.FollowUserItem) {
                        !removedIds.contains(row.followUserResponseDTO.id)
                    } else true
                }
                .map { row ->
                    if (row is FollowRow.FollowUserItem) {
                        val update = followMap[row.followUserResponseDTO.id]
                        if (update != null) {
                            Log.e("update", row.toString())
                            Log.e("update", update.toString())
                            row.copy(
                                followUserResponseDTO = row.followUserResponseDTO.copy(
                                    followInfo = update,
                                    followingYou = update.status == FollowRequestStatus.ACCEPTED
                                            || update.status == FollowRequestStatus.PENDING
                                )
                            )
                        } else row
                    } else row
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

    fun sendFollowRequest(userId: Long){
        viewModelScope.launch {
            try {
                val response = repository.followMyFollower(user.STATIC_USER_ID.toLong(), userId)
                if (response.isSuccessful){
                    response.body().let { followResponseDTO ->
                        if(followResponseDTO != null){
                           val action = dtoToAction()
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

    fun cancelFollowRequest(followId: Long){
        viewModelScope.launch {
            try {
                val response = repository.cancelFollowRequest(followId)
                if (response.isSuccessful){
                    val responseBody = response.body()
                    if(responseBody?.status == FollowRequestStatus.CANCEL){
                        followUpdates.value = followUpdates.value.orEmpty() + (responseBody.requested.userId to responseBody)
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
        onRemoved: () -> Unit
    ){
        viewModelScope.launch {
            try {
                val response = repository.removeFollower(userId)
                Log.e("response", response.toString())
                if(response.isSuccessful){
                    val responseBody = response.body()
                    removedUserIds.value = removedUserIds.value.orEmpty() + (responseBody?.followedId ?: 0)
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
        newState: FollowActionUiState
    ) {
        _actionUpdates.value =
            _actionUpdates.value.toMutableMap().apply {
                put(userId, newState)
            }
    }

    private fun mapToFollowRow(
        dto: FollowUserResponseDTO,
        followType: FollowType
    ): FollowRow.FollowUserItem {

        val action = dtoToAction(dto)

        return FollowRow.FollowUserItem(
            id = dto.id,
            username = dto.username,
            biography = dto.biography,
            profile = dto.profile,
            actionState = action
        )
    }

    private fun dtoToAction(
        dto: FollowUserResponseDTO
    ): FollowActionUiState {
        return when {
            dto.myFollower && !dto.followingYou ->
                FollowActionUiState.ShowAccept

            dto.followInfo.status == FollowRequestStatus.ACCEPTED ->
                FollowActionUiState.ShowMessage

            dto.followInfo.status == FollowRequestStatus.PENDING ->
                FollowActionUiState.ShowPending

            else ->
                FollowActionUiState.ShowAccept
        }
    }

}