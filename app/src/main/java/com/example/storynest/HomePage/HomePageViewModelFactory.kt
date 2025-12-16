package com.example.storynest.HomePage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storynest.dataLocal.UserPreferences

class HomePageViewModelFactory(
    private val repo: HomePageRepo
):  ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomePageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomePageViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}