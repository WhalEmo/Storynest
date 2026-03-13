package com.example.storynest.HomePage
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar

import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.Comments.CommentBottomFragment
import com.example.storynest.CustomViews.InfoMessage
import com.example.storynest.CustomViews.UiEvents
import com.example.storynest.R
import com.example.storynest.HomePage.HelperFragment.HelperFragment
import com.example.storynest.HomePage.PostLikeUser.LikeUsersBottomSheet
import com.example.storynest.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomePageFragment : Fragment() {
    private val viewModel: HomePageViewModel by activityViewModels()
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
        setupLifecycle()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(object : PostAdapter.OnPostInteractionListener {
            override fun onLikeClicked(post: postUiItem,likeView:View) {
                val animation = AnimationUtils.loadAnimation(context, R.anim.pop_heart)
                likeView.startAnimation(animation)
                likeView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                viewModel.toggleLike(post,isCurrentlyLiked = post.liked,
                    currentLikeCount = post.numberof_likes.toIntOrNull() ?: 0)
            }

            override fun onReadMoreClicked(post: postUiItem) {
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

            override fun clickComment(postId: Long,commentPinnedCount:Long) {
                CommentBottomFragment
                    .newInstance(postId,commentPinnedCount)
                    .show(parentFragmentManager,"CommentBottomFragment")
            }

        })

        val layoutManager = LinearLayoutManager(requireContext())

        recyclerViewPosts.apply {
            this.layoutManager = layoutManager
            adapter = postAdapter
        }

    }

    private fun setupLifecycle() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is UiEvents.ShowToast -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                            }

                            is UiEvents.showInfoMessage -> {
                                InfoMessage.show(
                                    fragment = this@HomePageFragment,
                                    message = event.message
                                )
                            }

                            is UiEvents.ShowSnackbar -> {

                            }
                        }
                    }
                    launch {
                        viewModel.posts.collectLatest { pagingData ->
                            postAdapter.submitData(pagingData)
                        }
                    }
                }
            }
        }
    }

}