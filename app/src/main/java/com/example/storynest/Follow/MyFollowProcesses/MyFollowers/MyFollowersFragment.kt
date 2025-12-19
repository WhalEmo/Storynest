package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.storynest.Follow.MyFollowProcesses.MyFollowers.Adapter.FollowersAdapter
import com.example.storynest.databinding.MyFollowersFragmentBinding

class MyFollowersFragment: Fragment() {

    private var _binding: MyFollowersFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FollowersAdapter

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
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = FollowersAdapter(
            onAccept = {

            },
            onReject = {

            }
        )


    }

}