package com.example.storynest.RegisterLogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.example.storynest.ApiClient
import com.example.storynest.R
import com.example.storynest.ResultWrapper
import com.example.storynest.UiState
import com.example.storynest.dataLocal.UserPreferences

class RegisterLoginFragmnet : Fragment() {
    private val userPrefs by lazy { UserPreferences.getInstance(requireContext()) }
    private val registerLoginRepo by lazy { RegisterLoginRepo(ApiClient.api) }

    private val viewModel: RegisterLoginViewModel by viewModels {
       RegisterLoginViewModelFactory(userPrefs,registerLoginRepo)
   }
    //private val viewModel: RegisterLoginViewModel by viewModels()
    private lateinit var lamp: ImageView
    private lateinit var layoutButtons: LinearLayout
    private lateinit var buttonRegister: Button
    private lateinit var buttonLogin: Button
    private lateinit var loginFields: LinearLayout
    private lateinit var edtLoginUsername: EditText
    private lateinit var edtLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var registerFields: LinearLayout
    private lateinit var edtUserName: EditText
    private lateinit var name: EditText
    private lateinit var surname: EditText
    private lateinit var edtRegEmail: EditText
    private lateinit var edtRegPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var lightGlow: View
    private lateinit var forgotPassword: TextView

    private lateinit var forgot: LinearLayout
    private lateinit var edtforgotemail: EditText
    private lateinit var btnSend: Button

    private lateinit var newpassword: LinearLayout
    private lateinit var newpsswrd: EditText
    private lateinit var confirmpsswrd: EditText
    private lateinit var btnnewsend: Button
    private lateinit var generalProgressBar: ProgressBar
    private var passwrdtoken: String? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_register_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val login = arguments?.getBoolean("login", false)
        val register = arguments?.getBoolean("register", false)
        val mainforgotpassword = arguments?.getBoolean("forgotpassword", false)
        passwrdtoken= arguments?.getString("TOKEN_KEY")

        lamp = view.findViewById(R.id.lambaSonuk)
        layoutButtons = view.findViewById(R.id.initialButtons)
        buttonRegister = view.findViewById(R.id.btnShowRegister)
        buttonLogin = view.findViewById(R.id.btnShowLogin)

        loginFields = view.findViewById(R.id.loginFields)
        edtLoginUsername=view.findViewById(R.id.edtLoginUsername)
        edtLoginPassword=view.findViewById(R.id.edtLoginPassword)
        btnLogin=view.findViewById(R.id.btnLogin)
        forgotPassword = view.findViewById(R.id.forgotPassword)


        registerFields = view.findViewById(R.id.registerFields)
        edtUserName=view.findViewById(R.id.edtUserName)
        name=view.findViewById(R.id.name)
        surname=view.findViewById(R.id.surname)
        edtRegEmail=view.findViewById(R.id.edtRegEmail)
        edtRegPassword=view.findViewById(R.id.edtRegPassword)
        btnRegister=view.findViewById(R.id.btnRegister)
        lightGlow = view.findViewById(R.id.lightGlow)


        forgot=view.findViewById(R.id.forgot)
        edtforgotemail=view.findViewById(R.id.edtforgotemail)
        btnSend=view.findViewById(R.id.btnSend)

        newpassword=view.findViewById(R.id.newpassword)
        newpsswrd=view.findViewById(R.id.newpsswrd)
        confirmpsswrd=view.findViewById(R.id.confirmpsswrd)
        btnnewsend=view.findViewById(R.id.btnnewsend)

        generalProgressBar=view.findViewById(R.id.generalProgressBar)


        setupButtonListeners()
        setupBackPressedCallback()
        clicks()
        setupObservers()

