package com.example.smb.booksapp.data.model

import com.google.firebase.database.Exclude
import java.util.ArrayList

data class BookDao(
    val name: String? = null,
    val description: String? = null,
    val author: String? = null,
    val pic: String? = null,
    val tags: ArrayList<String>? = null,
    val userId: String? = null
){

    @Exclude
    fun toHashMap(): Map<String, Any?> {
        return hashMapOf(
            "name" to name,
            "description" to description,
            "author" to author,
            "pic" to pic,
            "userId" to userId,
            "tags" to tags
        )
    }
}