package com.example.storynest.Profile.ProfileOptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.storynest.CustomViews.ConfirmDialog.ConfirmDialog
import com.example.storynest.CustomViews.ConfirmDialog.ConfirmDialogStatus
import com.example.storynest.R
import com.example.storynest.databinding.ProfileOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProfileOptionsBottomSheet: BottomSheetDialogFragment() {

    private var _binging: ProfileOptionsBinding? = null
    private val binding get() = _binging!!

    private var userId: Long = -1L
    private var username: String = ""
    private var profileImage: String = ""

    private lateinit var listener: ProfileOptionsClickListener

    companion object{
        private const val ARG_USER_ID = "arg_user_id"
        private const val ARG_USERNAME = "arg_username"
        private const val ARG_PROFILE_IMAGE = "arg_profile_image"

        fun newInstance(userId: Long, username: String, profileImage: String): ProfileOptionsBottomSheet{
            return ProfileOptionsBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                    putString(ARG_PROFILE_IMAGE, profileImage)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binging = ProfileOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            userId = bundle.getLong(ARG_USER_ID)
            username = bundle.getString(ARG_USERNAME).orEmpty()
            profileImage = bundle.getString(ARG_PROFILE_IMAGE).orEmpty()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActions()
        setupButtonActions()
    }


    private fun setupActions() {
        binding.actionUnfollow.tvActionTitle.text = "Takipten Çık"


        binding.actionBlock.tvActionTitle.setColorAndText(
            colorRes = R.color.block_text_selector,
            text = "Engelle"
        )
        binding.actionBlock.ivActionIcon.setColoredIcon(
            iconRes = R.drawable.block,
            colorRes = R.color.block_text_selector
        )
        binding.actionUnfollow.tvActionTitle.setColorAndText(
            colorRes = R.color.unfollow_text_selector,
            text = "Takipten Çık"
        )
        binding.actionUnfollow.ivActionIcon.setColoredIcon(
            iconRes = R.drawable.unfollow_icon,
            colorRes = R.color.unfollow_text_selector
        )
        binding.actionMessage.tvActionTitle.setColorAndText(
            colorRes = R.color.colorTextPrimary,
            text = "Mesaj Gönder"
        )
        binding.actionMessage.ivActionIcon.setColoredIcon(
            iconRes = R.drawable.message_icon,
            colorRes = R.color.colorTextPrimary
        )
        binding.actionShare.tvActionTitle.setColorAndText(
            colorRes = R.color.colorTextPrimary,
            text = "Profili Paylaş"
        )
        binding.actionShare.ivActionIcon.setColoredIcon(
            iconRes = R.drawable.share_icon,
            colorRes = R.color.colorTextPrimary
        )
    }

    private fun setupButtonActions(){
        binding.actionUnfollow.root.setOnClickListener {
            showDialog(
                status = ConfirmDialogStatus.UN_FOLLOW_DIALOG,
                onConfirm = {
                    listener.onUnFollow(userId)
                    dismiss()
                }
            )
        }
        binding.actionBlock.root.setOnClickListener {
            showDialog(
                status = ConfirmDialogStatus.BLOCK_DIALOG,
                onConfirm = {
                    listener.onBlock(userId)
                    dismiss()
                }
            )
        }
        binding.actionMessage.root.setOnClickListener {
            listener.onMessage(userId)
            dismiss()
        }
        binding.actionShare.root.setOnClickListener {
            listener.onShare(userId)
            dismiss()
        }
    }


    fun TextView.setColorAndText(
        @ColorRes colorRes: Int,
        text: String
    ){
        val color = ContextCompat.getColorStateList(context, colorRes)
        this.setTextColor(color)
        this.text = text
    }

    fun ImageView.setColoredIcon(
        @DrawableRes iconRes: Int,
        @ColorRes colorRes: Int
    ){
        val color = ContextCompat.getColorStateList(context, colorRes)
        this.imageTintList = color
        this.setImageResource(iconRes)
    }

    fun setListener(listener: ProfileOptionsClickListener){
        this.listener = listener
    }

    private fun showDialog(
        status: ConfirmDialogStatus,
        onConfirm: () -> Unit
    ){
        ConfirmDialog(
            status = status,
            username = username,
            onConfirm = onConfirm,
            imageUrl = profileImage
        ).show(
            parentFragmentManager,
            "ConfirmDialog_${status.name}_${userId}"
        )
    }
}