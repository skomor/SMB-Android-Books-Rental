package com.example.smb.booksapp.viewmodels.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smb.booksapp.data.drawerFragments.TagsRepository
import com.example.smb.booksapp.data.main.MainRepository

class MainViewModelFactory(val context: Context) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                mainRepository = MainRepository(
                ),
                tagsRepo = TagsRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}