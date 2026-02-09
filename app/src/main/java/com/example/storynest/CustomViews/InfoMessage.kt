package com.example.storynest.CustomViews

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.storynest.R

@SuppressLint("StaticFieldLeak")
object InfoMessage {

    private var view: View? = null
    private var isShowing = false

    fun show(
        activity: Activity,
        message: String,
        duration: Long = 1500
    ) {
        if (isShowing || activity.isFinishing) return

        val windowManager =
            activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val inflater = LayoutInflater.from(activity)
        view = inflater.inflate(R.layout.info_box, null)

        val textView = view!!.findViewById<TextView>(R.id.tvMessage)
        textView.text = message

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER
        params.token = activity.window.decorView.windowToken

        view!!.alpha = 0f
        windowManager.addView(view, params)

        view!!.animate()
            .alpha(1f)
            .setDuration(200)
            .withEndAction {
                view!!.postDelayed({
                    hide(windowManager)
                }, duration)
            }

        isShowing = true
    }

    private fun hide(windowManager: WindowManager) {
        view?.animate()
            ?.alpha(0f)
            ?.setDuration(200)
            ?.withEndAction {
                try {
                    windowManager.removeView(view)
                } catch (_: Exception) { }
                view = null
                isShowing = false
            }
    }
}

