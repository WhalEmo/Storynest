package com.example.storynest.Profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.Follow.FollowRepository
import com.example.storynest.Follow.ResponseDTO.FollowResponse
import com.example.storynest.GlobalEvent.FollowEvent
import com.example.storynest.Profile.MVC.ProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException


class ProfileViewModel: ViewModel() {

    private val profileRepository = ProfileRepository
    private val followRepository = FollowRepository
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow<ProfileScreenState?>(null)
    val uiState: StateFlow<ProfileScreenState?> = _uiState.asStateFlow()

    private lateinit var profileData: ProfileData



    fun init(mode: ProfileMode, userId: Long = -1L) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = ProfileScreenState.Loading

            try {
                val flow = profileRepository.loadProfile(
                    userId = userId,
                    profileMode = mode
                )

                flow.collect { profile ->
                    profileData = profile
                    _uiState.value = ProfileScreenState.Success(
                        uiState = profile.toUiState(type = mode)
                    )
                }

            } catch (e: HttpException) {
                _error.value = "Sunucuya bağlanılamadı (HTTP ${e.code()})"
            } catch (e: IOException) {
                _error.value = "İnternet bağlantısı yok!"
            } catch (e: Exception) {
                _error.value = "Beklenmeyen bir hata oluştu"
            }
            globalFollowEvents(userId)
        }
    }

    fun globalFollowEvents(profileUserId: Long){
        viewModelScope.launch {
            followRepository.globalFollowEvents.collect { (userId, eventCapsule) ->
                if(userId != profileUserId) return@collect
                val followResponse = eventCapsule.data
                val followEvent = eventCapsule.event
                when(followEvent){
                    FollowEvent.FOLLOW -> {
                        _uiState.value = ProfileScreenState.Update(
                            uiState = followResponse.ToBasicUiState(profileData.followers + 1)
                        )
                    }
                    FollowEvent.UNFOLLOW -> {
                        _uiState.value = ProfileScreenState.Update(
                            uiState = followResponse.ToBasicUiState(profileData.followers - 1)
                        )
                    }
                }
                profileData = profileRepository.updateUserProfile(userId, followResponse) ?: profileData
            }
        }
    }



    fun followUser(
        userId: Long,
        profileMode: ProfileMode
    ){
        viewModelScope.launch {
            followRepository.follow(userId)
        }
    }

    fun unFollowUser(
        userId: Long
    ){
        viewModelScope.launch {
            followRepository.unfollow(userId)
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
            isFollowing = isFollowing,
            isFollower = isFollower,

            showEditButton = type == ProfileMode.MY_PROFILE,
            showFollowButton = type == ProfileMode.USER_PROFILE && !isFollower && !isFollowing && !isPending,
            showSettingsButton = type == ProfileMode.MY_PROFILE,
            showDotMenuButton = type == ProfileMode.USER_PROFILE,
            showNotificationButton = type == ProfileMode.MY_PROFILE,
            isOwnProfile = type == ProfileMode.MY_PROFILE,

            showMessageButton = type == ProfileMode.USER_PROFILE && !isOwnProfile && isFollowing,

            btnFollowYour = type == ProfileMode.USER_PROFILE && isFollower && !isFollowing && !isPending,
            btnShareProfile = true,
            showPendingRequestButton = type == ProfileMode.USER_PROFILE && isPending && !isFollowing
        )

    }

    private fun FollowResponse.ToBasicUiState(count: Int): ProfileBasicUiState{
        return ProfileBasicUiState(
            showFollowButton = !follower && !following && !pending,
            showMessageButton = following && !pending,
            showPendingRequestButton = pending && !following,
            btnFollowYour = follower && !following && !pending,
            followCount = count
        )
    }

}