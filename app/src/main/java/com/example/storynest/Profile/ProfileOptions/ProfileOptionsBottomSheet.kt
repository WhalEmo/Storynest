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
import androidx.core.view.isVisible
import com.example.storynest.Block.BlockStatus
import com.example.storynest.CustomViews.ConfirmDialog.ConfirmDialog
import com.example.storynest.CustomViews.ConfirmDialog.ConfirmDialogStatus
import com.example.storynest.R
import com.example.storynest.databinding.ItemActionRowBinding
import com.example.storynest.databinding.ProfileOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProfileOptionsBottomSheet: BottomSheetDialogFragment() {

    private var _binging: ProfileOptionsBinding? = null
    private val binding get() = _binging!!

    private val userId by lazy {
        arguments?.getLong(ARG_USER_ID) ?: -1
    }
    private val username by lazy {
        arguments?.getString(ARG_USERNAME).orEmpty()
    }
    private val profileImage by lazy {
        arguments?.getString(ARG_PROFILE_IMAGE).orEmpty()
    }
    private val status by lazy {
        arguments?.getString(ARG_STATUS).let {
            if(it == null) return@let ProfileOptionsState.NORMAL_OPTIONS
            ProfileOptionsState.valueOf(it)
        }
    }

    private lateinit var listener: ProfileOptionsClickListener

    companion object{
        private const val ARG_USER_ID = "arg_user_id"
        private const val ARG_USERNAME = "arg_username"
        private const val ARG_PROFILE_IMAGE = "arg_profile_image"
        private const val ARG_STATUS = "arg__status"

        fun newInstance(userId: Long, username: String, profileImage: String, status: ProfileOptionsState): ProfileOptionsBottomSheet{
            return ProfileOptionsBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                    putString(ARG_PROFILE_IMAGE, profileImage)
                    putString(ARG_STATUS, status.name)
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binging = null
    }

    private fun renderUi(){
        hideAllActions()

        setupAction(
            actionBinding = binding.actionUnfollow,
            title = "Takipten Çık",
            icon = R.drawable.unfollow_icon,
            color = R.color.unfollow_text_selector,
            isVisibility = status.showUnfollow,
            onClick = { listener.onUnFollow(userId) }
        )

        setupAction(
            actionBinding = binding.actionBlock,
            title = status.blockText,
            icon = R.drawable.block,
            color = R.color.block_text_selector,
            isVisibility = status.showBlock,
            onClick = {
                if(status == ProfileOptionsState.BLOCKER_OPTIONS) {
                    listener.onUnBlock(userId)
                }
                else{
                    listener.onBlock(userId)
                }
            }
        )

        setupAction(
            actionBinding = binding.actionMessage,
            title = "Mesaj Gönder",
            icon = R.drawable.message_icon,
            color = R.color.colorTextPrimary,
            isVisibility = status.showMessage,
            onClick = { listener.onMessage(userId) }
        )

        setupAction(
            actionBinding = binding.actionShare,
            title = "Hesabı Paylaş",
            icon = R.drawable.share_icon,
            color = R.color.colorTextPrimary,
            isVisibility = status.showShare,
            onClick = { listener.onShare(userId) }
        )

    }

    private fun setupAction(
        actionBinding: ItemActionRowBinding,
        title: String,
        isVisibility: Boolean = true,
        @DrawableRes icon: Int,
        @ColorRes color: Int,
        onClick: () -> Unit = {}
    ) {
        if(!isVisibility){
            actionBinding.root.isVisible = false
            return
        }
        actionBinding.root.isVisible = true
        actionBinding.tvActionTitle.setColorAndText(color, title)
        actionBinding.ivActionIcon.setColoredIcon(icon, color)
        actionBinding.root.setOnClickListener {
            onClick()
            dismiss()
        }
    }

    private fun hideAllActions() {
        binding.apply {
            actionBlock.root.isVisible = false
            actionUnfollow.root.isVisible = false
            actionMessage.root.isVisible = false
            actionShare.root.isVisible = false
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