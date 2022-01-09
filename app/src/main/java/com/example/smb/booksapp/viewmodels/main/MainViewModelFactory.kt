package com.example.smb.booksapp.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smb.booksapp.data.drawerFragments.TagsDataSource
import com.example.smb.booksapp.data.drawerFragments.UserDataSource
import com.example.smb.booksapp.data.drawerFragments.UserRepository
import com.example.smb.booksapp.data.main.MainDataSource
import com.example.smb.booksapp.data.main.MainRepository

class MainViewModelFactory : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                mainRepository = MainRepository(
                    dataSource = MainDataSource(),
                    tagsDataSource = TagsDataSource()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}