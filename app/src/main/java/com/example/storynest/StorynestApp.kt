package com.example.storynest

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.example.storynest.Posts.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers

@HiltAndroidApp
class StorynestApp: Application(){
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CRASH_LOG", "Thread: ${thread.name}", throwable)
        }
    }
}