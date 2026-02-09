package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import android.media.SoundPool
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storynest.CustomViews.ConfirmDialog
import com.example.storynest.CustomViews.ErrorDialog
import com.example.storynest.CustomViews.InfoMessage
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersAdapter
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersRow
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO
import com.example.storynest.R
import com.example.storynest.databinding.MyFollowersFragmentBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyFollowersFragment: Fragment() {

    private var _binding: MyFollowersFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FollowersAdapter
    private lateinit var viewModel: MyFollowersViewModel

    private lateinit var soundPool: SoundPool

    private var acceptSoundId: Int = 0
    private var cancelSoundId: Int = 0


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
        binding.recycler.adapter = adapter.withLoadStateFooter(
            footer = FollowersLoadStateAdapter{
                adapter.retry()
            }
        )



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


}