package com.example.storynest.RegisterLogin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.*
import com.example.storynest.dataLocal.UserPreferences
import kotlinx.coroutines.launch

class RegisterLoginViewModel(
    private val userPrefs: UserPreferences,
    private val repository: RegisterLoginRepo
) : ViewModel() {

    // Her işlem için ayrı UiState
    private val _loginState = MutableLiveData<UiState<LoginResponse>>()
    val loginState: LiveData<UiState<LoginResponse>> = _loginState

    private val _registerState = MutableLiveData<UiState<UserResponse>>()
    val registerState: LiveData<UiState<UserResponse>> = _registerState

    private val _resetPasswordState = MutableLiveData<UiState<String>>()
    val resetPasswordState: LiveData<UiState<String>> = _resetPasswordState

    private val _newPasswordState = MutableLiveData<UiState<String>>()
    val newPasswordState: LiveData<UiState<String>> = _newPasswordState


    fun login(username: String, password: String) {
        val request = loginRequest(username, password)
        _loginState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.login(request)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    userPrefs.saveUser(
                        username = body.user.username,
                        token = body.token,
                        name = body.user.name,
                        surname = body.user.surname,
                        id = body.user.id,
                        email = body.user.email
                    )
                    ApiClient.updateToken(body.token)
                    _loginState.value = UiState.Success(body)
                }
                is ResultWrapper.Error -> _loginState.value = UiState.Error(result.message)
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        name: String,
        surname: String,
        profile: String? = null,
        biography: String? = null
    ) {
        val request = registerRequest(username, email, password, name, surname, profile, biography)
        _registerState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.register(request)
            when (result) {
                is ResultWrapper.Success -> {
                    val body = result.data
                    if (!body.emailVerified) {
                        _registerState.value = UiState.EmailNotVerified("Emailinizi doğrulayın.")
                    } else {
                        _registerState.value = UiState.Success(body)
                    }
                }
                is ResultWrapper.Error -> _registerState.value = UiState.Error(result.message)
            }
        }
    }

    fun resetPassword(email: String) {
        _resetPasswordState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.resetPassword(email)
            when (result) {
                is ResultWrapper.Success -> _resetPasswordState.value = UiState.EmailSent(result.data)
                is ResultWrapper.Error -> _resetPasswordState.value = UiState.Error(result.message)
            }
        }
    }


    fun newPassword(token: String, password: String, cpassword: String) {
        val request = ResetPasswordRequest(password, cpassword)
        _newPasswordState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.savePassword(token, request)
            when (result) {
                is ResultWrapper.Success -> _newPasswordState.value = UiState.Success(result.data)
                is ResultWrapper.Error -> _newPasswordState.value = UiState.Error(result.message)
            }
        }
    }
}
