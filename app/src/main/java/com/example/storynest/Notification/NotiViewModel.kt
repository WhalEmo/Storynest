package com.example.storynest.Notification

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
    private val _pending = MutableLiveData<List<FollowResponseDTO>>()
    val pending: LiveData<List<FollowResponseDTO>> = _pending

    private val _rows = MutableLiveData<List<NotificationRow>>()
    val rows: LiveData<List<NotificationRow>> = _rows

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getMyFollowPending() {
        viewModelScope.launch {
            try {
                val requset = notificationService.getFollowRequests()
                _pending.postValue(requset)
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

    fun buildRows(pendingList: List<NotificationRow>) {
        _rows.value = pendingList
    }

    fun accept(notificationId: Long) {
        /*_rows.value = _rows.value?.map {
            if (
                it is NotificationRow.NotificationItem &&
                it.notification.id == notificationId
            ) {
                it.copy(isAccepted = true)
            } else it
        }*/
        for (item in _rows.value!!) {
            if (item is NotificationRow.NotificationItem) {
                if (item.notification.id == notificationId) {
                    item.isAccepted = true
                }
            }
        }
    }

}