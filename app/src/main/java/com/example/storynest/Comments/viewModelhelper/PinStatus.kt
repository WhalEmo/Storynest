package com.example.storynest.Comments.viewModelhelper

sealed class PinStatus {
    object Loading: PinStatus()
    object Success: PinStatus()
    object Error: PinStatus()
}