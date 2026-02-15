package com.example.storynest.Profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.storynest.Follow.FollowListFragment
import com.example.storynest.Follow.FollowType
import com.example.storynest.MainActivity
import com.example.storynest.Notification.NotificationFragment
import com.example.storynest.R
import com.example.storynest.Settings.SettingsFragment
import com.example.storynest.databinding.MyProfileFragmentBinding
import kotlinx.coroutines.launch


class ProfileFragment : Fragment(){

    private var _binding: MyProfileFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel


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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MyProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        settingsButtonAnimation()

        binding.notificationBook.setOnClickListener {
            navigateTo(NotificationFragment())
        }

        binding.followersContainer.setOnClickListener {
            when(profileMode){
                ProfileMode.MY_PROFILE -> {
                    navigateTo(FollowListFragment.newInstance(
                        type = FollowType.MY_FOLLOWERS
                    ))
                }
                else -> {}
            }
        }
        binding.followingContainer.setOnClickListener {
            when(profileMode){
                ProfileMode.MY_PROFILE -> {
                    navigateTo(FollowListFragment.newInstance(
                        type = FollowType.MY_FOLLOWING
                    ))
                }
                else -> {
                }
            }
        }

        viewModel.init(
            mode = profileMode
        )
        observeScreenState()
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
                    navigateTo(SettingsFragment())
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

    fun navigateTo(fragment: Fragment) {
        (requireActivity() as MainActivity).navigateTo(fragment)
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

    }


}