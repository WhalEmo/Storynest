package com.example.storynest.Profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.Api.NetworkResult
import com.example.storynest.Block.BlockRepository
import com.example.storynest.Block.BlockStatus
import com.example.storynest.Follow.FollowRepository
import com.example.storynest.GlobalEvent.FollowEvent
import com.example.storynest.Profile.Data.ProfileData
import com.example.storynest.Profile.Data.UpdateProfileData
import com.example.storynest.Profile.MVC.ProfileRepository
import com.example.storynest.Profile.ProfileUiStates.ProfileBasicUiState
import com.example.storynest.Profile.ProfileUiStates.ProfileBlockUiState
import com.example.storynest.Profile.ProfileUiStates.ProfileUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class ProfileViewModel: ViewModel() {

    private val profileRepository = ProfileRepository
    private val followRepository = FollowRepository
    private val blockRepository = BlockRepository

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow<ProfileScreenState?>(null)
    val uiState: StateFlow<ProfileScreenState?> = _uiState.asStateFlow()




    fun init(mode: ProfileMode, userId: Long = -1L) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            profileRepository
                .loadProfile(userId, mode)
                .collectLatest { result ->
                    _uiState.value = result.ToScreenState(mode)
                }
        }
        globalFollowEvents(userId)
    }

    fun globalFollowEvents(profileUserId: Long){
        viewModelScope.launch {
            followRepository.globalFollowEvents.collect { (userId, eventCapsule) ->
                if(userId != profileUserId) return@collect
                val updateProfile = eventCapsule.data.updateProfile
                val followEvent = eventCapsule.event
                Log.d("ProfileViewModel", "globalFollowEvents: ${profileUserId}")
                when(followEvent){
                    FollowEvent.FOLLOW -> {
                        _uiState.value = ProfileScreenState.Update(
                            uiState = updateProfile.ToBasicUiState()
                        )
                    }
                    FollowEvent.UNFOLLOW -> {
                        _uiState.value = ProfileScreenState.Update(
                            uiState = updateProfile.ToBasicUiState()
                        )
                    }
                }
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

    fun blockUser(
        userId: Long
    ) {
        viewModelScope.launch {
            val response = blockRepository.block(userId)
            if(response){
                _uiState.value = ProfileScreenState.Blocked(
                    uiState = ProfileBlockUiState(
                        showUnBlockButton = true,
                        textUnBlock = "Bu hesabı engelledin"
                    )
                )
            }
        }
    }

    fun unBlockUser(
        userId: Long,
        mode: ProfileMode
    ) {
        viewModelScope.launch {
            val response = blockRepository.unBlock(userId)
            if(response){
                init(
                    mode = mode,
                    userId = userId
                )
            }
        }
    }


    private fun ProfileUiData.toUiState(type: ProfileMode): ProfileUiState {
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

    private fun UpdateProfileData.ToBasicUiState(): ProfileBasicUiState {
        return ProfileBasicUiState(
            showFollowButton = !follower && !following && !pending,
            showMessageButton = following && !pending,
            showPendingRequestButton = pending && !following,
            btnFollowYour = follower && !following && !pending,
            followersCount = this.followersCount,
            followingCount = this.followingCount
        )
    }

    private fun NetworkResult<ProfileData>.ToScreenState(profileMode: ProfileMode): ProfileScreenState{
        return when(this){
            is NetworkResult.Error ->{
                this.toScreenState()
            }
            is NetworkResult.Loading -> {
                ProfileScreenState.Loading
            }
            is NetworkResult.Success<*> -> {
                val data = this.data as ProfileResponse
                ProfileScreenState.Success(
                    uiState = data.toProfileData().toUiState(profileMode)
                )
            }
        }
    }


    private fun ProfileResponse.toProfileData(): ProfileUiData{
        return ProfileUiData(
            id = userResponseDto.id,
            username = userResponseDto.username,
            email = userResponseDto.email,
            name = userResponseDto.name,
            surname = userResponseDto.surname,
            profile = userResponseDto.profile?: "",
            biography = userResponseDto.biography,
            followers = followerCount,
            following = followedCount,
            isFollowing = following,
            isOwnProfile = ownProfile,
            isFollower = follower,
            isPending = pending
        )
    }

    private fun NetworkResult.Error.toScreenState(): ProfileScreenState {
        if(this.status == BlockStatus.YOU_BLOCKER.name){
            return ProfileScreenState.Blocked(
                uiState = ProfileBlockUiState(
                    showUnBlockButton = true,
                    textUnBlock = "Bu hesabı engelledin"
                )
            )
        }
        if (this.status == BlockStatus.TARGET_BLOCKER.name){
            return ProfileScreenState.Blocked(
                uiState = ProfileBlockUiState(
                    showUnBlockButton = false,
                    textUnBlock = "Bu hesaba erişilemiyor"
                )
            )
        }
        return ProfileScreenState.Error(
            message = this.message ?: "Unknown Error"
        )
    }
}