package com.example.storynest.RegisterLogin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storynest.ApiClient
import com.example.storynest.ResultWrapper
import com.example.storynest.dataLocal.UserPreferences
import kotlinx.coroutines.launch
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterLoginViewModel( private val userPrefs: UserPreferences) : ViewModel() {
    val loginResult = MutableLiveData<ResultWrapper<LoginResponse>>()
    val registerResult= MutableLiveData<ResultWrapper<UserResponse>>()

    fun login(username: String, password: String) {
        val request = loginRequest(username, password)

        ApiClient.api.login(request).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        viewModelScope.launch {
                            userPrefs.saveUser(
                                username = body.user.username,
                                token = body.token,
                                name = body.user.name,
                                surname = body.user.surname,
                                id = body.user.id,
                                email = body.user.email
                            )
                        }
                        ApiClient.updateToken(body.token)
                        loginResult.value = ResultWrapper.Success(body)
                    } else {
                        loginResult.value = ResultWrapper.Error("Hata Sunucu boş yanıt döndürdü")
                    }
                } else {
                    val errorString = response.errorBody()?.string() ?: ""
                    val errorMessage = when {
                        errorString.contains("Bad credentials", ignoreCase = true) -> "Kullanıcı adı veya şifre yanlış"
                        else -> parseErrorBody(errorString)
                    }
                    loginResult.value = ResultWrapper.Error(errorMessage)
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginResult.value = ResultWrapper.Error("Sunucuya bağlanılamadı: ${t.message}")
            }
        })
    }
    fun register( username: String,  email: String, password: String, name: String, surname: String, profile: String?, biography: String?){
        val request= registerRequest(username,email,password,name,surname,profile,biography)

        ApiClient.api.register(request).enqueue(object : Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if(response.isSuccessful){
                    val body = response.body()
                    if (body != null) {
                        registerResult.value = ResultWrapper.Success(body)
                        login(username, password)
                    } else {
                        registerResult.value = ResultWrapper.Error("Hata Sunucu boş yanıt döndürdü")
                    }
                }else{
                    val errorMessage = parseErrorBody(response.errorBody()?.string())
                    registerResult.value = ResultWrapper.Error(errorMessage)
                }
            }
            override fun onFailure(
                call: Call<UserResponse?>,
                t: Throwable
            ) {
                registerResult.value= ResultWrapper.Error("Sunucuya bağlanılamadı: ${t.message}")
            }

        })

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


}