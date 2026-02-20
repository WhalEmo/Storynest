package com.example.storynest.Profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.Profile.MVC.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException


class ProfileViewModel: ViewModel() {

    private val profileRepository = ProfileRepository
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val profileCache = mutableMapOf<Long, ProfileUiState>()

    private val _uiState = MutableStateFlow<ProfileScreenState?>(null)
    val uiState: StateFlow<ProfileScreenState?> = _uiState.asStateFlow()


    init {
        Log.d("VM_DEBUG", "MyViewModel oluşturuldu -> ${this.hashCode()}")
    }


    fun init(mode: ProfileMode, userId: Long = -1L) {
        when(mode){
            ProfileMode.USER_PROFILE -> loadUserProfile(userId)

            ProfileMode.MY_PROFILE -> loadMyProfile(userId)
        }
    }

    private fun loadMyProfile(userId: Long) {
        viewModelScope.launch {
            _uiState.value = ProfileScreenState.Loading
            try {
                profileRepository.loadMyProfile(userId)
                    .collect {
                        _uiState.value = ProfileScreenState.Success(
                            uiState = it.toUiState(
                                type = ProfileMode.MY_PROFILE
                            )
                        )
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
    private fun loadUserProfile(userId: Long) {
        viewModelScope.launch {
            _uiState.value = ProfileScreenState.Loading
            try {
                profileRepository.loadUserProfile(userId)
                    .collect {
                        _uiState.value = ProfileScreenState.Success(
                            uiState = it.toUiState(
                                type = ProfileMode.USER_PROFILE
                            )
                        )
                    }
            } catch (e: HttpException) {
                Log.e("ProfileViewModel", "Error loading user profile", e)
                _error.value = "Sunucuya bağlanılamadı (HTTP ${e.code()})"
            } catch (e: IOException) {
                Log.e("ProfileViewModel", "Error loading user profile", e)
                _error.value = "İnternet bağlantısı yok!"
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading user profile", e)
                _error.value = "Beklenmeyen bir hata oluştu"
            }
        }
    }

    private fun ProfileData.toUiState(type: ProfileMode): ProfileUiState {
        Log.d("Model", type.name)
        return ProfileUiState(
            id = id,
            username = username,
            name = name,
            surname = surname,
            biography = biography,
            profileImageUrl = profile,
            followers = followers,
            following = following,
            isFollowing = false,

            showEditButton = type == ProfileMode.MY_PROFILE,
            showFollowButton = type == ProfileMode.USER_PROFILE && !isFollowing,
            showSettingsButton = type == ProfileMode.MY_PROFILE,
            showDotMenuButton = type == ProfileMode.USER_PROFILE,
            showNotificationButton = type == ProfileMode.MY_PROFILE,
            isOwnProfile = type == ProfileMode.MY_PROFILE,
            showMessageButton = type == ProfileMode.USER_PROFILE && !isOwnProfile && isFollowing
        )

    }

}