package com.example.storynest.RegisterLogin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.ApiClient
import com.example.storynest.ErrorType
import com.example.storynest.ResultWrapper
import com.example.storynest.dataLocal.UserPreferences
import com.example.storynest.parseErrorBody
import kotlinx.coroutines.launch

class RegisterLoginViewModel(
    private val userPrefs: UserPreferences,
    private val repository: RegisterLoginRepo
) : ViewModel() {

    val loginResult = MutableLiveData<ResultWrapper<LoginResponse>>()
    val registerResult = MutableLiveData<ResultWrapper<UserResponse>>()

    fun login(username: String, password: String) {
        val request = loginRequest(username, password)

        viewModelScope.launch {
            try {
                val body = repository.login(request)

                userPrefs.saveUser(
                    username = body.user.username,
                    token = body.token,
                    name = body.user.name,
                    surname = body.user.surname,
                    id = body.user.id,
                    email = body.user.email
                )

                ApiClient.updateToken(body.token)

                loginResult.value = ResultWrapper.Success(body)

            } catch (e: Exception) {
                loginResult.value = ResultWrapper.Error(
                    message = parseErrorBody(e.message),
                    type = ErrorType.WRONG_REGISTER
                )
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        name: String,
        surname: String,
        profile: String?,
        biography: String?
    ) {
        val request = registerRequest(
            username, email, password, name, surname, profile, biography
        )

        viewModelScope.launch {
            try {
                val body = repository.register(request)

                if (!body.emailVerified) {
                    registerResult.value = ResultWrapper.Error(
                        message = "Emailinizi doğrulayın.",
                        type = ErrorType.EMAIL_NOT_VERIFIED
                    )
                } else {
                    registerResult.value = ResultWrapper.Success(body)
                }

            } catch (e: Exception) {
                registerResult.value = ResultWrapper.Error(
                    message = parseErrorBody(e.message),
                    type = ErrorType.SERVER_ERROR
                )
            }
        }
    }
}

