package com.example.storynest.Api

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val status: String?,
    val timestamp: String?
)