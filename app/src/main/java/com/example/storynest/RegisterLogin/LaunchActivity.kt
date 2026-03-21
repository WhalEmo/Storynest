package com.example.storynest.RegisterLogin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.storynest.ApiClient
import com.example.storynest.MainActivity
import com.example.storynest.Posts.AppDatabase
import com.example.storynest.R
import com.example.storynest.dataLocal.UserPreferences
import com.example.storynest.dataLocal.UserStaticClass
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class LaunchActivity : AppCompatActivity() {

  //  @Inject lateinit var db: AppDatabase
    @Inject lateinit var rlApi: RLController

    private lateinit var userPrefs: UserPreferences
    private val isTokenExpired = IsTokenExpired()
    private var isIntentHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launch)

        setupWindowInsets()
        userPrefs = UserPreferences.getInstance(this)

        // TÜM BAŞLANGIÇ SÜRECİ TEK BİR COROUTINE İÇİNDE
        lifecycleScope.launch {
            try {
               /*
                withContext(Dispatchers.IO) {
                    db.remoteKeysDao().clearRemoteKeys()
                    db.postDao().clearAll()
                }

                */

                val currentToken = withContext(Dispatchers.IO) { userPrefs.token.firstOrNull() }

                if (currentToken.isNullOrEmpty() || isTokenExpired.isTokenExpired(currentToken)) {
                    handleInitialIntent(intent)
                } else {
                    checkDeepLinkForLoggedInUser(intent)
                    proceedToMainFlow(currentToken)
                }
            } catch (e: Exception) {
                Log.e("LaunchActivity", "Kritik hata: ${e.message}")
                navigateToMain("showLogin") // Hata durumunda en güvenli liman login ekranı
            }
        }
    }

    private fun checkDeepLinkForLoggedInUser(intent: Intent?) {
        val token = intent?.data?.getQueryParameter("token")
        if (!token.isNullOrEmpty()) {
            Toast.makeText(this, "Zaten oturum açılmış!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleInitialIntent(intent: Intent?) {
        if (isIntentHandled) return
        isIntentHandled = true

        val uri = intent?.data
        val token = uri?.getQueryParameter("token")
        val path = uri?.path

        if (token.isNullOrEmpty()) {
            navigateToMain("showLogin")
            return
        }

        when {
            path?.startsWith("/auth/verify") == true -> verifyEmailAction(token)
            path?.startsWith("/auth/reset-password") == true -> verifyPasswordResetAction(token)
            else -> navigateToMain("showLogin")
        }
    }

    private suspend fun proceedToMainFlow(token: String) {
        withContext(Dispatchers.IO) {
            UserStaticClass.userId = userPrefs.id.firstOrNull()
            UserStaticClass.name = userPrefs.name.firstOrNull()
            UserStaticClass.username = userPrefs.username.firstOrNull()
            UserStaticClass.surname = userPrefs.surname.firstOrNull()
            UserStaticClass.email = userPrefs.email.firstOrNull()
            UserStaticClass.ppfoto = userPrefs.profilePhoto.firstOrNull()

            ApiClient.updateToken(token)
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToMain(actionKey: String, token: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(actionKey, true)
            token?.let { putExtra("TOKEN_KEY", it) }
        }
        startActivity(intent)
        finish()
    }

    private fun verifyEmailAction(token: String) {
        lifecycleScope.launch {
            try {
                rlApi.verify(token)
                navigateToMain("login")
            } catch (e: Exception) {
                showErrorToast("Doğrulama başarısız veya internet yok.")
                navigateToMain("showLogin")
            }
        }
    }

    private fun verifyPasswordResetAction(token: String) {
        lifecycleScope.launch {
            try {
                rlApi.verifyResetPassword(token)
                navigateToMain("forgotpassword", token)
            } catch (e: Exception) {
                showErrorToast("Link geçersiz veya süresi dolmuş.")
                navigateToMain("login")
            }
        }
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleInitialIntent(intent)
    }
}