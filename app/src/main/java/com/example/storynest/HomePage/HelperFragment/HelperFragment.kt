package com.example.storynest.HomePage.HelperFragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.storynest.ApiClient
import com.example.storynest.HomePage.HomePageRepo
import com.example.storynest.HomePage.HomePageViewModel
import com.example.storynest.HomePage.HomePageViewModelFactory
import com.example.storynest.HomePage.postResponse
import com.example.storynest.R
import com.example.storynest.dataLocal.UserPreferences

class HelperFragment :  Fragment() {
    private val homePageRepo by lazy { HomePageRepo(ApiClient.postApi) }

    private val viewModel: HomePageViewModel by activityViewModels() {
        HomePageViewModelFactory(homePageRepo)
    }
    private lateinit var imgBookCover: ImageView
    private lateinit var categories: TextView
    private lateinit var txtBookContent: TextView
    private lateinit var txtBookTitle: TextView

    companion object {
        private const val ARG_POST_CONTENTS = "post_content"
        private const val ARG_POST_CATEGORY ="post_categories"
        private const val ARG_POST_NAME ="post_name"
        private const val ARG_POST_COVERIMAGE="post_cover_image"

        fun newInstance(post: postResponse): HelperFragment {
            val fragment = HelperFragment()
            val args = Bundle()
            args.putString(ARG_POST_CONTENTS,post.contents)
            args.putString(ARG_POST_CATEGORY,post.categories)
            args.putString(ARG_POST_NAME,post.postName)
            args.putString(ARG_POST_COVERIMAGE, post.coverImage)

            fragment.arguments = args
            return fragment
        }
    }

    private var postContents: String? = null
    private var postCategory: String? = null
    private var postName: String? = null
    private var postCoverImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            postContents = bundle.getString(ARG_POST_CONTENTS)
            postCategory = bundle.getString(ARG_POST_CATEGORY)
            postName = bundle.getString(ARG_POST_NAME)
            postCoverImage = bundle.getString(ARG_POST_COVERIMAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_helper, container, false)
    }

    override fun onViewCreated(view:View,savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        imgBookCover=view.findViewById(R.id.imgBookCover)
        categories=view.findViewById(R.id.categories)
        txtBookContent=view.findViewById(R.id.txtBookContent)
        txtBookTitle=view.findViewById(R.id.txtBookTitle)


        val coverUrl = postCoverImage
        Glide.with(requireContext())
            .load(coverUrl)
            .into(imgBookCover)

        categories.text=postCategory
        txtBookContent.text=postContents
        txtBookTitle.text=postName

    }

}