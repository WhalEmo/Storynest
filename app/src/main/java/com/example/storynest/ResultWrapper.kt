package com.example.storynest

import org.json.JSONArray

sealed class ResultWrapper<out T> {

    data class Success<T>(val data: T) : ResultWrapper<T>()

    data class Error(
        val message: String,
        val type: ErrorType
    ) : ResultWrapper<Nothing>()
}

enum class ErrorType {
    EMAIL_NOT_VERIFIED,
    WRONG_REGISTER,
    SERVER_ERROR,
    NETWORK_ERROR,
    EMPTY_RESPONSE
}
fun parseErrorBody(errorBody: String?): String {
    if (errorBody.isNullOrEmpty()) return "Bilinmeyen hata"
    return try {
        if (errorBody.startsWith("[")) {
            val jsonArray = JSONArray(errorBody)
            val messages = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                messages.add(jsonArray.getString(i))
            }
            messages.joinToString("\n")
        } else {
            errorBody
        }
    } catch (e: Exception) {
        errorBody
    }
}
