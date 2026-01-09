package com.example.storynest.RegisterLogin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LaunchActivity : AppCompatActivity() {
    private lateinit var userPrefs: UserPreferences
    private lateinit var isTokenExpired: IsTokenExpired
    private lateinit var intentOther: Intent

    private lateinit var context: Context



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        context = this

        intentOther = Intent(this@LaunchActivity, MainActivity::class.java)

        userPrefs = UserPreferences.getInstance(this)
        isTokenExpired = IsTokenExpired()
        lifecycleScope.launch {
            val tokenStart = userPrefs.token.firstOrNull()
            if ((tokenStart.isNullOrEmpty() || isTokenExpired.isTokenExpired(tokenStart))) {
                handleIntent(intent)
            }
            else{
                val uri = intent?.data
                val token = uri?.getQueryParameter("token")
                val resetPasswordToken = uri?.getQueryParameter("resetpasswordtoken")

                if (!token.isNullOrEmpty()) {
                    Toast.makeText(context, "Email zaten doğrulanmış!", Toast.LENGTH_SHORT).show()
                }
                if(!resetPasswordToken.isNullOrEmpty()){
                    Toast.makeText(context, "Link zaten gönderildi!", Toast.LENGTH_SHORT).show()
                }
                goNormalFlow()

            }
        }

    }
    private var handled = false

    private fun handleIntent(intent: Intent?) {
        println("handleIntent")
        if (handled) return
        handled = true

        val uri = intent?.data
        val token = uri?.getQueryParameter("token")
        //DEVAMI VAR RESETPASSWORD yapılacak

        if (!token.isNullOrEmpty()) {
            println("token")
            verifyEmail(token)
        } else {
            goNormalFlow()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println("onNewIntent")
        setIntent(intent)
        handleIntent(intent)
    }

    private fun goNormalFlow(){
        println("goNormalFlow")
        lifecycleScope.launch {
            val token = userPrefs.token.firstOrNull()
            if (token.isNullOrEmpty() || isTokenExpired.isTokenExpired(token)) {
                withContext(Dispatchers.IO) {
                    ApiClient.clearAllUserData(userPrefs)
                }
                intentOther.putExtra("showLogin", true)
                startActivity(intentOther)
            } else {
                UserStaticClass.userId=userPrefs.id.firstOrNull()
                UserStaticClass.name=userPrefs.name.firstOrNull()
                UserStaticClass.username=userPrefs.username.firstOrNull()
                UserStaticClass.surname=userPrefs.surname.firstOrNull()
                UserStaticClass.email=userPrefs.email.firstOrNull()
                ApiClient.updateToken(token)
                startActivity(Intent(this@LaunchActivity, MainActivity::class.java))
            }
            finish()
        }
    }
    private fun dontNormalFlow(name:String){
        println("dontNormalFlow"+" "+name)
        intentOther.putExtra(name, true)
        startActivity(intentOther)
        finish()
    }


    private fun verifyEmail(token: String) {
        Log.d("VERIFY", "verifyEmail çağrıldı")

        ApiClient.api.verify(token).enqueue(object : Callback<VerifyResponse> {

            override fun onResponse(
                call: Call<VerifyResponse>,
                response: Response<VerifyResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("VERIFY", response.body()?.message ?: "success")
                    dontNormalFlow("login")
                } else {
                    Log.e("VERIFY", "response error: ${response.code()}")
                    dontNormalFlow("register")
                }
            }

            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                Log.e("VERIFY", "network error", t)
            }
        })
    }


}