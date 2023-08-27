package com.example.smb.booksapp.viewmodels.drawerFragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smb.booksapp.data.drawerFragments.UserRepository

class UserInfoViewModelFactory  : ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserInfoViewModel::class.java)) {
            return UserInfoViewModel(
                userRepository = UserRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}