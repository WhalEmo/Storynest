package com.example.storynest.Comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.ApiClient
import com.example.storynest.R
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
        viewModel.commentsGet(postId,reset = true)

    }

    private fun setUpRecyclerView(){
        commentAdapter= CommentsAdapter()
        val layoutManager = LinearLayoutManager(requireContext())
        rvComment.apply {
            this.layoutManager = layoutManager
            adapter = commentAdapter
        }

    }

    companion object {
        private const val ARG_POST_ID = "post_id"

        fun newInstance(postId: Long): LikeUsersBottomSheet {
            return LikeUsersBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong(ARG_POST_ID, postId)
                }
            }
        }
    }


}