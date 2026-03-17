package com.example.storynest


import com.example.storynest.Comments.CMController
import com.example.storynest.HomePage.HPController
import com.example.storynest.RegisterLogin.RLController
import com.example.storynest.dataLocal.UserPreferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    var currentToken: String? = null
    fun updateToken(token: String?) {
        currentToken = token
    }
    fun clearToken() {
        currentToken = null
    }

    suspend fun clearAllUserData(userPrefs: UserPreferences) {
        clearToken()
        userPrefs.clearUser()
    }
}
