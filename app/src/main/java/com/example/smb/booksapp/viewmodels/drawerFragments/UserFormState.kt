package com.example.smb.booksapp.viewmodels.drawerFragments

    data class UserFormState(
        val nickError: Int? = null,
        val descriptionError: Int? = null,
        val picError: Int? = null,
        val locationError: Int? = null,
        val isDataValid: Boolean = false
    )