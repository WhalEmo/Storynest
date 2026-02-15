package com.example.storynest.CustomViews

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import coil.load
import com.example.storynest.R
import com.google.android.material.button.MaterialButton

class ConfirmDialog(
    private val title: String,
    private val message: String,
    private val imageUrl: String? = null,
    private val onConfirm: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_confirm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val titleTv = dialog.findViewById<TextView>(R.id.tvTitle)
        val messageTv = dialog.findViewById<TextView>(R.id.tvMessage)
        val btnConfirm = dialog.findViewById<MaterialButton>(R.id.btnConfirm)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val imgIcon = dialog.findViewById<ImageView>(R.id.imgIcon)

        titleTv.text = title
        messageTv.text = message

        setupImage(imgIcon)

        btnConfirm.setOnClickListener {
            onConfirm.invoke()
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    private fun setupImage(imageView: ImageView){
        if (imageUrl.isNullOrBlank()) {
            imageView.visibility = View.GONE
            return
        }

        imageView.visibility = View.VISIBLE

        imageView.load(imageUrl){
            crossfade(false)
            placeholder(R.drawable.placeholder)
            allowHardware(true)
        }
    }
}
