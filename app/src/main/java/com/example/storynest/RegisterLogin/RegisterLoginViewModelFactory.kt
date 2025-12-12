package com.example.storynest.RegisterLogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storynest.dataLocal.UserPreferences

class RegisterLoginViewModelFactory(
    private val userPrefs: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterLoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterLoginViewModel(userPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
//BU SINIF SİLİNCEEK DİGER SINIFLARA BAKAMK İİCN KOYULUD
