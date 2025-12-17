package com.example.storynest.HomePage.BarFragmnets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.storynest.HomePage.HomePageViewModel
import com.example.storynest.R
import kotlin.getValue

class SearchFragment : Fragment() {

    companion object {
        fun newInstance() = AddPostFragmnet()
    }

    private val viewModel: HomePageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_post_fragmnet, container, false)
    }
}