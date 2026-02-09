package com.example.storynest.Follow.MyFollowProcesses.MyFollowers


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersRow
import com.example.storynest.TestUserProvider
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersRow.FollowerUserItem
import com.example.storynest.Notification.FollowRequestStatus
import com.example.storynest.Notification.FollowResponseDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


class MyFollowersViewModel: ViewModel() {
    private val service: MyFollowersService = MyFollowersService()

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var user = TestUserProvider

    private val followUpdates =
        MutableStateFlow<Map<Long, FollowResponseDTO>>(emptyMap())

    private val removedUserIds = MutableStateFlow<Set<Long>>(emptySet())

    val pagingFollowers = createPagingFlow()


    init {
        viewModelScope.launch {
            pagingFollowers.first()
        }
    }
/*
    val followers: Flow<PagingData<FollowersRow>> =
        followUpdates.flatMapLatest { followMap ->
            pagingFollowers.map { pagingData ->
                pagingData.map { row ->
                    if (row is FollowerUserItem) {
                        val update = followMap[row.followUserResponseDTO.id]
                        if (update != null) {
                            row.copy(
                                followUserResponseDTO = row.followUserResponseDTO.copy(
                                    followInfo = update
                                )
                            )
                        } else row
                    } else row
                }
            }
        }*/

    val followers: Flow<PagingData<FollowersRow>> =
        combine(
            pagingFollowers,
            followUpdates,
            removedUserIds
        ){pagindata, followMap, removedIds ->
            pagindata
                .filter {
                    row ->
                    if (row is FollowerUserItem){
                        !removedIds.contains(row.followUserResponseDTO.id)
                    } else true
                }
                .map {
                    row ->
                    if(row is FollowerUserItem){
                        val update = followMap[row.followUserResponseDTO.id]
                        if(update != null){
                            row.copy(
                                followUserResponseDTO = row.followUserResponseDTO.copy(
                                    followInfo = update
                                )
                            )
                        }else row
                    }else row
                }
        }


    private fun createPagingFlow(): Flow<PagingData<FollowersRow>>{
        return service.getFollowers()
            .cachedIn(viewModelScope)
    }


    fun sendFollowRequest(userId: Long){
        viewModelScope.launch {
            try {
                val response = service.followMyFollower(user.STATIC_USER_ID.toLong(), userId)
                if (response.isSuccessful){
                    response.body().let { followResponseDTO ->
                        if(followResponseDTO != null){
                            followUpdates.value = followUpdates.value.orEmpty() + (followResponseDTO.requested.userId to followResponseDTO)
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
                val response = service.cancelFollowRequest(followId)
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
                val response = service.removeFollower(userId)
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
}