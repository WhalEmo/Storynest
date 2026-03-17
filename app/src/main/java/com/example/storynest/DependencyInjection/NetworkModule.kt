package com.example.storynest.DependencyInjection

import com.example.storynest.ApiClient
import com.example.storynest.Block.BlockApiController
import com.example.storynest.BuildConfig
import com.example.storynest.Comments.CMController
import com.example.storynest.Follow.FollowApiController
import com.example.storynest.HomePage.HPController
import com.example.storynest.Notification.NotificationApiController
import com.example.storynest.Profile.MVC.ProfileApiController
import com.example.storynest.RegisterLogin.RLController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()

                ApiClient.currentToken?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()

    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHPController(retrofit: Retrofit): HPController {
        return retrofit.create(HPController::class.java)
    }

    @Provides
    @Singleton
    fun provideRLController(retrofit: Retrofit): RLController {
        return retrofit.create(RLController::class.java)
    }

    @Provides
    @Singleton
    fun provideCMController(retrofit: Retrofit): CMController {
        return retrofit.create(CMController::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileController(retrofit: Retrofit): ProfileApiController {
        return retrofit.create(ProfileApiController::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationController(retrofit: Retrofit): NotificationApiController {
        return retrofit.create(NotificationApiController::class.java)
    }

    @Provides
    @Singleton
    fun provideFollowController(retrofit: Retrofit): FollowApiController {
        return retrofit.create(FollowApiController::class.java)
    }

    @Provides
    @Singleton
    fun provideBlockController(retrofit: Retrofit): BlockApiController {
        return retrofit.create(BlockApiController::class.java)
    }
}
