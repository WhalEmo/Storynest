package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import android.annotation.SuppressLint
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
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
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storynest.CustomViews.ConfirmDialog
import com.example.storynest.CustomViews.InfoMessage
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersAdapter
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import com.example.storynest.R
import com.example.storynest.databinding.MyFollowersFragmentBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MyFollowersFragment: Fragment() {

    private var _binding: MyFollowersFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FollowersAdapter
    private lateinit var viewModel: MyFollowersViewModel

    private lateinit var soundPool: SoundPool

    private var acceptSoundId: Int = 0
    private var cancelSoundId: Int = 0

    private var loadingStartTime: Long = 0L
    private val MIN_LOADING_DURATION = 500L



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
        viewModel = ViewModelProvider(this)[MyFollowersViewModel::class.java]

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

        adapter = FollowersAdapter(
            onAccept = {
                onAccept(
                    it.followUserResponseDTO.id
                )
            },
            onReject = {
                sendMessage()
            },
            onCancelRequest = {
                onCancelRequest(
                    it.followUserResponseDTO.followInfo.id
                )
            },
            onUnFollowMy = {
                onUnFollowMy(
                    it.followUserResponseDTO
                )
            }
        )

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.itemAnimator = null
        binding.recycler.setHasFixedSize(true)

        binding.recycler.adapter = adapter.withLoadStateHeaderAndFooter(
            header = FollowersLoadStateAdapter{
                adapter.retry()
            },
            footer = FollowersLoadStateAdapter{
                adapter.retry()
            }
        )

        setupRetry()

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

            render(uiState)
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
        viewModel.sendFollowRequest(userId)
    }

    private fun onCancelRequest(followId: Long){
        soundPool.play(
            cancelSoundId,
            1f,
            1f,
            1,
            0,
            1f
        )
        viewModel.cancelFollowRequest(followId)
    }

    private fun onUnFollowMy(usrResponse: FollowUserResponseDTO){
        ConfirmDialog(
            title = "Takipçiyi Çıkar?",
            message = "${usrResponse.username} kullanıcıyı takipten çıkarmak istiyor musunuz?",
            imageUrl = usrResponse.profile,
            onConfirm = {
                viewModel.removeFollower(
                    usrResponse.id,
                    {
                        InfoMessage.show(
                            requireActivity(),
                            "Takipçiden Çıkarıldı"
                        )
                    }
                )
            }
        ).show(
            parentFragmentManager,
            "ConfirmDialog"
        )
    }

    private fun sendMessage(){
        InfoMessage.show(
            requireActivity(),
            "Mesaj gönderilme henüz desteklenmiyor"
        )
    }

    private fun render(state: FollowersUiState) = with(binding) {

        emptyLayout.isVisible = state is FollowersUiState.Empty

        when(state){
            is FollowersUiState.Loading -> showLoading()
            is FollowersUiState.Error -> showError(state.message)
            is FollowersUiState.Content -> showContent()
            else -> hideOverlays()
        }
    }

    private fun setupRetry() {
        binding.retryButton.setOnClickListener {
            showLoading()
            adapter.retry()
        }
    }

    private fun showContent() = with(binding){
        hideLoadingWithDelay {
            recycler.recyclerFadeIn()
        }
    }


    private fun showLoading() = with(binding){
        loadingStartTime = System.currentTimeMillis()
        errorLayout.fadeOut{
            errorAnimation.cancelAnimation()
        }
        progressBar.fadeIn {
            progressBar.playAnimation()
        }
    }

    private fun showError(message: String) = with(binding){
        progressBar.fadeOut{
            progressBar.cancelAnimation()
        }
        errorText.text = message
        errorLayout.fadeIn{
            errorAnimation.playAnimation()
        }
    }
    private fun hideOverlays() = with(binding){
        progressBar.fadeOut{
            progressBar.cancelAnimation()
        }
        errorLayout.fadeOut {
            errorAnimation.cancelAnimation()
        }
    }

    private fun View.fadeIn(
        duration: Long = 200,
        onStart: (() -> Unit)? = null
    ) {
        animate().cancel()
        alpha = 0f
        isVisible = true
        onStart?.invoke()
        animate().alpha(1f).setDuration(duration).start()
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


    private fun View.fadeOut(
        duration: Long = 200,
        onEnd: (() -> Unit)? = null
    ) {
        animate().cancel()
        animate().alpha(0f).setDuration(duration).withEndAction {
            isVisible = false
            onEnd?.invoke()
        }.start()
    }

    private fun hideLoadingWithDelay(onEnd: () -> Unit) = with(binding) {
        val elapsed = System.currentTimeMillis() - loadingStartTime
        val remaining = MIN_LOADING_DURATION - elapsed

        if (remaining > 0) {
            progressBar.postDelayed({
                onEnd
                progressBar.fadeOut {
                    progressBar.cancelAnimation()
                }
            }, remaining)
        } else {
            progressBar.fadeOut {
                progressBar.cancelAnimation()
                onEnd()
            }
        }
    }


}