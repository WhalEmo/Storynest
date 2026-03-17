package com.example.storynest

sealed class AppError:Exception() {
    data class NetworkError(val msg: String = "İnternet bağlantısı yok") : AppError()
    data class ServerError(val code: Int, val msg: String) : AppError()
    data class NgrokError(val msg: String = "Sunucu tüneli (Ngrok) kapalı") : AppError()
    data class UnknownError(val msg: String = "Beklenmedik bir hata oluştu") : AppError()
}