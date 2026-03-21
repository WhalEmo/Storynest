package com.example.storynest.HomePage.UpdatePost

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.storynest.CustomViews.InfoMessage
import com.example.storynest.CustomViews.UiEvents
import com.example.storynest.HomePage.HomePageViewModel

import com.example.storynest.HomePage.postUiItem
import com.example.storynest.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.color.MaterialColors
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class UpdatePostFragmnets: Fragment() {
    private val viewModel: HomePageViewModel by activityViewModels()
    private lateinit var imgCover: ImageView
    private lateinit var changePhoto: LinearLayout
    private lateinit var edtUpdateTitle: EditText
    private lateinit var txtKategori: TextView
    private lateinit var txtSelectedCategories: TextView
    private lateinit var edtUpdateContent: TextInputEditText
    private lateinit var btnUpdatePost: MaterialButton
    private lateinit var btnCancelUpdate: MaterialButton
    private lateinit var imgUserProfile: ImageView
    private lateinit var txtUsername: TextView

    private lateinit var loadingOverlay: RelativeLayout
    private val secilenKategoriler = mutableSetOf<String>()
    private var postId: Long = -1L

/*
    private val postData: postUiItem? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_POST, postUiItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_POST)
        }
    }

 */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.update_post, container, false)
    }

    override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        imgCover=view.findViewById(R.id.imgCover)
        edtUpdateTitle=view.findViewById(R.id.edtUpdateTitle)
        txtKategori=view.findViewById(R.id.txtKategori)
        edtUpdateContent=view.findViewById(R.id.edtUpdateContent)
        imgUserProfile=view.findViewById(R.id.imgUserProfile)
        txtUsername=view.findViewById(R.id.txtUsername)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)

        changePhoto=view.findViewById(R.id.changePhoto)
        txtSelectedCategories=view.findViewById(R.id.txtSelectedCategories)
        btnUpdatePost=view.findViewById(R.id.btnUpdatePost)
        btnCancelUpdate=view.findViewById(R.id.btnCancelUpdate)

/*
        postData?.let { post ->
            postId=post.postId
            txtUsername.text = post.userName
            edtUpdateTitle.setText(post.postName)
            edtUpdateTitle.setSelection(edtUpdateTitle.text.length)

            if (!post.categories.isNullOrEmpty()) {
                val list = post.categories.split(",")
                secilenKategoriler.addAll(list)
                updateSelectedCategoriesText()
            }
            txtKategori.text=post.categories

            edtUpdateContent.setText(post.contents)
            edtUpdateContent.setSelection(edtUpdateTitle.text.length)

            Glide.with(requireContext())
                .load(post.profileUrl)
                .placeholder(R.drawable.account_circle_24)
                .error(R.drawable.account_circle_24)
                .circleCrop()
                .into(imgUserProfile)

            Glide.with(requireContext())
                .load(post.coverImage)
                .placeholder(R.drawable.outline_broken_image_24)
                .error(R.drawable.outline_broken_image_24)
                .into(imgCover)

        }

 */
        listenData()
        textListener()
        setupTextWatchers()
        clicks()
    }

    private fun clicks(){
        txtKategori.setOnClickListener {
            showCategoryBottomSheet()
        }
        btnUpdatePost.setOnClickListener {
            val title = edtUpdateTitle.text.toString().trim()
            val content = edtUpdateContent.text.toString().trim()
            val categoriesString = secilenKategoriler.joinToString(",")

            val minChars = 100
            val maxChars = 2000

            when {
                title.isEmpty() -> {
                    edtUpdateTitle.error = "Başlık boş olamaz"
                }
                content.length < minChars -> {
                    edtUpdateContent.error = "Metin en az $minChars karakter olmalı"
                }
                content.length > maxChars -> {
                    edtUpdateContent.error = "Metin en fazla $maxChars karakter olabilir"
                }
                secilenKategoriler.isEmpty() -> {
                    txtSelectedCategories.error = "En az bir kategori seçmelisin"
                }
                else -> {
                    edtUpdateContent.error = null
                   // viewModel.updatePost(postId,title,content,categoriesString,postData?.coverImage)
                }
            }
        }
    }

    private fun setupTextWatchers() {
        edtUpdateTitle.addTextChangedListener {
            edtUpdateTitle.error = null
        }

        edtUpdateContent.addTextChangedListener {
            edtUpdateContent.error = null
        }
    }

    private fun textListener() {
        val maxChars = 2000

        edtUpdateContent.filters = arrayOf(InputFilter.LengthFilter(maxChars))
        edtUpdateContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0

                if (currentLength >= maxChars) {
                    edtUpdateContent.error = "Metin en fazla $maxChars karakter olabilir!"
                } else {
                    edtUpdateContent.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun showCategoryBottomSheet() {

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_categories, null)

        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupCategories)
        val btnOnayla = view.findViewById<MaterialButton>(R.id.btnOnayla)

        val categories = listOf(
            "Korku", "Aksiyon", "Aşk", "Fantastik", "Bilim Kurgu", "Tümünü kaldır"
        )

        categories.forEach { category ->
            val chip = Chip(requireContext())
            chip.text = category

            if (category == "Tümünü kaldır") {
                chip.isCheckable = false
                chip.setOnClickListener {

                    secilenKategoriler.clear()

                    for (i in 0 until chipGroup.childCount) {
                        val otherChip = chipGroup.getChildAt(i) as Chip
                        otherChip.isChecked = false
                    }

                    updateSelectedCategoriesText()
                }

            } else {
                chip.isCheckable = true
                chip.isChecked = secilenKategoriler.contains(category)

                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        secilenKategoriler.add(category)
                    } else {
                        secilenKategoriler.remove(category)
                    }
                    updateSelectedCategoriesText()
                }
            }

            chipGroup.addView(chip)
        }

        btnOnayla.setOnClickListener {
            updateSelectedCategoriesText()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun resetForm() {
        edtUpdateTitle.text.clear()
        edtUpdateContent.text?.clear()
        imgCover.setImageResource(0)
        txtSelectedCategories.text = ""
        txtSelectedCategories.error = null
        txtKategori.error = null
        secilenKategoriler.clear()
    }
    private fun updateSelectedCategoriesText() {
        txtSelectedCategories.error = null
        val text = if (secilenKategoriler.isEmpty()) {
            ""
        } else {
            secilenKategoriler.joinToString(" , ")
        }
        txtSelectedCategories.text = text
    }

    private fun listenData(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.updateSucces.collect {
                        resetForm()
                        parentFragmentManager.popBackStack()
                    }
                }
                launch {
                    viewModel.uiEvent.collect { event ->
                        when(event){
                            is UiEvents.ShowSnackbar -> {

                            }
                            is UiEvents.ShowToast -> {}
                            is UiEvents.ShowUndoSnackbar -> {}
                            is UiEvents.showInfoMessage -> {
                                InfoMessage.show(
                                    fragment = this@UpdatePostFragmnets,
                                    message = event.message
                                )
                            }
                        }

                    }
                }
            }
        }

    }

    companion object {
        private const val ARG_POST = "post"

        fun newInstance(post: postUiItem): UpdatePostFragmnets {
            return UpdatePostFragmnets().apply {
                arguments = Bundle().apply {
                    //putParcelable(ARG_POST, post)
                }
            }
        }
    }
}