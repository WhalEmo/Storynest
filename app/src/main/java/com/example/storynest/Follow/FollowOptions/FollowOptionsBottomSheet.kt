package com.example.storynest.Follow.FollowOptions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import coil.load
import com.example.storynest.Profile.ProfileMode
import com.example.storynest.R
import com.example.storynest.databinding.FollowingOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FollowOptionsBottomSheet: BottomSheetDialogFragment() {

    private var _binding: FollowingOptionsBinding? = null
    private val binding get() = _binding!!

    private var userId: Long = -1L
    private lateinit var username: String
    private lateinit var profileImage: String
    private lateinit var profileMode: ProfileMode
    private lateinit var listener: FollowOptionClickListener


    companion object {

        private const val ARG_USER_ID = "arg_user_id"
        private const val ARG_USERNAME = "arg_username"
        private const val ARG_PROFILE_IMAGE = "arg_profile_image"
        private const val ARG_PROFILE_MODE = "arg_profile_mode"

        fun newInstance(
            userId: Long,
            username: String,
            profileImage: String,
            profileMode: ProfileMode
        ): FollowOptionsBottomSheet {

            return FollowOptionsBottomSheet().apply {
                arguments = bundleOf(
                    ARG_USER_ID to userId,
                    ARG_USERNAME to username,
                    ARG_PROFILE_IMAGE to profileImage,
                    ARG_PROFILE_MODE to profileMode.name
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            userId = bundle.getLong(ARG_USER_ID)
            username = bundle.getString(ARG_USERNAME).orEmpty()
            profileImage = bundle.getString(ARG_PROFILE_IMAGE).orEmpty()

            val typeName = bundle.getString(ARG_PROFILE_MODE)
            profileMode = ProfileMode.valueOf(typeName ?: ProfileMode.MY_PROFILE.name)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FollowingOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.tvTitle.text = "$username"
        binding.imgIcon.load(profileImage){
            crossfade(true)
            crossfade(500)
            placeholder(R.drawable.placeholder)
        }


        binding.layoutViewProfile.setOnClickListener {
            Log.d("FollowOptionsBottomSheet", "layoutViewProfile userId: $userId")
            listener.onViewProfile(userId, profileMode)
            dismiss()
        }

        binding.unFollow.layoutUnfollow.setOnClickListener {
            Log.d("FollowOptionsBottomSheet", "unFollow userId: $userId")
            listener.onUnfollow(userId)
            dismiss()
        }

        binding.block.layoutBlock.setOnClickListener {
            Log.d("FollowOptionsBottomSheet", "block userId: $userId")
            listener.onBlock(userId)
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }


    fun setFollowOptionClickListener(listener: FollowOptionClickListener){
        this.listener = listener
    }
}