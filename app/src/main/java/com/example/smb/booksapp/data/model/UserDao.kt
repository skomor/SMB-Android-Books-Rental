package com.example.smb.booksapp.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserDao(
    val email: String? = null,
    val description: String? = null,
    val location: String? = null
)