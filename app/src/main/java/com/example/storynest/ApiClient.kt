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
    private val retrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()

                //headera ekledik
                currentToken?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Farklı servisler
    val api: RLController by lazy {retrofit.create(RLController::class.java)}
    val postApi: HPController by lazy { retrofit.create(HPController::class.java) }
    //val userApi: UserService by lazy { retrofit.create(UserService::class.java) }
   val commentApi: CMController by lazy { retrofit.create(CMController::class.java) }

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
