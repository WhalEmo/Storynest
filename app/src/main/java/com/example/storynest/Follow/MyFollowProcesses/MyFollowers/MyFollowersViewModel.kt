package com.example.storynest.Follow.MyFollowProcesses.MyFollowers


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersRow
import com.example.storynest.TestUserProvider
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersRow.FollowerUserItem
import com.example.storynest.Notification.FollowRequestStatus
import com.example.storynest.Notification.FollowResponseDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
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


    val followers = pagingGetFollowers()


    private fun pagingGetFollowers(): Flow<PagingData<FollowersRow>>{
        return service.getFollowers()
            .combine(followUpdates){ pagingData, followMap ->
                pagingData.map { row ->
                    if(row is FollowerUserItem){
                        val update =
                            followUpdates.value?.get(row.followUserResponseDTO.followInfo.id)
                        if(update != null){
                            row.copy(followUserResponseDTO =
                                row.followUserResponseDTO.copy(
                                    followInfo = update,
                                    followingYou = update.status == FollowRequestStatus.ACCEPTED
                                            || update.status == FollowRequestStatus.PENDING
                                )
                            )
                        } else row
                    } else row
                }
            }
            .cachedIn(viewModelScope)
    }


    fun sendFollowRequest(userId: Long){
        viewModelScope.launch {
            try {
                val response = service.followMyFollower(user.STATIC_USER_ID.toLong(), userId)
                if (response.isSuccessful){
                    response.body().let { followResponseDTO ->
                        if(followResponseDTO != null){
                            followUpdates.value = followUpdates.value.orEmpty() + (followResponseDTO.id to followResponseDTO)
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
            val response = service.cancelFollowRequest(followId)
            try {
                if (response.isSuccessful){
                    val responseBody = response.body()
                    if(responseBody?.status == FollowRequestStatus.CANCEL){
                        followUpdates.value = followUpdates.value.orEmpty() + (followId to responseBody)
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

}