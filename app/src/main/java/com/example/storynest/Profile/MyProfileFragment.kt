package com.example.storynest.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import com.example.storynest.databinding.MyProfileFragmentBinding


class MyProfileFragment : Fragment(){

    private var _binding: MyProfileFragmentBinding? = null
    private val binding get() = _binding!!

    private var myProfile : MyProfile? = null

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
        println("Fragment Message")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}