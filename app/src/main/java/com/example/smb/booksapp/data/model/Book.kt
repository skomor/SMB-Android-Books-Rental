package com.example.smb.booksapp.data.model

import android.graphics.Bitmap
import android.net.Uri

data class Book(
    var name: String? = null,
    val description: String? = null,
    val author: String? = null,
    val pic: String? = null,
    val tags: List<Tag>? = null,
    val userId: String? = null,
    val bookId: String? = null,
    val lat: String? = null,
    val log: String? =null,
    var isRemoved: Boolean = false,
    val bookerId: String? =null
) {
}