package com.example.storynest.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.storynest.CustomViews.ErrorDialog
import com.example.storynest.R
import com.example.storynest.Settings.SettingsFragment
import com.example.storynest.databinding.MyProfileFragmentBinding


class MyProfileFragment : Fragment(){

    private var _binding: MyProfileFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    private var myProfile: MyProfile? = null

    companion object {
        fun newInstance(myProfile : MyProfile) : MyProfileFragment{
            val fragment = MyProfileFragment()
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
        viewModel.getMyProfile()

        viewModel.profile.observe(viewLifecycleOwner){ profile ->
            binding.username.text = profile.username
            binding.nameSurname.text = profile.name + " " + profile.surname
            binding.biography.text = profile.biography
            binding.followersCount.text = profile.followers.toString()
            binding.followingCount.text = profile.following.toString()
        }

        viewModel.error.observe(viewLifecycleOwner){ error ->
            if(!error.isNullOrEmpty()){
                var errorDialog = ErrorDialog.newInstance(
                    title = "Bağlantı Hatası",
                    message = error
                )
                errorDialog.setOnOkClickListener{
                    viewModel.getMyProfile()
                }
                errorDialog.show(parentFragmentManager, "ErrorDialogTag")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun settingsButtonAnimation(){
        binding.settingsButton.setOnClickListener {
            it.animate().rotationBy(360f).setDuration(300).start()
            val settingsFragment = SettingsFragment()

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left
                )
                .replace(R.id.fragmentContainer, settingsFragment)
                .addToBackStack(null)
                .commit()

        }
    }

}