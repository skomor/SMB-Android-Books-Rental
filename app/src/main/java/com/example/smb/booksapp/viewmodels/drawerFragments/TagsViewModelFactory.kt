package com.example.smb.booksapp.viewmodels.drawerFragments

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smb.booksapp.data.drawerFragments.TagsRepository

class TagsViewModelFactory(val context: Context) : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagsViewModel::class.java)) {
            return TagsViewModel(
                tagsRepository = TagsRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}