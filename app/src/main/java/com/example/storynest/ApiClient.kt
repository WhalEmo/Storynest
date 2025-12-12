package com.example.storynest

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {


    fun getClient(token: String): Retrofit {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL+"/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}