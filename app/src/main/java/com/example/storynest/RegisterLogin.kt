package com.example.storynest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class RegisterLogin : Fragment() {

    private val viewModel: RegisterLoginViewModel by viewModels()
    private lateinit var lamp: ImageView
    private lateinit var layoutButtons: LinearLayout
    private lateinit var buttonRegister: Button
    private lateinit var buttonLogin: Button
    private lateinit var loginFields: LinearLayout
    private lateinit var registerFields: LinearLayout
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
        registerFields = view.findViewById(R.id.registerFields)
        lightGlow = view.findViewById(R.id.lightGlow)

        startLampAnimation()

        setupButtonListeners()

        setupBackPressedCallback()
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
