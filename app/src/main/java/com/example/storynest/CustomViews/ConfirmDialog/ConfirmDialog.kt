package com.example.storynest.CustomViews.ConfirmDialog

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import coil.load
import com.example.storynest.R
import com.google.android.material.button.MaterialButton

class ConfirmDialog(
    private val status: ConfirmDialogStatus,
    private val username: String,
    private val imageUrl: String? = null,
    private val onConfirm: () -> Unit,
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

        titleTv.text = status.title
        messageTv.text = username.plus(status.message)
        btnConfirm.text = status.confirmText
        btnCancel.text = status.cancelText

        setupImage(imgIcon)
        setupStatusDialog(btnConfirm, btnCancel)

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

    private fun setupStatusDialog(
        btnConfirm: MaterialButton,
        btnCancel: MaterialButton
    ) {
        when (status) {
            ConfirmDialogStatus.UN_FOLLOW_DIALOG -> {
                btnConfirm.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.un_follow_dialog)
                    )

                btnConfirm.setTextColor(Color.WHITE)
            }
            ConfirmDialogStatus.BLOCK_DIALOG -> {
                btnConfirm.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.block_text_selector)
                    )
                btnConfirm.strokeWidth = 0
                btnConfirm.elevation = 2f
                btnConfirm.setTextColor(Color.WHITE)
            }
            else -> {}
        }
    }
}