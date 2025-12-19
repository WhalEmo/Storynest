package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersRow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MyFollowersViewModel: ViewModel() {
    private val service: MyFollowersService = MyFollowersService()

    private val _rows = MutableLiveData<List<FollowersRow>>()
    val rows: LiveData<List<FollowersRow>> = _rows

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error


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

}