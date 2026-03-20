package com.example.storynest.HomePage
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView

import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.AppError
import com.example.storynest.Comments.CommentBottomFragment
import com.example.storynest.Comments.CommentLoadStateAdapter
import com.example.storynest.CustomViews.InfoMessage
import com.example.storynest.CustomViews.UiEvents
import com.example.storynest.R
import com.example.storynest.HomePage.HelperFragment.HelperFragment
import com.example.storynest.HomePage.PostLikeUser.LikeUsersBottomSheet
import com.example.storynest.HomePage.UpdatePost.UpdatePostFragmnets
import com.example.storynest.dataLocal.UserStaticClass
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomePageFragment : Fragment() {
    private val viewModel: HomePageViewModel by activityViewModels()
    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var generalProgressBar: ProgressBar

    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var shimmerRecyclerView: RecyclerView
    private lateinit var txtEmpty: TextView
    private lateinit var btnRetry: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        recyclerViewPosts=view.findViewById(R.id.recyclerViewPosts)
        shimmerLayout=view.findViewById(R.id.shimmerLayout)
        shimmerRecyclerView=view.findViewById(R.id.shimmerRecyclerView)
        txtEmpty=view.findViewById(R.id.txtEmpty)
        btnRetry=view.findViewById(R.id.btnRetry)
        generalProgressBar=view.findViewById(R.id.generalProgressBar)

        shimmerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        shimmerRecyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post_placeholder, parent, false)
                return object : RecyclerView.ViewHolder(v) {}
            }
            override fun getItemCount() = 10
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
        }

        setupRecyclerView()
        setupLifecycle()
    }

    private fun goBar(message:String){
        InfoMessage.show(this,message)
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
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    .replace(R.id.fragment_container, HelperFragment.newInstance(post))
                    .addToBackStack(null)
                    .commit()
            }

            override fun getLikeUsers(postId: Long) {
                LikeUsersBottomSheet
                    .newInstance(postId)
                    .show(parentFragmentManager, "LikeUsersBottomSheet")
            }

            override fun clickComment(postId: Long, commentPinnedCount: Long) {
                CommentBottomFragment
                    .newInstance(postId,commentPinnedCount)
                    .show(parentFragmentManager,"CommentBottomFragment")
            }

            override fun clickMenu(post: postUiItem,anchorView: View) {
                val inflater= LayoutInflater.from(context)
                val view= inflater.inflate(R.layout.post_custom_menu,null)
                val popupWindow = PopupWindow(
                    view,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true
                )
                popupWindow.isOutsideTouchable = true
                popupWindow.isFocusable = true
                popupWindow.setTouchInterceptor { _, event ->
                    if (event.action == MotionEvent.ACTION_OUTSIDE) {
                        endPopupAnimation(popupWindow, view)
                        true
                    } else {
                        false
                    }
                }
                popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val deleteItem= view.findViewById<LinearLayout>(R.id.post_delete)
                if(post.userId== UserStaticClass.userId){
                    deleteItem.visibility=View.VISIBLE
                    deleteItem.setOnClickListener {
                        viewModel.deletePosts(post)
                        endPopupAnimation(popupWindow,view)
                    }
                }else{
                    deleteItem.visibility=View.GONE
                }
                val updateItem= view.findViewById<LinearLayout>(R.id.post_edit)
                if(post.userId== UserStaticClass.userId){
                    updateItem.visibility=View.VISIBLE
                    updateItem.setOnClickListener {
                       showUpdateDialog()
                        endPopupAnimation(popupWindow,view)
                    }
                }else{
                    updateItem.visibility=View.GONE
                }

                val gapInDp = -24
                val gapInPx = (gapInDp * anchorView.resources.displayMetrics.density).toInt()

                view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                val popupWidth = view.measuredWidth


                val xOffset = anchorView.width - popupWidth

                val yOffset = gapInPx
                popupWindow.showAsDropDown(anchorView, xOffset, yOffset)
                startPopupAnimation(view)
            }
        })

        val layoutManager = LinearLayoutManager(requireContext())

        recyclerViewPosts.apply {
            this.layoutManager = layoutManager
            this.adapter = postAdapter.withLoadStateFooter(
                footer = CommentLoadStateAdapter { postAdapter.retry() }
            )
            (itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        }

    }
    private fun updateDialog(postUi: postUiItem){
        UpdatePostFragmnets
            .newInstance(postUi)
            .show(parentFragmentManager,"UpdateFragment")
    }
    private fun startPopupAnimation(view: View){
        view.alpha=0f
        view.scaleX=0.85f
        view.scaleY=0.85f
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(180)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }
    private fun endPopupAnimation(popupWindow: PopupWindow,view: View){
        view.animate()
            .alpha(0f)
            .scaleX(0.85f)
            .scaleY(0.85f)
            .setDuration(180)
            .withEndAction {
                popupWindow.dismiss()
            }
            .start()
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

                            is UiEvents.ShowUndoSnackbar -> {
                                val view = view ?: return@collect

                                Snackbar.make(view, event.message, Snackbar.LENGTH_LONG)
                                    .setAction("Geri al") {
                                        viewModel.undoDelete()
                                    }
                                    .addCallback(object : Snackbar.Callback() {
                                        override fun onDismissed(transientBottomBar: Snackbar?, eventType: Int) {
                                            if (eventType != DISMISS_EVENT_ACTION && eventType != DISMISS_EVENT_CONSECUTIVE) {
                                                viewModel.confirmDelete()
                                            }
                                        }
                                    }).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.posts.collectLatest { pagingData ->
                        postAdapter.submitData(pagingData)
                    }
                }
                launch {
                    postAdapter.loadStateFlow.collectLatest { loadStates ->
                        val refreshState = loadStates.refresh
                        val isItemEmpty = postAdapter.itemCount == 0

                        when (refreshState) {
                            is LoadState.Loading -> {
                                if (isItemEmpty && shimmerLayout.visibility != View.VISIBLE) {
                                    shimmerLayout.startShimmer()
                                    shimmerLayout.visibility = View.VISIBLE
                                    recyclerViewPosts.visibility = View.GONE
                                    txtEmpty.visibility = View.GONE
                                    btnRetry.visibility = View.GONE
                                }
                            }
                            is LoadState.NotLoading -> {
                                if (isItemEmpty) {
                                    if (loadStates.append.endOfPaginationReached) {
                                        recyclerViewPosts.visibility = View.GONE
                                        shimmerLayout.stopShimmer()
                                        shimmerLayout.visibility = View.GONE
                                        txtEmpty.visibility = View.VISIBLE
                                        txtEmpty.text = "Henüz gönderi paylaşılmamış."
                                    }
                                } else {
                                    if (shimmerLayout.visibility == View.VISIBLE) {
                                        shimmerLayout.stopShimmer()
                                        shimmerLayout.visibility = View.GONE
                                    }

                                    recyclerViewPosts.visibility = View.VISIBLE
                                    txtEmpty.visibility = View.GONE
                                    btnRetry.visibility = View.GONE
                                }
                            }

                            is LoadState.Error -> {
                                shimmerLayout.stopShimmer()
                                shimmerLayout.visibility = View.GONE

                                val error = refreshState.error
                                if (isItemEmpty) {
                                    txtEmpty.visibility = View.VISIBLE
                                    btnRetry.visibility = View.VISIBLE
                                    txtEmpty.text = error.localizedMessage
                                    recyclerViewPosts.visibility = View.GONE
                                } else {
                                    goBar(error.localizedMessage ?: "Bir hata oluştu")
                                }
                            }
                        }
                    }
                }


            }
        }
    }

}