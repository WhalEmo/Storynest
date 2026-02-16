package com.example.storynest.CustomViews

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.storynest.R

@SuppressLint("StaticFieldLeak")
object InfoMessage {

    private var view: View? = null
    private var isShowing = false
    private val handler = Handler(Looper.getMainLooper())

    fun show(
        fragment: Fragment,
        message: String,
        duration: Long = 1500
    ) {
        if (isShowing) return
        val parent = fragment.view?.rootView as? ViewGroup ?: return
        val inflater = LayoutInflater.from(fragment.requireContext())
        view = inflater.inflate(R.layout.info_box, parent, false)

        val textView = view!!.findViewById<TextView>(R.id.tvMessage)
        textView.text = message

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER

        view!!.alpha = 0f
        parent.addView(view, params)

        view!!.animate()
            .alpha(1f)
            .setDuration(200)
            .withEndAction {
                handler.postDelayed({
                    hide(parent)
                }, duration)
            }

        isShowing = true
    }

    private fun hide(parent: ViewGroup) {
        view?.animate()
            ?.alpha(0f)
            ?.setDuration(200)
            ?.withEndAction {
                try {
                    parent.removeView(view)
                } catch (_: Exception) {}
                view = null
                isShowing = false
            }
    }
}
