package com.example.storynest.Notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.Notification.Adapter.NotificationRow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class NotiViewModel: ViewModel() {
    private val notificationService = NotificationService()

    private val _rows = MutableLiveData<List<NotificationRow>>()
    val rows: LiveData<List<NotificationRow>> = _rows

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMyFollowPending() {
        viewModelScope.launch {
            try {
                val requset = notificationService.getFollowRequests()
                _rows.value = requset
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

    fun markAsRead(notificationId: Long) {
        _rows.value = _rows.value?.map {
            if (it is NotificationRow.NotificationItem &&
                it.notification.id == notificationId
            ) {
                it.copy(
                    isUnread = true
                )
            } else it
        }
    }

    fun accept(notificationId: Long) {
        viewModelScope.launch {
            try {
                val response = notificationService.acceptFollow(notificationId)
                if (response.isSuccessful) {
                    _rows.value = _rows.value?.map {
                        if (it is NotificationRow.NotificationItem &&
                            it.notification.id == notificationId
                        ) {
                            it.copy(
                                isAccepted = true
                            )
                        } else it
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

    fun reject(notificationId: Long) {
        viewModelScope.launch {
            try {
                val response = notificationService.rejectFollow(notificationId)
                if (response.isSuccessful) {
                    _rows.value = _rows.value?.map {
                        if (it is NotificationRow.NotificationItem &&
                            it.notification.id == notificationId
                        ) {
                            it.copy(
                                isRejected = true
                            )
                        } else it
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