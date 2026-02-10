package com.example.storynest.HomePage.PostLikeUser


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.ApiClient
import com.example.storynest.HomePage.HomePageRepo
import com.example.storynest.HomePage.HomePageViewModel
import com.example.storynest.HomePage.HomePageViewModelFactory
import com.example.storynest.HomePage.UserResponse
import com.example.storynest.R
import com.example.storynest.UiState

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.getValue

class LikeUsersBottomSheet: BottomSheetDialogFragment()  {
    private val homePageRepo by lazy { HomePageRepo(ApiClient.postApi) }
    private val viewModel: HomePageViewModel by activityViewModels() {
        HomePageViewModelFactory(homePageRepo)
    }
    private lateinit var rvLikeUsers: RecyclerView
    private lateinit var Likeadapter: LikeUsersAdapter
    private lateinit var progressBar: ProgressBar
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
        return inflater.inflate(R.layout.bottomsheet_like_users, container, false)
    }

    override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        rvLikeUsers=view.findViewById(R.id.rvLikeUsers)
        progressBar=view.findViewById(R.id.progressBar)
        txtEmpty=view.findViewById(R.id.txtEmpty)

        setUpRecyclerView()
        setupObserves()
        viewModel.getUsersWhoLike(postId,reset = true)
    }
    private fun setUpRecyclerView(){
        Likeadapter= LikeUsersAdapter(object : LikeUsersAdapter.OnUserInteractionListener{
            override fun onFollowClicked(Id: Long) {

            }

            override fun onMessageClicked(User: UserResponse) {
                //mesajlar fragmentına yonlendırcek
            }

            override fun onSendingClicked(Id: Long) {

            }

            override fun onLayoutClicked(Id: Long) {
                //profil fragmneta yonlendırıcek
            }

        })

        val layoutManager = LinearLayoutManager(requireContext())
        rvLikeUsers.apply {
            this.layoutManager = layoutManager
            adapter = Likeadapter
        }

        rvLikeUsers.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount=layoutManager.childCount
                val totalItemCount=layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if(!viewModel.isLoadingUser && !viewModel.isLastPageUser){
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0
                    ) {
                        viewModel.getUsersWhoLike(postId)
                    }
                }
            }
        })
    }
    private fun setupObserves(){
        viewModel.usersWhoLike.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    rvLikeUsers.visibility = View.GONE
                    txtEmpty.visibility = View.GONE

                }is UiState.Success -> {
                    if(state.data.isEmpty()){
                        progressBar.visibility = View.GONE
                        rvLikeUsers.visibility = View.GONE
                        txtEmpty.visibility = View.VISIBLE
                    }else {
                        progressBar.visibility = View.GONE
                        txtEmpty.visibility = View.GONE
                        rvLikeUsers.visibility = View.VISIBLE
                        Likeadapter.submitList(state.data)
                    }
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    rvLikeUsers.visibility = View.GONE
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

        fun newInstance(postId: Long): LikeUsersBottomSheet {
            return LikeUsersBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong(ARG_POST_ID, postId)
                }
            }
        }
    }


}