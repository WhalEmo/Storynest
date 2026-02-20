package com.example.storynest.Follow.FollowOptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.storynest.databinding.FollowingOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FollowOptionsBottomSheet(
    private val username: String,
    private val listener: FollowOptionClickListener
) : BottomSheetDialogFragment() {

    private var _binding: FollowingOptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FollowingOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.tvTitle.text = "@$username"


        binding.profile.layoutViewProfile.setOnClickListener {
            listener.onViewProfile()
            dismiss()
        }

        binding.unFollow.layoutUnfollow.setOnClickListener {
            listener.onUnfollow()
            dismiss()
        }

        binding.block.layoutBlock.setOnClickListener {
            listener.onBlock()
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}