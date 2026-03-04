package com.example.storynest.Comments.viewModelhelper

sealed class PinStatus {
    data class Loading(val parentCommentId:Long): PinStatus()
    data class Succes(val parentCommentId:Long): PinStatus()
    object Error: PinStatus()
}