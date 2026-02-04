package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersRow
import com.example.storynest.Notification.FollowRequestStatus
import com.example.storynest.TestUserProvider
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MyFollowersViewModel: ViewModel() {
    private val service: MyFollowersService = MyFollowersService()

    private val _rows = MutableLiveData<List<FollowersRow>>()
    val rows: LiveData<List<FollowersRow>> = _rows

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var user = TestUserProvider

    fun getMyFollowers() {
        viewModelScope.launch {
            try {
                _rows.value = service.getMyFollowers()
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

    fun sendFollowRequest(userId: Long){
        viewModelScope.launch {
            try {
                val response = service.followMyFollower(user.STATIC_USER_ID.toLong(), userId)
                if (response.isSuccessful){
                    _rows.value = _rows.value?.map {
                        if(it is FollowersRow.FollowerUserItem &&
                            it.followUserResponseDTO.id == userId){
                            it.copy(
                                followUserResponseDTO = it.followUserResponseDTO.copy(
                                    followInfo = response.body()!!
                                )
                            )
                        }
                        else it
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
                        _rows.value = _rows.value?.map {
                            if(it is FollowersRow.FollowerUserItem &&
                                it.followUserResponseDTO.followInfo.id == responseBody.id){
                                it.copy(
                                    followUserResponseDTO = it.followUserResponseDTO.copy(
                                        followInfo = responseBody
                                    )
                                )
                            }
                            else it
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


}