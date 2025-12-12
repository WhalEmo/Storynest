package com.example.storynest.RegisterLogin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.storynest.ApiClient
import com.example.storynest.MainActivity
import com.example.storynest.R
import com.example.storynest.dataLocal.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LaunchActivity : AppCompatActivity() {
    private lateinit var userPrefs: UserPreferences
    private lateinit var isTokenExpired: IsTokenExpired

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        userPrefs = UserPreferences.getInstance(this)
        isTokenExpired = IsTokenExpired()

        lifecycleScope.launch {
            val token = userPrefs.token.firstOrNull()
            if (token.isNullOrEmpty() && isTokenExpired.isTokenExpired(token)) {
                withContext(Dispatchers.IO) {
                    ApiClient.clearAllUserData(userPrefs)
                }
                val intent = Intent(this@LaunchActivity, MainActivity::class.java)
                intent.putExtra("showLogin", true)
                startActivity(intent)
            } else {
                ApiClient.updateToken(token)
                startActivity(Intent(this@LaunchActivity, MainActivity::class.java))
            }
            finish()
        }
    }
}