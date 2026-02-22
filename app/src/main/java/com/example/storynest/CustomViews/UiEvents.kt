package com.example.storynest.CustomViews

sealed class UiEvents {
    data class ShowToast(val message: String) : UiEvents()
    data class ShowSnackbar(val message: String) : UiEvents()
    data class showInfoMessage(val message: String): UiEvents()

}