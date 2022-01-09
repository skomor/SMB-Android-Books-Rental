package com.example.smb.booksapp.viewmodels.drawerFragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smb.booksapp.data.drawerFragments.TagsDataSource
import com.example.smb.booksapp.data.drawerFragments.TagsRepository

class TagsViewModelFactory : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagsViewModel::class.java)) {
            return TagsViewModel(
                tagsRepository = TagsRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}