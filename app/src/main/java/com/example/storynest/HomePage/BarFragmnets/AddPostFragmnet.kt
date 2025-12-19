package com.example.storynest.HomePage.BarFragmnets

import android.net.Uri
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
import com.bumptech.glide.Glide
import com.example.storynest.ApiClient
import com.example.storynest.HomePage.HomePageRepo
import com.example.storynest.HomePage.HomePageViewModel
import com.example.storynest.HomePage.HomePageViewModelFactory
import com.example.storynest.R
import com.example.storynest.ResultWrapper
import com.example.storynest.dataLocal.UserPreferences
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class AddPostFragmnet : Fragment() {
    private val homePageRepo by lazy { HomePageRepo(ApiClient.postApi) }

    private val viewModel: HomePageViewModel by activityViewModels() {
        HomePageViewModelFactory(homePageRepo)
    }

    private lateinit var edtStoryTitle: EditText
    private lateinit var imgCover: ImageView
    private lateinit var txtKategori: TextView
    private lateinit var txtSelectedCategories: TextView
    private lateinit var edtStoryContent: EditText
    private lateinit var btnPublish: Button
    private lateinit var textkapakekle: TextView

    private val secilenKategoriler = mutableSetOf<String>()
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
        edtStoryContent = view.findViewById(R.id.edtStoryContent)
        btnPublish = view.findViewById(R.id.btnPublish)
        textkapakekle=view.findViewById(R.id.textkapakekle)

        textListener()
        setupTextWatchers()
        click()
        setupObservers()
    }
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(requireContext())
                .load(it)
                .into(imgCover)

            textkapakekle.visibility= View.GONE
        }
    }

    private fun click() {
        imgCover.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        txtKategori.setOnClickListener {
            showCategoryBottomSheet()
        }

        btnPublish.setOnClickListener {
            val title = edtStoryTitle.text.toString().trim()
            val content = edtStoryContent.text.toString().trim()
            val categoriesString = secilenKategoriler.joinToString(",")
            val lineCount = content.lines().size

            when {
                title.isEmpty() -> {
                    edtStoryTitle.error = "Başlık boş olamaz"
                }


                lineCount < 5 -> {
                    edtStoryContent.error = "Metin en az 5 satır olmalı"
                }
               secilenKategoriler.isEmpty() ->{
                   txtSelectedCategories.error = "En az bir kategori seçmelisin"

               }
                else -> {
                    viewModel.addPost(title,content,categoriesString,selectedImageUri.toString())
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

    private fun textListener() {
        edtStoryContent.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                edtStoryContent.post {
                    val layout = edtStoryContent.layout ?: return@post
                    val lineCount = layout.lineCount
                    if(lineCount>31){
                        edtStoryContent.error = "Metin en fazla 32 satır olabilir!"
                    }
                    if (lineCount > 32) {
                        s?.delete(s.length - 1, s.length)
                    }else if(lineCount< 5){
                        edtStoryContent.error = null
                    }
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
                    print(result.message)
                    Toast.makeText(requireContext(), "Hata: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
    private fun updateSelectedCategoriesText() {
        txtSelectedCategories.error = null
        val text = if (secilenKategoriler.isEmpty()) {
            ""
        } else {
            secilenKategoriler.joinToString(", ")
        }
        txtSelectedCategories.text = text
    }



    private fun resetForm() {
        edtStoryTitle.text.clear()
        edtStoryContent.text.clear()
        imgCover.setImageResource(0)
        txtSelectedCategories.text = ""
        txtSelectedCategories.error = null
        txtKategori.error = null
        secilenKategoriler.clear()
        selectedImageUri = null
        textkapakekle.visibility = View.VISIBLE

    }

}