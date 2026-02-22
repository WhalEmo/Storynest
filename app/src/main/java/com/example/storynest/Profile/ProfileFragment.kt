package com.example.storynest.Profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.storynest.Follow.FollowType
import com.example.storynest.Navigator
import com.example.storynest.R
import com.example.storynest.databinding.ProfileFragmentBinding
import kotlinx.coroutines.launch


class ProfileFragment : Fragment(){

    private var _binding: ProfileFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val navigator = Navigator



    private val profileMode by lazy {
        val mode = requireArguments().getString(ARG_MODE)
            ?: throw IllegalStateException("ProfileMode argument is missing")

        ProfileMode.valueOf(mode)
    }
    private val userId by lazy {
        requireArguments().getLong(ARG_USER_ID)
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_USER_ID = "userId"

        fun newInstance(mode: ProfileMode, userId: Long = -1) : ProfileFragment{
            val fragment = ProfileFragment()
            fragment.apply {
                arguments = Bundle().apply {
                    putString(ARG_MODE, mode.name)
                    putLong(ARG_USER_ID, userId)
                }
            }
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("INSTANCE_TEST", "Fragment hash: ${this.hashCode()} tag=$tag")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        settingsButtonAnimation()

        setupButtonFlow(
            profileMode = profileMode,
            userId = userId
        )

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.notificationBook.setOnClickListener {
            navigator.openNotification(
                activity = requireActivity() as AppCompatActivity
            )
        }

        Log.d("ProfileFragment", "userId: $userId")

        observeScreenState()

        viewModel.init(
            mode = profileMode,
            userId = userId
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun settingsButtonAnimation() {

        binding.settingsButton.setOnTouchListener { view, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    view.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .rotationBy(360f)
                        .setDuration(150)
                        .start()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                    navigator.openSettings(
                        activity = requireActivity() as AppCompatActivity
                    )
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                    true
                }

                else -> false
            }
        }
    }


    private fun observeScreenState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ProfileScreenState.Success -> {
                            render(state.uiState)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun render(state: ProfileUiState) {
        binding.username.text = state.username
        binding.nameSurname.text = "${state.name} ${state.surname}"
        binding.biography.text = state.biography

        binding.profileImage.load(state.profileImageUrl){
            crossfade(true)
            placeholder(R.drawable.placeholder)
        }

        binding.followersCount.text = state.followers.toString()
        binding.followingCount.text = state.following.toString()

        binding.btnEditProfile.isVisible = state.showEditButton
        binding.btnFollow.isVisible = state.showFollowButton
        binding.settingsButton.isVisible = state.showSettingsButton
        binding.dotMenu.isVisible = state.showDotMenuButton
        binding.notificationContainer.isVisible = state.showNotificationButton
        binding.btnMessage.isVisible = state.showMessageButton

        binding.btnFollowYour.isVisible = state.btnFollowYour
        binding.btnShareProfile.isVisible = state.btnShareProfile
        binding.btnPendingRequest.isVisible = state.showPendingRequestButton

    }

    private fun setupButtonFlow(
        profileMode: ProfileMode,
        userId: Long
    ){
        when(profileMode){
            ProfileMode.MY_PROFILE -> {

            }
            ProfileMode.USER_PROFILE -> {

            }
        }
        val followingContainerType = when(profileMode){
            ProfileMode.MY_PROFILE -> {
                FollowType.MY_FOLLOWING
            }
            ProfileMode.USER_PROFILE -> {
                FollowType.USER_FOLLOWING
            }
        }
        val followersContainerType = when(profileMode){
            ProfileMode.MY_PROFILE -> {
                FollowType.MY_FOLLOWERS
            }
            ProfileMode.USER_PROFILE -> {
                FollowType.USER_FOLLOWERS
            }
        }
        binding.followersContainer.setOnClickListener {
            navigator.openFollowList(
                activity = requireActivity() as AppCompatActivity,
                type = followersContainerType,
                userId = userId
            )
        }
        binding.followingContainer.setOnClickListener {
            navigator.openFollowList(
                activity = requireActivity() as AppCompatActivity,
                type = followingContainerType,
                userId = userId
            )
        }
    }

}