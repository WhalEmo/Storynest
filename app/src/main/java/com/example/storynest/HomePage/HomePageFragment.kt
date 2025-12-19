package com.example.storynest.HomePage

import android.media.Image
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.ApiClient
import com.example.storynest.R
import com.example.storynest.HomePage.BarFragmnets.AddPostFragmnet
import com.example.storynest.HomePage.HelperFragment.HelperFragment
import com.example.storynest.ResultWrapper
import com.example.storynest.dataLocal.UserPreferences

class HomePageFragment : Fragment() {
    private val homePageRepo by lazy { HomePageRepo(ApiClient.postApi) }
    private val viewModel: HomePageViewModel by activityViewModels() {
        HomePageViewModelFactory(homePageRepo)
    }
    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        recyclerViewPosts=view.findViewById(R.id.recyclerViewPosts)

        setupRecyclerView()
        setupObservers()
        viewModel.homePagePosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(emptyList(), object : PostAdapter.OnPostInteractionListener {
            override fun onLikeClicked(Id: Long) {
               viewModel.toggleLike(Id)
            }

            override fun onReadMoreClicked(post: postResponse) {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HelperFragment.newInstance(post))
                        .addToBackStack(null)
                        .commit()
            }
        })

        recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun setupObservers(){
        viewModel.homepageposts.observe(viewLifecycleOwner){result->
            when(result) {
                is ResultWrapper.Success -> {
                    postAdapter.updateList(result.data)
                }
                is ResultWrapper.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.PostsLike.observe(viewLifecycleOwner){result->
            when(result){
                is ResultWrapper.Success -> {
                }
                is ResultWrapper.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}