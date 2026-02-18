package com.example.storynest.Comments

import android.content.ClipboardManager
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
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.ClipData
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import com.example.storynest.CustomViews.InfoMessage
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
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
    private lateinit var commentInputContainer: ConstraintLayout

    private lateinit var imgProfile: ImageView
    private lateinit var etComment: EditText
    private lateinit var btnSend: TextView

    private lateinit var replyLayout: ConstraintLayout
    private lateinit var txtReplyingTo: TextView
    private lateinit var btnCancelReply: ImageView

    private lateinit var commentforReply: commentResponse

    private lateinit var dragHandle: View


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
        commentInputContainer=view.findViewById(R.id.commentInputContainer)

        imgProfile=view.findViewById(R.id.imgProfile)
        etComment=view.findViewById(R.id.etComment)
        btnSend=view.findViewById(R.id.btnSend)

        replyLayout=view.findViewById(R.id.replyLayout)
        txtReplyingTo=view.findViewById(R.id.txtReplyingTo)
        btnCancelReply=view.findViewById(R.id.btnCancelReply)
        dragHandle=view.findViewById(R.id.dragHandle)



        setUpRecyclerView()
        setupLifecyle()
        clicks()
        viewModel.setPostId(postId)

    }
    override fun onStart() {
        super.onStart()

        val bottomSheet = (dialog as? BottomSheetDialog)
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return

        val behavior = BottomSheetBehavior.from(bottomSheet) as? ScrollAwareBottomSheetBehavior<View>
        behavior?.let {
            it.isFitToContents = false
            it.skipCollapsed = true
            it.expandedOffset = 0
            it.halfExpandedRatio=0.6f
            it.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        rvComment.isNestedScrollingEnabled = true
    }



    private fun goBar(){
        InfoMessage.show(this,"Yorum kopyalandı.")
    }
    private fun setUpRecyclerView() {
        commentAdapter = CommentsAdapter(object : CommentsAdapter.OnCommentInteractionListener {
            override fun onLikeClicked(commentId: Long) {
                viewModel.toggleLike(commentId)
            }

            override fun onLongClicked(
                commentId: Long,
                commentContents: String,
                userId: Long,
                postUserId: Long,
                anchorView: View,
                onPinnedAlready: (() -> Unit)?
            ) {
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.custom_layout_menu, null)

                val popupWindow = PopupWindow(
                    view,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true
                )

                popupWindow.isOutsideTouchable = true
                popupWindow.isFocusable = true
                popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val editItem = view.findViewById<LinearLayout>(R.id.item_edit)
                if(userId == UserStaticClass.userId) {
                    editItem.visibility = View.VISIBLE
                    editItem.setOnClickListener {
                        popupWindow.dismiss()
                        showUpdateDialog(commentId, commentContents)
                    }
                } else {
                    editItem.visibility = View.GONE
                }

                view.findViewById<LinearLayout>(R.id.itemcopy).setOnClickListener {
                    val textCopy=commentContents
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                    val clip = ClipData.newPlainText("comment", textCopy)
                    clipboard.setPrimaryClip(clip)
                    goBar()
                    popupWindow.dismiss()
                }
                val deleteItem=view.findViewById<LinearLayout>(R.id.item_delete)
                if(userId== UserStaticClass.userId) {
                    deleteItem.visibility=View.VISIBLE
                    view.findViewById<LinearLayout>(R.id.item_delete).setOnClickListener {
                        viewModel.deleteComment(commentId)
                        popupWindow.dismiss()
                    }
                }else{
                    deleteItem.visibility=View.GONE
                }
                val pinItem=view.findViewById<LinearLayout>(R.id.item_pin)
                  if(postUserId== UserStaticClass.userId) {
                      pinItem.visibility=View.VISIBLE
                      viewModel.pinComments(commentId)
                      popupWindow.dismiss()
                  }else{
                      pinItem.visibility=View.GONE
                  }

                view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                val popupWidth = view.measuredWidth
                val popupHeight = view.measuredHeight

                val location = IntArray(2)
                anchorView.getLocationOnScreen(location)
                val anchorX = location[0]
                val anchorY = location[1]+anchorView.height

                val xOffset = anchorView.width - popupWidth
                val yOffset = -popupHeight

                popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, anchorX + xOffset, anchorY + yOffset)
            }

            override fun onReplyClicked(comment: commentResponse) {
                commentforReply = comment
                replyLayout.visibility = View.VISIBLE
                txtReplyingTo.text = comment.parentCommentUsername
                btnCancelReply.setOnClickListener {
                    replyLayout.visibility = View.GONE
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

        commentAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        rvComment.smoothScrollToPosition(0)
                    }
                }
                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                    rvComment.smoothScrollToPosition(0)
                }

            }
        )


        val layoutManager = LinearLayoutManager(requireContext())
        rvComment.apply {
            this.layoutManager = layoutManager
            this.adapter = commentAdapter.withLoadStateFooter(
                footer = CommentLoadStateAdapter { commentAdapter.retry() }
            )
        }

    }

    private fun showUpdateDialog(commentId: Long, commentContents: String) {

        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_update_comment, null)

        val textInputLayout = view.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = view.findViewById<TextInputEditText>(R.id.editComment)
        val btnUpdate = view.findViewById<MaterialButton>(R.id.btnUpdate)
        val btnIptal = view.findViewById<MaterialButton>(R.id.btnIptal)

        editText.setText(commentContents)
        editText.setSelection(commentContents.length)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()

        dialog.show()

        fun updateButtonAndField() {
            val currentText = editText.text.toString()
            val trimmedCurrent = currentText.trim()
            val trimmedOriginal = commentContents.trim()

            val isChanged = trimmedCurrent.isNotEmpty() && trimmedCurrent != trimmedOriginal

            // Buton durumu
            btnUpdate.isEnabled = isChanged
            btnUpdate.alpha = if (isChanged) 1f else 0.5f
            btnUpdate.setTextColor(
                ContextCompat.getColor(requireContext(), if (isChanged) R.color.accept_blue else R.color.gray)
            )
            btnUpdate.strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    if (isChanged) R.color.accept_blue else R.color.gray
                )
            )

            if (!editText.isFocused) {
                textInputLayout.boxStrokeColor = ContextCompat.getColor(
                    requireContext(),
                    if (isChanged) R.color.accept_blue else R.color.gray
                )
                textInputLayout.defaultHintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), if (isChanged) R.color.accept_blue else R.color.gray)
                )
            }
        }

        updateButtonAndField()
        editText.addTextChangedListener { editable ->
            val currentText = editable.toString()
            if (currentText.length > 250) {
                editText.setText(currentText.substring(0, 250))
                editText.setSelection(250)
            }
            updateButtonAndField()
        }

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                textInputLayout.boxStrokeColor =
                    ContextCompat.getColor(requireContext(), R.color.black)
                textInputLayout.defaultHintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.black)
                )
                editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            } else {
                updateButtonAndField()
            }
        }

        btnUpdate.setOnClickListener {
            val newText = editText.text.toString().trim()
            if (newText != commentContents.trim() && newText.isNotEmpty()) {
                viewModel.updateComment(commentId, newText)
            }
            dialog.dismiss()
        }

        btnIptal.setOnClickListener {
            dialog.dismiss()
        }
    }


    private fun setupLifecyle(){
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
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Yorum eklenemedi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                launch {
                    viewModel.pinState.collect { result ->
                        if(result){
                            rvComment.smoothScrollToPosition(0)
                        }else{
                            Toast.makeText(
                                requireContext(),
                                "Yorum sabitlenemedi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                launch {
                    commentAdapter.loadStateFlow.collectLatest { loadStates ->

                        val isRefreshing = loadStates.refresh is LoadState.Loading
                        progressBar.visibility =
                            if (isRefreshing) View.VISIBLE else View.GONE

                        val isListEmpty =
                            loadStates.refresh is LoadState.NotLoading &&
                                    commentAdapter.itemCount == 0

                        val errorState = loadStates.source.refresh as? LoadState.Error
                            ?: loadStates.source.append as? LoadState.Error
                            ?: loadStates.source.prepend as? LoadState.Error

                        when {
                            errorState != null -> {
                                txtEmpty.visibility = View.VISIBLE
                                txtEmpty.text = "Bir hata oluştu: ${errorState.error.localizedMessage}"
                            }

                            isListEmpty -> {
                                txtEmpty.visibility = View.VISIBLE
                                txtEmpty.text = "Henüz yorum yapılmamış."
                            }

                            else -> {
                                txtEmpty.visibility = View.GONE
                            }
                        }
                    }
                }


            }
        }


    }

    private fun clicks(){
        Glide.with(requireContext())
            .load(UserStaticClass.ppfoto)
            .placeholder(R.drawable.account_circle_24)
            .error(R.drawable.account_circle_24)
            .circleCrop()
            .into(imgProfile)


        btnSend.isEnabled = false
        btnSend.alpha = 0.5f

        etComment.doOnTextChanged { text, _, _, _ ->
            val hasText = !text.isNullOrBlank()
            btnSend.isEnabled = hasText
            btnSend.alpha = if (hasText) 1.0f else 0.5f
        }

        btnSend.setOnClickListener {
            val commentText = etComment.text.toString().trim()

            if (commentText.isEmpty()) return@setOnClickListener

            if (commentText.length > 250) {
                etComment.error = "En fazla 250 karakter yazabilirsiniz"
                return@setOnClickListener
            }

            if (replyLayout.visibility == View.VISIBLE) {
                viewModel.addSubComment(postId, UserStaticClass.userId,commentText,commentforReply.comment_id)
            } else {

                viewModel.addComment(postId, UserStaticClass.userId,commentText,null)
            }

            etComment.text.clear()
            replyLayout.visibility = View.GONE
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
