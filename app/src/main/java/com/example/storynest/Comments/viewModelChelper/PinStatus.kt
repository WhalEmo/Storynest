package com.example.storynest.Comments.viewModelChelper

sealed class PinStatus {
    object Loading: PinStatus()
    object Success: PinStatus()
    object Error: PinStatus()
}