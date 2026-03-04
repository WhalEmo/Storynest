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
import com.example.storynest.Profile.ProfileOptions.ProfileOptionsBottomSheet
import com.example.storynest.Profile.ProfileOptions.ProfileOptionsClickListener
import com.example.storynest.Profile.ProfileUiStates.ProfileBasicUiState
import com.example.storynest.Profile.ProfileUiStates.ProfileBlockUiState
import com.example.storynest.Profile.ProfileUiStates.ProfileUiState
import com.example.storynest.R
import com.example.storynest.databinding.ProfileBlockedBinding
import com.example.storynest.databinding.ProfileFragmentBinding
import com.example.storynest.databinding.ProfileHeaderBinding
import kotlinx.coroutines.launch


class ProfileFragment : Fragment(){

    private var _binding: ProfileFragmentBinding? = null
    private var _headerBinding: ProfileHeaderBinding? = null
    private val headerBinding get() = _headerBinding!!
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val navigator = Navigator
    private lateinit var userData: ProfileUiState



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

        fun newInstance(mode: ProfileMode, userId: Long) : ProfileFragment{
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

        if(_headerBinding == null){
            _headerBinding = ProfileHeaderBinding.bind(binding.root)
        }

        settingsButtonAnimation()

        setupButtonFlow(
            profileMode = profileMode,
            userId = userId
        )

        binding.toolBar.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.toolBar.notificationBook.setOnClickListener {
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
        _headerBinding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun settingsButtonAnimation() {

        binding.toolBar.settingsButton.setOnTouchListener { view, event ->
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
                        is ProfileScreenState.Update -> {
                            basicRender(state.uiState)
                        }
                        is ProfileScreenState.Blocked -> {
                            blockRender(state.uiState)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun render(state: ProfileUiState) {
        binding.profileHeaderGroup.isVisible = true
        binding.containerBlockedByMe.isVisible = false

        headerBinding.username.text = state.username
        headerBinding.nameSurname.text = "${state.name} ${state.surname}"
        headerBinding.biography.text = state.biography

        headerBinding.profileImage.load(state.profileImageUrl){
            crossfade(true)
            placeholder(R.drawable.placeholder)
        }

        headerBinding.followersCount.text = state.followers.toString()
        headerBinding.followingCount.text = state.following.toString()

        headerBinding.btnEditProfile.isVisible = state.showEditButton
        headerBinding.btnFollow.isVisible = state.showFollowButton
        binding.toolBar.settingsButton.isVisible = state.showSettingsButton
        binding.toolBar.dotMenu.isVisible = state.showDotMenuButton
        binding.toolBar.notificationContainer.isVisible = state.showNotificationButton
        headerBinding.btnMessage.isVisible = state.showMessageButton

        headerBinding.btnFollowYour.isVisible = state.btnFollowYour
        headerBinding.btnShareProfile.isVisible = state.btnShareProfile
        headerBinding.btnPendingRequest.isVisible = state.showPendingRequestButton
        userData = state

    }

    private fun blockRender(state: ProfileBlockUiState){
        binding.profileHeaderGroup.isVisible = false
        binding.containerBlockedByMe.isVisible = true
        binding.toolBar.settingsButton.isVisible = false
        binding.toolBar.notificationContainer.isVisible = false

        val blockBinding = ProfileBlockedBinding.bind(binding.containerBlockedByMe)

        blockBinding.txtBlockTitle.text = state.textUnBlock
        blockBinding.btnUnblock.isVisible = state.showUnBlockButton

        blockBinding.btnUnblock.setOnClickListener {
            viewModel.unBlockUser(userId, profileMode)
        }

    }

    private fun basicRender(state: ProfileBasicUiState){
        headerBinding.btnFollow.isVisible = state.showFollowButton ?: false
        headerBinding.btnMessage.isVisible = state.showMessageButton ?: false
        headerBinding.btnPendingRequest.isVisible = state.showPendingRequestButton
        headerBinding.btnFollowYour.isVisible = state.btnFollowYour
        headerBinding.followersCount.text = state.followersCount.toString()
        headerBinding.followingCount.text = state.followingCount.toString()
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
        headerBinding.followersContainer.setOnClickListener {
            navigator.openFollowList(
                activity = requireActivity() as AppCompatActivity,
                type = followersContainerType,
                userId = userId
            )
        }
        headerBinding.followingContainer.setOnClickListener {
            navigator.openFollowList(
                activity = requireActivity() as AppCompatActivity,
                type = followingContainerType,
                userId = userId
            )
        }
        headerBinding.btnFollow.setOnClickListener {
            viewModel.followUser(
                userId = userId,
                profileMode = profileMode
            )
        }
        headerBinding.btnFollowYour.setOnClickListener {
            viewModel.followUser(
                userId = userId,
                profileMode = profileMode
            )
        }

        binding.toolBar.dotMenu.setOnClickListener {
            showProfileOptions()
        }
    }


    private fun showProfileOptions(){
        val sheet = ProfileOptionsBottomSheet.newInstance(
            userId = userId,
            username = userData.username,
            profileImage = userData.profileImageUrl ?: ""
        )
        createOptionsListener(sheet)
        sheet.show(parentFragmentManager, "ProfileOptions")

    }

    private fun createOptionsListener(sheet: ProfileOptionsBottomSheet){
        val listener = object : ProfileOptionsClickListener {
            override fun onUnFollow(userId: Long) {
                viewModel.unFollowUser(userId)
            }

            override fun onBlock(userId: Long) {
                viewModel.unBlockUser(userId, profileMode)
            }

            override fun onMessage(userId: Long) {

            }

            override fun onShare(userId: Long) {
            }

        }
        sheet.setListener(listener)
    }

}