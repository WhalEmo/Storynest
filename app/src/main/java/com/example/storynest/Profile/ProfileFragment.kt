package com.example.storynest.Profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.storynest.CustomViews.InfoMessage
import com.example.storynest.Follow.FollowType
import com.example.storynest.databinding.MyProfileFragmentBinding


class ProfileFragment : Fragment(){

    private var _binding: MyProfileFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    private var myProfile: MyProfile? = null

    companion object {
        fun newInstance(myProfile : MyProfile) : ProfileFragment{
            val fragment = ProfileFragment()
            val bundle = Bundle()
            bundle.putSerializable("myprofile", myProfile)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myProfile = arguments?.getSerializable("myprofile") as MyProfile?

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MyProfileFragmentBinding.inflate(inflater, container, false)
        binding.username.text = myProfile?.username
        binding.nameSurname.text = myProfile?.name + " " + myProfile?.surname
        binding.biography.text = myProfile?.biography
        binding.followersCount.text = myProfile?.followers.toString()
        binding.followingCount.text = myProfile?.following.toString()
        binding.profileImage.load(myProfile?.profile){
            crossfade(true)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        settingsButtonAnimation()

        binding.notificationBook.setOnClickListener {
            val action =
                ProfileFragmentDirections
                    .actionProfileToNotification()
            findNavController().navigate(action)
        }

        binding.followersContainer.setOnClickListener {

            val action =
                ProfileFragmentDirections
                    .actionProfileToFollowList(
                        FollowType.MY_FOLLOWERS.name,
                        0
                    )

            findNavController().navigate(action)
        }


        viewModel.getMyProfile()

        viewModel.profile.observe(viewLifecycleOwner){ profile ->
            binding.username.text = profile.username
            binding.nameSurname.text = profile.name + " " + profile.surname
            binding.biography.text = profile.biography
            binding.followersCount.text = profile.followers.toString()
            binding.followingCount.text = profile.following.toString()
            binding.profileImage.load(profile.profile){
                crossfade(true)
            }
        }

        viewModel.error.observe(viewLifecycleOwner){ error ->
            if(!error.isNullOrEmpty()){
                InfoMessage.show(
                    requireActivity(),
                    error
                )
                /*
                var errorDialog = ErrorDialog.newInstance(
                    title = "Bağlantı Hatası",
                    message = error
                )
                errorDialog.setOnOkClickListener{
                    viewModel.getMyProfile()
                }
                errorDialog.show(parentFragmentManager, "ErrorDialogTag")*/
            }
        }
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
                    val action =
                        ProfileFragmentDirections
                            .actionProfileToSettings()
                    findNavController().navigate(action)
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


}