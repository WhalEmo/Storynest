package com.example.storynest.Notification


import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storynest.CustomViews.ErrorDialog
import com.example.storynest.Notification.Adapter.NotificationAdapter
import com.example.storynest.R
import com.example.storynest.databinding.ProfileNotificationFragmentBinding

class NotificationFragment : Fragment() {
    private lateinit var viewModel: NotiViewModel
    private lateinit var adapter: NotificationAdapter
    private lateinit var soundPool: SoundPool

    private var acceptSoundId: Int = 0
    private var rejectSoundId: Int = 0



    private var _binding: ProfileNotificationFragmentBinding? = null
    private val binding get() = _binding!!

    val testItems = mutableListOf(
        FollowResponseDTO(
            id = 1L,
            requester = SimpleUserDTO(
                userId = 101L,
                username = "ahmet_y",
                profileURL = "https://i.pravatar.cc/150?img=3"
            ),
            requested = SimpleUserDTO(
                userId = 999L,
                username = "current_user",
                profileURL = null
            ),
            status = FollowRequestStatus.PENDING,
            date = "2025-12-16T14:30:00"
        ),
        FollowResponseDTO(
            id = 2L,
            requester = SimpleUserDTO(
                userId = 102L,
                username = "melis_k",
                profileURL = "https://i.pravatar.cc/150?img=5"
            ),
            requested = SimpleUserDTO(
                userId = 999L,
                username = "current_user",
                profileURL = null
            ),
            status = FollowRequestStatus.PENDING,
            date = "2025-12-16T13:10:00"
        ),
        FollowResponseDTO(
            id = 3L,
            requester = SimpleUserDTO(
                userId = 103L,
                username = "emre_dev",
                profileURL = "https://i.pravatar.cc/150?img=8"
            ),
            requested = SimpleUserDTO(
                userId = 999L,
                username = "current_user",
                profileURL = null
            ),
            status = FollowRequestStatus.PENDING,
            date = "2025-12-15T22:45:00"
        )
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ProfileNotificationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[NotiViewModel::class.java]

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
        rejectSoundId = soundPool.load(
            requireContext(),
            R.raw.reject,
            1
        )

        adapter = NotificationAdapter(
            onItemShown = { notificationId ->
                viewModel.markAsRead(notificationId)
            },
            onAccept = { item ->
                onAccept()
                viewModel.accept(item.notification.id)
            },
            onReject = { item ->
                onReject()
            }
        )
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter


        viewModel.getMyFollowPending()

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if(!error.isNullOrEmpty()){
                var errorDialog = ErrorDialog.newInstance(
                    title = "Bağlantı Hatası",
                    message = error
                )
                errorDialog.setOnOkClickListener{
                    viewModel.getMyFollowPending()
                }
                errorDialog.show(parentFragmentManager, "ErrorDialogTag")
            }
        }

        viewModel.rows.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
        }

    }


    private fun onAccept(){
        soundPool.play(
            acceptSoundId,
            1f,
            1f,
            1,
            0,
            1f
        )
    }

    private fun onReject(){
        soundPool.play(
            rejectSoundId,
            1f,
            1f,
            1,
            0,
            1f
        )

    }



}