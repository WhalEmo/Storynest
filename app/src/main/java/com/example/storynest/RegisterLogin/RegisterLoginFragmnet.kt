package com.example.storynest.RegisterLogin

import android.graphics.Color
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.example.storynest.ApiClient
import com.example.storynest.R
import com.example.storynest.ResultWrapper
import com.example.storynest.UiState
import com.example.storynest.dataLocal.UserPreferences
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
    private lateinit var edtLoginUsername: TextInputEditText
    private lateinit var tilLoginUsername: TextInputLayout

    private lateinit var edtLoginPassword: TextInputEditText
    private lateinit var tilLoginPassword: TextInputLayout
    private lateinit var btnLogin: Button
    private lateinit var registerFields: LinearLayout
    private lateinit var edtUserName: TextInputEditText
    private lateinit var tilUserName: TextInputLayout
    private lateinit var name: TextInputEditText
    private lateinit var tilName: TextInputLayout
    private lateinit var surname: TextInputEditText
    private lateinit var tilSurname: TextInputLayout
    private lateinit var edtRegEmail: TextInputEditText
    private lateinit var tilRegEmail: TextInputLayout
    private lateinit var edtRegPassword: TextInputEditText
    private lateinit var tilRegPassword: TextInputLayout
    private lateinit var btnRegister: Button
    private lateinit var lightGlow: View
    private lateinit var forgotPassword: TextView

    private lateinit var forgot: LinearLayout
    private lateinit var edtforgotemail: TextInputEditText
    private lateinit var tilForgotEmail: TextInputLayout
    private lateinit var btnSend: Button

    private lateinit var newpassword: LinearLayout
    private lateinit var newpsswrd: TextInputEditText
    private lateinit var tilNewPassword: TextInputLayout

    private lateinit var confirmpsswrd: TextInputEditText
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var btnnewsend: Button
    private lateinit var generalProgressBar: ProgressBar
    private var passwrdtoken: String? = null

    private lateinit var information: LinearLayout
    private lateinit var informationPassword: LinearLayout

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
        tilLoginUsername=view.findViewById(R.id.tilLoginUsername)
        edtLoginPassword=view.findViewById(R.id.edtLoginPassword)
        tilLoginPassword=view.findViewById(R.id.tilLoginPassword)
        btnLogin=view.findViewById(R.id.btnLogin)
        forgotPassword = view.findViewById(R.id.forgotPassword)


        registerFields = view.findViewById(R.id.registerFields)
        edtUserName=view.findViewById(R.id.edtUserName)
        tilUserName=view.findViewById(R.id.tilUserName)
        name=view.findViewById(R.id.name)
        tilName=view.findViewById(R.id.tilName)
        surname=view.findViewById(R.id.surname)
        tilSurname=view.findViewById(R.id.tilSurname)
        edtRegEmail=view.findViewById(R.id.edtRegEmail)
        tilRegEmail=view.findViewById(R.id.tilRegEmail)
        edtRegPassword=view.findViewById(R.id.edtRegPassword)
        tilRegPassword=view.findViewById(R.id.tilRegPassword)
        btnRegister=view.findViewById(R.id.btnRegister)
        lightGlow = view.findViewById(R.id.lightGlow)


        forgot=view.findViewById(R.id.forgot)
        edtforgotemail=view.findViewById(R.id.edtforgotemail)
        tilForgotEmail=view.findViewById(R.id.tilForgotEmail)
        btnSend=view.findViewById(R.id.btnSend)

        newpassword=view.findViewById(R.id.newpassword)
        newpsswrd=view.findViewById(R.id.newpsswrd)
        tilNewPassword=view.findViewById(R.id.tilNewPassword)
        confirmpsswrd=view.findViewById(R.id.confirmpsswrd)
        tilConfirmPassword=view.findViewById(R.id.tilConfirmPassword)
        btnnewsend=view.findViewById(R.id.btnnewsend)

        generalProgressBar=view.findViewById(R.id.generalProgressBar)
        information=view.findViewById(R.id.information)
        informationPassword=view.findViewById(R.id.informationPassword)


        setupButtonListeners()
        setupBackPressedCallback()
        clicks()
        setupObservers()

        if(login==true){
            animateViewGone(layoutButtons) {
                lamp.setImageResource(R.drawable.lampon)
                lightGlow.visibility = View.VISIBLE
                loginFields.visibility = View.VISIBLE
                forgot.visibility= View.GONE
                registerFields.visibility=View.GONE
            }
        }
        else if(register==true){
            animateViewGone(layoutButtons) {
                lamp.setImageResource(R.drawable.lampon)
                lightGlow.visibility = View.VISIBLE
                registerFields.visibility=View.VISIBLE
                loginFields.visibility = View.GONE
            }
        }else if(mainforgotpassword==true){
            animateViewGone(layoutButtons) {
                lamp.setImageResource(R.drawable.lampon)
                lightGlow.visibility = View.VISIBLE
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

    private fun clearInputs() {
        edtLoginUsername.text?.clear()
        edtLoginPassword.text?.clear()
        tilLoginUsername.error = null
        tilLoginPassword.error = null

        edtUserName.text?.clear()
        name.text?.clear()
        surname.text?.clear()
        edtRegEmail.text?.clear()
        edtRegPassword.text?.clear()
        tilRegPassword.error = null
        tilRegEmail.error = null
        tilSurname.error = null
        tilName.error = null
        tilUserName.error = null

        edtforgotemail.text?.clear()
        tilForgotEmail.error=null;


        tilNewPassword.error=null;
        tilConfirmPassword.error=null;
        newpsswrd.text?.clear()
        confirmpsswrd.text?.clear()
    }

    private fun clicks(){
        btnLogin.setOnClickListener {

            tilLoginUsername.error = null
            tilLoginPassword.error = null

            val username=edtLoginUsername.text.toString().trim()
            val password=edtLoginPassword.text.toString().trim()

            if (username.isEmpty()) {
                tilLoginUsername.error = "Kullanıcı adı boş olamaz"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                tilLoginPassword.error = "Şifre boş olamaz"
                return@setOnClickListener
            }
            if (password.length < 6) {
                tilLoginPassword.error = "Şifre en az 6 karakter olmalı"
                return@setOnClickListener
            }
            viewModel.login(username,password)
        }

        btnRegister.setOnClickListener {

            tilRegPassword.error = null
            tilRegEmail.error = null
            tilSurname.error = null
            tilName.error = null
            tilUserName.error = null

            val username=edtUserName.text.toString().trim()
            val namee=name.text.toString().trim()
            val surnamee=surname.text.toString().trim()
            val email=edtRegEmail.text.toString().trim()
            val password=edtRegPassword.text.toString().trim()

            if (username.isEmpty()) {
                tilUserName.error = "Kullanıcı adı boş olamaz"
                return@setOnClickListener
            }

            if (namee.isEmpty()) {
                tilName.error = "Ad boş olamaz"
                return@setOnClickListener
            }

            if (surnamee.isEmpty()) {
                tilSurname.error = "Soyad boş olamaz"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                tilRegEmail.error = "Email boş olamaz"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                tilRegPassword.error = "Şifre boş olamaz"
                return@setOnClickListener
            } else if (password.length < 6) {
                tilRegPassword.error = "Şifre en az 6 karakter olmalı"
                return@setOnClickListener
            }
            viewModel.register(username,email,password,namee,surnamee,null,null)
        }

        forgotPassword.setOnClickListener {
            loginFields.visibility= View.GONE
            forgot.visibility= View.VISIBLE
        }
        btnSend.setOnClickListener {
            tilForgotEmail.error=null;

            val email=edtforgotemail.text.toString().trim();
            if (email.isEmpty()) {
                tilForgotEmail.error = "Email boş olamaz"
                return@setOnClickListener
            }
            viewModel.resetPassword(email);
        }
        btnnewsend.setOnClickListener{

            tilNewPassword.error=null;
            tilConfirmPassword.error=null;

            val password = newpsswrd.text.toString().trim()
            val confirmpassword= confirmpsswrd.text.toString().trim()

            if (password.isEmpty()&&confirmpassword.isEmpty()) {
                tilNewPassword.error= "Şifre boş olamaz"
                tilConfirmPassword.error= "Şifre boş olamaz"
                return@setOnClickListener
            }
            if (password.length < 6) {
                tilNewPassword.error= "Şifre en az 6 karakter olmalı"
                return@setOnClickListener
            }
            if (password != confirmpassword) {
               tilConfirmPassword.error="Şifre eşleşmiyor!"
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
            Toast.makeText(requireContext(), "Giriş yapabilirsiniz.", Toast.LENGTH_SHORT).show()
        }

        observeUiState(viewModel.resetPasswordState, generalProgressBar) { data ->
            Toast.makeText(requireContext(), data, Toast.LENGTH_SHORT).show()
        }

        observeUiState(viewModel.newPasswordState, generalProgressBar) { data ->
            Toast.makeText(requireContext(), data, Toast.LENGTH_SHORT).show()
            animateViewGone(newpassword) {
                animateViewVisible(loginFields)
            }
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
                is UiState.EmailNotVerified->{
                    progressBar.visibility = View.GONE
                    clearInputs()
                    registerFields.visibility=View.GONE
                    information.visibility=View.VISIBLE
                }
                is UiState.EmailSent->{
                    progressBar.visibility = View.GONE
                    clearInputs()
                    forgot.visibility=View.GONE
                    informationPassword.visibility=View.VISIBLE
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
            clearInputs()
            animateViewGone(layoutButtons) {
                registerFields.visibility = View.VISIBLE
            }
        }

        buttonLogin.setOnClickListener {
            clearInputs()
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