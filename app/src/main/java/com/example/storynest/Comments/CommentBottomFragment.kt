package com.example.storynest.Comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.ApiClient
import com.example.storynest.R
import com.example.storynest.UiState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomFragment: BottomSheetDialogFragment() {
    private val CommentRepo by lazy { CommentRepo(ApiClient.commentApi) }

    private val viewModel: CommentsViewModel by activityViewModels {
        CommentsViewModelFactory(CommentRepo)
    }
    private lateinit var rvComment: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var commentAdapter: CommentsAdapter
    private lateinit var txtEmpty: TextView
    private var postId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = requireArguments().getLong(ARG_POST_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_comments, container, false)
    }

    override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        rvComment=view.findViewById(R.id.rvLikeUsers)
        progressBar=view.findViewById(R.id.progressBar)
        txtEmpty=view.findViewById(R.id.txtEmpty)

        setUpRecyclerView()
        setupObserves()
        viewModel.commentsGet(postId,reset = true)

    }

    private fun setUpRecyclerView(){
        commentAdapter= CommentsAdapter(object : CommentsAdapter.OnCommentInteractionListener{
            override fun onLikeClicked(commentId: Long) {
                viewModel.toggleLike(commentId)
            }
            override fun onReplyClicked(comment: commentResponse) {

            }

            override fun onViewReplys(
                commentId: Long,
                reset: Boolean,
                onResult: (List<commentResponse>) -> Unit
            ) {
                viewModel.subCommentsGet(commentId, reset, onResult)
            }
        })
        val layoutManager = LinearLayoutManager(requireContext())
        rvComment.apply {
            this.layoutManager = layoutManager
            adapter = commentAdapter
        }


        rvComment.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount=layoutManager.childCount
                val totalItemCount=layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if(!viewModel.isLoadingComment && !viewModel.isLastPageComment){
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0
                    ) {
                        viewModel.commentsGet(postId)
                    }
                }
            }
        })
    }
    private fun setupObserves(){
        observeUiState(viewModel.postComments,progressBar){ data ->
            if(data.isEmpty()){
                progressBar.visibility = View.GONE
                rvComment.visibility = View.GONE
                txtEmpty.visibility = View.VISIBLE
            }else {
                progressBar.visibility = View.GONE
                txtEmpty.visibility = View.GONE
                rvComment.visibility = View.VISIBLE
                commentAdapter.submitList(data)
            }
        }

        observeUiState(viewModel.postSubComments,progressBar){data->

        }
    }


    private fun <T> observeUiState(
        liveData: LiveData<UiState<T>>,
        progressBar: View,
        onSuccess: (T) -> Unit = {}
    ) {
        liveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    rvComment.visibility = View.GONE
                    txtEmpty.visibility = View.GONE
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    onSuccess(state.data)
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    rvComment.visibility = View.GONE
                    txtEmpty.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Hata: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                is UiState.EmailNotVerified->{
                }
                is UiState.EmailSent->{
                }
            }
        }
    }
    companion object {
        private const val ARG_POST_ID = "post_id"

        fun newInstance(postId: Long): CommentBottomFragment {
            return CommentBottomFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_POST_ID, postId)
                }
            }
        }
    }
}