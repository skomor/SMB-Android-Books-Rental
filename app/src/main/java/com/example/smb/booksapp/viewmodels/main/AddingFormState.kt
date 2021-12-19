package com.example.smb.booksapp.viewmodels.main

import java.io.FileDescriptor

data class AddingFormState (
    val pictureError: Int? = null,
    val authorError: Int? = null,
    val bookNameError: Int? = null,
    val tagsError: Int? = null,
    val descriptionError: Int? = null,
    val isDataValid: Boolean = false,
)