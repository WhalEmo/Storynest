package com.example.storynest.Follow

import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storynest.CustomViews.ConfirmDialog
import com.example.storynest.CustomViews.InfoMessage
import com.example.storynest.Follow.Adapter.FollowAdapter
import com.example.storynest.Follow.FollowOptions.FollowOptionClickListener
import com.example.storynest.Follow.FollowOptions.FollowOptionsBottomSheet
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.FollowersLoadStateAdapter
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.FollowersUiState
import com.example.storynest.Navigator
import com.example.storynest.Profile.ProfileMode
import com.example.storynest.R
import com.example.storynest.databinding.MyFollowersFragmentBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlin.getValue

class FollowListFragment: Fragment() {

    private var _binding: MyFollowersFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FollowAdapter
    private val viewModel: FollowViewModel by viewModels()

    private lateinit var soundPool: SoundPool

    private var acceptSoundId: Int = 0
    private var cancelSoundId: Int = 0

    private val navigator = Navigator

    private var loadingStartTime: Long = 0L
    private val MIN_LOADING_DURATION = 500L

    companion object {
        private const val ARG_TYPE = "type"
        private const val ARG_USER_ID = "userId"

        fun newInstance(type: FollowType, userId: Long = -1) : FollowListFragment{
            val fragment = FollowListFragment()
            fragment.apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type.name)
                    putLong(ARG_USER_ID, userId)
                }
            }
            return fragment
        }
    }

    private val followType by lazy {
        val type = requireArguments().getString(ARG_TYPE)
            ?: throw IllegalStateException("FollowType argument is missing")

        FollowType.valueOf(type)
    }
    private val userId by lazy {
        requireArguments().getLong(ARG_USER_ID)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MyFollowersFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.setParams(followType, userId)

        headerRender()

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .build()

        acceptSoundId = soundPool.load(
            requireContext(),
            R.raw.accept,
            1
        )
        cancelSoundId = soundPool.load(
            requireContext(),
            R.raw.reject,
            1
        )

        adapter = FollowAdapter(
            onAccept = {
                onAccept(
                    it.id
                )
            },
            onReject = {
                sendMessage()
            },
            onCancelRequest = {
                onCancelRequest(
                    it.requestId
                )
            },
            onUnFollowMy = {
                onUnFollowMy(
                    it
                )
            }
            ,
            onProfileClick = {
                openProfile(
                    userId = it.id,
                    profileMode = ProfileMode.USER_PROFILE
                )
            },
            onDotMenuClick = {
                showFollowOptions(
                    userId = it.id,
                    username = it.username,
                    profileImage = it.profile ?: "",
                    profileMode = ProfileMode.USER_PROFILE
                )
            }

        )

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.itemAnimator = null
        binding.recycler.setHasFixedSize(true)

        binding.recycler.adapter = adapter.withLoadStateHeaderAndFooter(
            header = FollowersLoadStateAdapter {
                adapter.retry()
            },
            footer = FollowersLoadStateAdapter {
                adapter.retry()
            }
        )


        adapter.addLoadStateListener { loadState ->

            val refresh = loadState.refresh

            val uiState = when {
                refresh is LoadState.Loading -> {
                    FollowersUiState.Loading
                }

                refresh is LoadState.Error -> {
                    val error = refresh.error

                    val message = when (error) {
                        is IOException -> "İnternet bağlantısı yok"
                        is HttpException -> "Sunucuya ulaşılamıyor"
                        else -> "Beklenmeyen bir hata oluştu"
                    }
                    FollowersUiState.Error(
                        message
                    )
                }

                refresh is LoadState.NotLoading &&
                        adapter.itemCount == 0 -> {
                    FollowersUiState.Empty
                }

                else -> {
                    FollowersUiState.Content
                }
            }

            contentRender(uiState)

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.followers.collectLatest { pagingData ->
                    delay(50)
                    adapter.submitData(pagingData)
                }
            }
        }
    }

    private fun onAccept(userId: Long){
        soundPool.play(
            acceptSoundId,
            1f,
            1f,
            1,
            0,
            1f
        )
        viewModel.sendFollowRequest(userId, followType)
    }

    private fun onCancelRequest(followId: Long?){
        soundPool.play(
            cancelSoundId,
            1f,
            1f,
            1,
            0,
            1f
        )
        viewModel.cancelFollowRequest(followId, followType)
    }

    private fun onUnFollowMy(usrResponse: FollowRow.FollowUserItem){
        ConfirmDialog(
            title = "Takipçiyi Çıkar?",
            message = "${usrResponse.username} kullanıcıyı takipten çıkarmak istiyor musunuz?",
            imageUrl = usrResponse.profile,
            onConfirm = {
                viewModel.removeFollower(
                    userId = usrResponse.id,
                    onRemoved = {
                        InfoMessage.show(
                            requireActivity(),
                            "Takipçiden Çıkarıldı"
                        )
                    },
                    followType = followType
                )
            }
        ).show(
            parentFragmentManager,
            "ConfirmDialog"
        )
    }

    private fun headerRender(){
        when(followType) {
            FollowType.MY_FOLLOWERS -> {
                binding.headerTitle.text = "Takipçiler"
            }
            FollowType.MY_FOLLOWING -> {
                binding.headerTitle.text = "Takip Edilenler"
            }
            FollowType.USER_FOLLOWERS -> {
                binding.headerTitle.text = "Takipçiler"
            }
            FollowType.USER_FOLLOWING -> {
                binding.headerTitle.text = "Takip Edilenler"
            }
        }
    }

    private fun contentRender(
        uiState: FollowersUiState
    ){
        when(uiState){
            is FollowersUiState.Content ->{
                binding.recycler.recyclerFadeIn()
            }
            else -> null
        }
    }

    private fun sendMessage(){
        InfoMessage.show(
            requireActivity(),
            "Mesaj gönderilme henüz desteklenmiyor"
        )
    }

    private fun View.recyclerFadeIn(
        duration: Long = 200,
        onStart: (() -> Unit)? = null
    ) {
        if(isVisible) return
        animate().cancel()
        alpha = 0f
        isVisible = true
        onStart?.invoke()
        animate().alpha(1f).setDuration(duration).start()
    }

    private fun showFollowOptions(userId: Long, username: String, profileImage: String, profileMode: ProfileMode){
        val tag = "FollowOptions"

        val existing = parentFragmentManager.findFragmentByTag(tag)
        if (existing != null) return

        val sheet = FollowOptionsBottomSheet.newInstance(
            userId,
            username,
            profileImage,
            profileMode
        )
        setupFollowOptionClickListener(sheet)
        sheet.show(parentFragmentManager, tag)
    }

    private fun openProfile(
        userId: Long,
        profileMode: ProfileMode
    ){
        Log.d("FollowListFragment", "open profile userId: $userId")

        navigator.openProfile(
            activity = requireActivity() as AppCompatActivity,
            id = userId,
            mode = profileMode
        )
    }

    private fun setupFollowOptionClickListener(sheet: FollowOptionsBottomSheet){
        val listener = object : FollowOptionClickListener {
            override fun onViewProfile(userId: Long, profileMode: ProfileMode) {
                openProfile(
                    userId = userId,
                    profileMode = profileMode
                )
            }

            override fun onUnfollow(userId: Long) {
                viewModel.unFollow(userId)
            }

            override fun onBlock(userId: Long) {

            }
        }
        sheet.setFollowOptionClickListener(
            listener
        )
    }
}