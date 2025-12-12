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
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.storynest.R
import com.example.storynest.RegisterLogin.RegisterLoginViewModel
import com.example.storynest.ResultWrapper
import com.example.storynest.dataLocal.UserPreferences

class RegisterLogin : Fragment() {
    private val userPrefs by lazy { UserPreferences.getInstance(requireContext()) }

    private val viewModel: RegisterLoginViewModel by viewModels {
       RegisterLoginViewModelFactory(userPrefs)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_register_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lamp = view.findViewById(R.id.lambaSonuk)
        layoutButtons = view.findViewById(R.id.initialButtons)
        buttonRegister = view.findViewById(R.id.btnShowRegister)
        buttonLogin = view.findViewById(R.id.btnShowLogin)

        loginFields = view.findViewById(R.id.loginFields)
        edtLoginUsername=view.findViewById(R.id.edtLoginUsername)
        edtLoginPassword=view.findViewById(R.id.edtLoginPassword)
        btnLogin=view.findViewById(R.id.btnLogin)

        registerFields = view.findViewById(R.id.registerFields)
        edtUserName=view.findViewById(R.id.edtUserName)
        name=view.findViewById(R.id.name)
        surname=view.findViewById(R.id.surname)
        edtRegEmail=view.findViewById(R.id.edtRegEmail)
        edtRegPassword=view.findViewById(R.id.edtRegPassword)
        btnRegister=view.findViewById(R.id.btnRegister)
        lightGlow = view.findViewById(R.id.lightGlow)

        startLampAnimation()
        setupButtonListeners()
        setupBackPressedCallback()
        clicks()
        setupObservers()
    }
    private fun clicks(){
        btnLogin.setOnClickListener {
            val username=edtLoginUsername.text.toString()
            val password=edtLoginPassword.text.toString()
            viewModel.login(username,password)
        }
        btnRegister.setOnClickListener {
            val username=edtUserName.text.toString()
            val name=name.text.toString()
            val surname=surname.text.toString()
            val edtRegEmail=edtRegEmail.text.toString()
            val edtRegPassword=edtRegPassword.text.toString()
            viewModel.register(username,edtRegEmail,edtRegPassword,name,surname,null,null)
        }

    }
    private fun setupObservers(){
        viewModel.loginResult.observe(viewLifecycleOwner){result->
            when(result){
                is ResultWrapper.Success -> {
                    Toast.makeText(requireContext(), "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                    // //FRAGMNET YONLENDİRMESİ YAPILCAK
                }
                is ResultWrapper.Error -> {
                    Toast.makeText(requireContext(), "Hata: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.registerResult.observe(viewLifecycleOwner){result->
            when(result) {
                is ResultWrapper.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Kayıt başarılı! Otomatik giriş yapılıyor...",
                        Toast.LENGTH_SHORT
                    ).show()
                    //FRAGMNET YONLENDİRMESİ YAPILCAK
                }
                is ResultWrapper.Error -> {
                    Toast.makeText(requireContext(), "Hata: ${result.message}", Toast.LENGTH_SHORT)
                        .show()
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