        if(login==true){
            animateViewGone(layoutButtons) {
                loginFields.visibility = View.VISIBLE
                forgot.visibility= View.GONE
                registerFields.visibility=View.GONE
            }
        }
        else if(register==true){
            animateViewGone(layoutButtons) {
            registerFields.visibility=View.VISIBLE
            loginFields.visibility = View.GONE
            }
        }else if(mainforgotpassword==true){
            animateViewGone(layoutButtons) {
                loginFields.visibility = View.GONE
                forgot.visibility= View.GONE
                registerFields.visibility=View.GONE
                newpassword.visibility=View.VISIBLE
            }
        }
        else{
            startLampAnimation()
        }

    }
    private fun clicks(){
        btnLogin.setOnClickListener {
            val username=edtLoginUsername.text.toString().trim()
            val password=edtLoginPassword.text.toString().trim()

            if (username.isEmpty()) {
                edtLoginUsername.error = "Kullanıcı adı boş olamaz"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                edtLoginPassword.error = "Şifre boş olamaz"
                return@setOnClickListener
            }
            if (password.length < 6) {
                edtLoginPassword.error = "Şifre en az 6 karakter olmalı"
                return@setOnClickListener
            }
            edtLoginUsername.error = null
            edtLoginPassword.error = null
            viewModel.login(username,password)
        }

        btnRegister.setOnClickListener {
            val username=edtUserName.text.toString().trim()
            val namee=name.text.toString().trim()
            val surnamee=surname.text.toString().trim()
            val email=edtRegEmail.text.toString().trim()
            val password=edtRegPassword.text.toString().trim()

            if (username.isEmpty()) {
                edtUserName.error = "Kullanıcı adı boş olamaz"
                return@setOnClickListener
            }

            if (namee.isEmpty()) {
                name.error = "Ad boş olamaz"
                return@setOnClickListener
            }

            if (surnamee.isEmpty()) {
                surname.error = "Soyad boş olamaz"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                edtRegEmail.error = "Email boş olamaz"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                edtRegPassword.error = "Şifre boş olamaz"
                return@setOnClickListener
            } else if (password.length < 6) {
                edtRegPassword.error = "Şifre en az 6 karakter olmalı"
                return@setOnClickListener
            }
            edtRegPassword.error = null
            edtRegEmail.error = null
            surname.error = null
            name.error = null
            edtUserName.error = null
            viewModel.register(username,email,password,namee,surnamee,null,null)
        }

        forgotPassword.setOnClickListener {
            loginFields.visibility= View.GONE
            forgot.visibility= View.VISIBLE
        }
        btnSend.setOnClickListener {
            viewModel.resetPassword(edtforgotemail.text.toString());
        }
        btnnewsend.setOnClickListener{
            val password = newpsswrd.text.toString().trim()
            val confirmpassword= confirmpsswrd.text.toString().trim()

            if (password.isEmpty()&&confirmpassword.isEmpty()) {
                newpsswrd.error= "Şifre boş olamaz"
                confirmpsswrd.error= "Şifre boş olamaz"
                return@setOnClickListener
            }
            if (password.length < 6) {
                newpsswrd.error= "Şifre en az 6 karakter olmalı"
                return@setOnClickListener
            }
            if (password != confirmpassword) {
               confirmpsswrd.error="Şifre eşleşmiyor!"
                return@setOnClickListener
            }
            passwrdtoken?.let {
                viewModel.newPassword(it, password, confirmpassword)
            } ?: run {
                Toast.makeText(requireContext(), "Süresi doldu!", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun setupObservers() {
        observeUiState(viewModel.loginState, generalProgressBar) { data ->
            Toast.makeText(requireContext(), "Giriş başarılı!", Toast.LENGTH_SHORT).show()
            // Fragment yönlendirme burada yapılabilir
        }

        observeUiState(viewModel.registerState, generalProgressBar) { data ->
            Toast.makeText(requireContext(), "Email doğrulandı. Giriş yapabilirsiniz.", Toast.LENGTH_SHORT).show()
            // Fragment yönlendirme burada yapılabilir
        }

        observeUiState(viewModel.resetPasswordState, generalProgressBar) { data ->
            Toast.makeText(requireContext(), data, Toast.LENGTH_SHORT).show()
        }

        observeUiState(viewModel.newPasswordState, generalProgressBar) { data ->
            Toast.makeText(requireContext(), data, Toast.LENGTH_SHORT).show()
        }
    }

    private fun <T> observeUiState(
        liveData: LiveData<UiState<T>>,
        progressBar: View,
        onSuccess: (T) -> Unit = {}
    ) {
        liveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    onSuccess(state.data)
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Hata: ${state.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startLampAnimation() {
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.lamp_on)
        lamp.setImageResource(R.drawable.lampon)
        lamp.startAnimation(anim)

        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                lightGlow.visibility = View.VISIBLE
                layoutButtons.visibility = View.VISIBLE
                lamp.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    private fun setupButtonListeners() {
        buttonRegister.setOnClickListener {
            animateViewGone(layoutButtons) {
                registerFields.visibility = View.VISIBLE
            }
        }

        buttonLogin.setOnClickListener {
            animateViewGone(layoutButtons) {
                loginFields.visibility = View.VISIBLE
            }
        }
    }

    private fun setupBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            when {
                registerFields.visibility == View.VISIBLE -> {
                    animateViewGone(registerFields) {
                        animateViewVisible(layoutButtons)
                    }
                }
                loginFields.visibility == View.VISIBLE -> {
                    animateViewGone(loginFields) {
                        animateViewVisible(layoutButtons)
                    }
                }
                else -> {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
    }
    private fun animateViewGone(view: View, endAction: () -> Unit) {
        view.animate()
            .alpha(0f)
            .setDuration(1000)
            .withEndAction {
                view.visibility = View.GONE
                view.alpha = 1f
                endAction()
            }
            .start()
    }

    private fun animateViewVisible(view: View) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(1000)
            .start()
    }
}