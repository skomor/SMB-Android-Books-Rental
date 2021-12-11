package com.example.smb.booksapp.viewmodels.login

import com.example.smb.booksapp.views.LoggedInUserView

data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)