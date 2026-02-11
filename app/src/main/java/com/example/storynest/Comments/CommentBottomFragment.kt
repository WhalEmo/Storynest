package com.example.storynest.Comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storynest.ApiClient
import com.example.storynest.R
import com.example.storynest.UiState
import com.example.storynest.dataLocal.UserStaticClass
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CommentBottomFragment: BottomSheetDialogFragment() {
    private val CommentRepo by lazy { CommentRepo(ApiClient.commentApi) }

    private val viewModel: CommentsViewModel by activityViewModels {
        CommentsViewModelFactory(CommentRepo)
    }
    private lateinit var rvComment: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var commentAdapter: CommentsAdapter
    private lateinit var txtEmpty: TextView
    private lateinit var commentinput: LinearLayout

    private lateinit var imgProfile: ImageView
    private lateinit var etComment: EditText
    private lateinit var btnSend: TextView

    private lateinit var replyinput: LinearLayout
    private lateinit var txtReplyingTo: TextView
    private lateinit var btnCancelReply: ImageView

    private lateinit var commentforReply: commentResponse

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
        rvComment=view.findViewById(R.id.rvComment)
        progressBar=view.findViewById(R.id.progressBar)
        txtEmpty=view.findViewById(R.id.txtEmpty)
        commentinput=view.findViewById(R.id.commentinput)

        imgProfile=view.findViewById(R.id.imgProfile)
        etComment=view.findViewById(R.id.etComment)
        btnSend=view.findViewById(R.id.btnSend)

        replyinput=view.findViewById(R.id.replyinput)
        txtReplyingTo=view.findViewById(R.id.txtReplyingTo)
        btnCancelReply=view.findViewById(R.id.btnCancelReply)

        setUpRecyclerView()
        setupObserves()
        clicks()
    }

    private fun setUpRecyclerView(){
        commentAdapter= CommentsAdapter(object : CommentsAdapter.OnCommentInteractionListener{
            override fun onLikeClicked(commentId: Long) {
                viewModel.toggleLike(commentId)
            }
            override fun onReplyClicked(comment: commentResponse) {
                commentforReply=comment
                replyinput.visibility=View.VISIBLE
                txtReplyingTo.text=comment.parentCommentUsername
                btnCancelReply.setOnClickListener {
                    replyinput.visibility= View.GONE
                }
            }

            override fun onViewReplys(
                commentId: Long,
                reset: Boolean,
                onResult: (List<commentResponse>) -> Unit
            ) {
                //viewModel.subCommentsGet(commentId, reset, onResult)
            }
        })

        val layoutManager = LinearLayoutManager(requireContext())
        rvComment.apply {
            this.layoutManager = layoutManager
            this.adapter = commentAdapter.withLoadStateFooter(
                footer = CommentLoadStateAdapter { commentAdapter.retry() }
            )
        }
        commentAdapter.addLoadStateListener { loadState ->
            progressBar.visibility = if (loadState.source.refresh is LoadState.Loading) View.VISIBLE else View.GONE
            val isListEmpty = loadState.source.refresh is LoadState.NotLoading && commentAdapter.itemCount == 0
             txtEmpty.visibility = if (isListEmpty) View.VISIBLE else View.GONE
            val errorState = loadState.source.refresh as? LoadState.Error
            if (errorState != null) {
                txtEmpty.visibility = View.VISIBLE
                txtEmpty.text = "İnternet bağlantısı yok veya bir hata oluştu."
            } else if (!isListEmpty) {
                txtEmpty.text = "Henüz yorum yapılmamış."
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getComments(postId).collectLatest { pagingData ->
                    commentAdapter.submitData(pagingData)
                }
            }
        }

    }
    private fun setupObserves(){


    }

    private fun clicks(){
        Glide.with(requireContext())
            .load(UserStaticClass.ppfoto)
            .placeholder(R.drawable.account_circle_24)
            .error(R.drawable.account_circle_24)
            .circleCrop()
            .into(imgProfile)

        btnSend.setOnClickListener {
            val commentText=etComment.text.toString()
            if (commentText.isEmpty()) return@setOnClickListener

            if (replyinput.visibility == View.VISIBLE) {
                viewModel.addSubComment(postId, UserStaticClass.userId,commentText,commentforReply.comment_id)
            } else {

                viewModel.addComment(postId, UserStaticClass.userId,commentText,null)
            }

            etComment.text.clear()
            replyinput.visibility = View.GONE
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
                else -> {}
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
