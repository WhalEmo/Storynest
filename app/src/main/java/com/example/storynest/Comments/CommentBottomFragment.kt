package com.example.storynest.Comments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.google.android.material.internal.ViewUtils.hideKeyboard
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
    private var shouldScrollToTop = false


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
        viewModel.setPostId(postId)
    }

    data class OptionItem(val title: String, val iconRes: Int, val action: () -> Unit)
    private fun setUpRecyclerView() {
        commentAdapter = CommentsAdapter(object : CommentsAdapter.OnCommentInteractionListener {
            override fun onLikeClicked(commentId: Long) {
                viewModel.toggleLike(commentId)
            }

            override fun onLongClicked(commentId: Long) {
                val options = listOf(
                    OptionItem("Sil", R.drawable.trash) { viewModel.deleteComment(commentId) },
                    OptionItem("Güncelle", R.drawable.edit) { showUpdateDialog(commentId) }
                )

                val adapter = object : ArrayAdapter<OptionItem>(
                    requireContext(),
                    R.layout.dialog_option_item,
                    options
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = convertView ?: LayoutInflater.from(context)
                            .inflate(R.layout.dialog_option_item, parent, false)
                        val item = options[position]
                        val title = view.findViewById<TextView>(R.id.title)
                        val icon = view.findViewById<ImageView>(R.id.icon)
                        title.text = item.title
                        icon.setImageResource(item.iconRes)
                        return view
                    }
                }
                AlertDialog.Builder(requireContext())
                    .setAdapter(adapter) { dialog, which ->
                        options[which].action()
                    }
                    .show()

            }

            override fun onReplyClicked(comment: commentResponse) {
                commentforReply = comment
                replyinput.visibility = View.VISIBLE
                txtReplyingTo.text = comment.parentCommentUsername
                btnCancelReply.setOnClickListener {
                    replyinput.visibility = View.GONE
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
            if (loadState.refresh is LoadState.NotLoading && shouldScrollToTop) {
                rvComment.scrollToPosition(0)
                shouldScrollToTop = false
            }
            progressBar.visibility =
                if (loadState.source.refresh is LoadState.Loading) View.VISIBLE else View.GONE
            val isListEmpty =
                loadState.source.refresh is LoadState.NotLoading && commentAdapter.itemCount == 0
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

                launch {
                    viewModel.comments.collectLatest { pagingData ->
                        commentAdapter.submitData(pagingData)
                    }
                }

                launch {
                    viewModel.commentAddState.collect { success ->
                        if (success) {
                            shouldScrollToTop = true
                            rvComment.post {
                                commentAdapter.refresh()
                            }

                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Yorum eklenemedi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            }
        }
    }

        private fun showUpdateDialog(commentId: Long) {
        val editText = EditText(requireContext())
        editText.hint = "Yorumu güncelle"

        AlertDialog.Builder(requireContext())
            .setTitle("Yorumu Düzenle")
            .setView(editText)
            .setPositiveButton("Güncelle") { dialog, _ ->
                val newText = editText.text.toString()
                if (newText.isNotBlank()) {
                    viewModel.updateComment(commentId, newText)
                }
                dialog.dismiss()
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
