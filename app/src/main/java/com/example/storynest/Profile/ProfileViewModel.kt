package com.example.storynest.Profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.Profile.MVC.ProfileRepository
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException


class ProfileViewModel: ViewModel() {

    private val profileRepository = ProfileRepository()
    private val _profile = MutableLiveData<MyProfile>()
    val profile: LiveData<MyProfile> = _profile
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error


    fun getMyProfile() {
        viewModelScope.launch {
            try {
                val profile = profileRepository.loadMyProfile()
                _profile.postValue(profile)
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