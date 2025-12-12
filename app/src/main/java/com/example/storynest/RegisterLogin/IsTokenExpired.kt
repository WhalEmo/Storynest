package com.example.storynest.RegisterLogin
import android.util.Base64
import org.json.JSONObject

class IsTokenExpired {
    fun isTokenExpired(token: String?): Boolean {
        try {
            val parts = token?.split(".")
            if (parts?.size != 3) return true

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)

            val exp = json.optLong("exp", 0L)
            val now = System.currentTimeMillis() / 1000

            return now >= exp
        } catch (e: Exception) {
            return true
        }
    }

}