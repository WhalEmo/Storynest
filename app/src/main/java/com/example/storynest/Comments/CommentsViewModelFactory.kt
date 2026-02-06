package com.example.storynest.Comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CommentsViewModelFactory(
    private val repo: CommentRepo
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommentsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}