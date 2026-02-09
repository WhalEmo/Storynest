package com.example.storynest.HomePage

import android.media.Image
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar

import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.ApiClient
import com.example.storynest.Comments.CommentBottomFragment
import com.example.storynest.R
import com.example.storynest.HomePage.HelperFragment.HelperFragment
import com.example.storynest.HomePage.PostLikeUser.LikeUsersBottomSheet
import com.example.storynest.ResultWrapper
import com.example.storynest.UiState

class HomePageFragment : Fragment() {
    private val homePageRepo by lazy { HomePageRepo(ApiClient.postApi) }
    private val viewModel: HomePageViewModel by activityViewModels() {
        HomePageViewModelFactory(homePageRepo)
    }
    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var generalProgressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        recyclerViewPosts=view.findViewById(R.id.recyclerViewPosts)
        generalProgressBar=view.findViewById(R.id.generalProgressBar)

        setupRecyclerView()
        setupObservers()
        viewModel.homePagePosts(reset = true)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(object : PostAdapter.OnPostInteractionListener {
            override fun onLikeClicked(Id: Long) {
                viewModel.toggleLike(Id)
            }

            override fun onReadMoreClicked(post: postResponse) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HelperFragment.newInstance(post))
                    .addToBackStack(null)
                    .commit()
            }

            override fun getLikeUsers(postId: Long) {
                LikeUsersBottomSheet
                    .newInstance(postId)
                    .show(parentFragmentManager, "LikeUsersBottomSheet")
            }

            override fun clickComment(postId: Long) {
                CommentBottomFragment
                    .newInstance(postId)
                    .show(parentFragmentManager,"CommentBottomFragment")
            }

        })

        val layoutManager = LinearLayoutManager(requireContext())

        recyclerViewPosts.apply {
            this.layoutManager = layoutManager
            adapter = postAdapter
        }

        recyclerViewPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!viewModel.isLoadingHome && !viewModel.isLastPageHome) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0
                    ) {
                        viewModel.homePagePosts()
                    }
                }
            }
        })
    }

    private fun setupObservers(){
        observeUiState(viewModel.homepagePosts, generalProgressBar) { data ->
            postAdapter.submitList(data)
        }
       viewModel.postsLike.observe(viewLifecycleOwner){ postsLikeList ->
       }

    }
    private fun <T> observeUiState(
        liveData: LiveData<UiState<T>>,
        progressBar: View,
        onSuccess: (T) -> Unit = {}
    ) {
        liveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    onSuccess(state.data)
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Hata: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                is UiState.EmailNotVerified->{
                }
                is UiState.EmailSent->{
                }
            }
        }
    }
}