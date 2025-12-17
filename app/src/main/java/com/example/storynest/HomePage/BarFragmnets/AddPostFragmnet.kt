package com.example.storynest.HomePage.BarFragmnets

import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.example.storynest.ApiClient
import com.example.storynest.HomePage.HomePageRepo
import com.example.storynest.HomePage.HomePageViewModel
import com.example.storynest.HomePage.HomePageViewModelFactory
import com.example.storynest.R
import com.example.storynest.ResultWrapper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.w3c.dom.Text

class AddPostFragmnet : Fragment() {
    private val homePageRepo by lazy { HomePageRepo(ApiClient.postApi) }

    private val viewModel: HomePageViewModel by activityViewModels() {
        HomePageViewModelFactory(homePageRepo)
    }

    private lateinit var edtStoryTitle: EditText
    private lateinit var imgCover: ImageView
    private lateinit var txtKategori: TextView
    private lateinit var txtSelectedCategories: TextView
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var edtStoryContent: EditText
    private lateinit var btnPublish: Button

    private var selectedImageUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_post_fragmnet, container, false)
    }
    override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)

        edtStoryTitle = view.findViewById(R.id.edtStoryTitle)
        imgCover = view.findViewById(R.id.imgCover)
        txtKategori = view.findViewById(R.id.txtKategori)
        txtSelectedCategories = view.findViewById(R.id.txtSelectedCategories)
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory)
        edtStoryContent = view.findViewById(R.id.edtStoryContent)
        btnPublish = view.findViewById(R.id.btnPublish)

        setupCategoryLimit()
        TextListener()
        setupTextWatchers()
        click()
        setupObservers()
    }
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imgCover.setImageURI(it)
        }
    }

    private fun click() {
        imgCover.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnPublish.setOnClickListener {
            val title = edtStoryTitle.text.toString().trim()
            val content = edtStoryContent.text.toString().trim()
            val categories=txtSelectedCategories.toString().trim()

            val lineCount = content.lines().size
            val isCategorySelected = chipGroupCategory.checkedChipIds.isNotEmpty()

            when {
                title.isEmpty() -> {
                    edtStoryTitle.error = "Başlık boş olamaz"
                }

                !isCategorySelected -> {
                    txtKategori.error = "Kategori seçmelisin"
                }

                lineCount < 5 -> {
                    edtStoryContent.error = "Metin en az 5 satır olmalı"
                }

                else -> {
                    viewModel.addPost(title,content,categories,selectedImageUri.toString())
                }
            }

        }
    }
    private fun setupTextWatchers() {
        edtStoryTitle.addTextChangedListener {
            edtStoryTitle.error = null
        }

        edtStoryContent.addTextChangedListener {
            edtStoryContent.error = null
        }
    }

    private fun setupCategoryLimit() {
        chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.size > 3) {
                val lastCheckedId = checkedIds.last()
                group.findViewById<Chip>(lastCheckedId).isChecked = false
                return@setOnCheckedStateChangeListener
            }

            val selectedCategories = checkedIds.map { id ->
                group.findViewById<Chip>(id).text.toString()
            }
            txtSelectedCategories.text =
                if (selectedCategories.isEmpty())
                    "Seçilen kategoriler: -"
                else
                    "Seçilen kategoriler: ${selectedCategories.joinToString(", ")}"
        }
    }
    private fun TextListener() {
        edtStoryContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s == null) return

                val lines = s.toString().lines()

                if (lines.size > 32) {
                    s.delete(s.lastIndexOf("\n"), s.length)
                }
            }
        })
    }
    private fun setupObservers(){
        viewModel.addPostResult.observe(viewLifecycleOwner){result->
            when(result){
                is ResultWrapper.Success -> {
                    Toast.makeText(requireContext(), "Post paylaşıldı!", Toast.LENGTH_SHORT).show()
                    resetForm()
                }
                is ResultWrapper.Error -> {
                    Toast.makeText(requireContext(), "Hata: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun resetForm() {
        edtStoryTitle.text.clear()
        edtStoryContent.text.clear()

        imgCover.setImageResource(0)

        txtSelectedCategories.text = "Seçilen kategoriler: -"
        txtKategori.error = null

        chipGroupCategory.clearCheck()
    }

}