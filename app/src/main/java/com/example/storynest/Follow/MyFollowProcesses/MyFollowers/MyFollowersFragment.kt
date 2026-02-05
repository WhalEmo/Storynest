package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import android.media.SoundPool
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storynest.CustomViews.ErrorDialog
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersAdapter
import com.example.storynest.R
import com.example.storynest.databinding.MyFollowersFragmentBinding

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

            },
            onCancelRequest = {
                onCancelRequest(
                    it.followUserResponseDTO.followInfo.id
                )
            }
        )
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        viewModel.getMyFollowers()



        viewModel.rows.observe(viewLifecycleOwner){ rows ->
            adapter.submitList(rows)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if(!error.isNullOrEmpty()){
                var errorDialog = ErrorDialog.newInstance(
                    title = "Bağlantı Hatası",
                    message = error
                )
                errorDialog.setOnOkClickListener{
                    viewModel.getMyFollowers()
                }
                errorDialog.show(parentFragmentManager, "ErrorDialogTag")
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


}