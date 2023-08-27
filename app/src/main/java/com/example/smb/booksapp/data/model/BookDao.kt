package com.example.smb.booksapp.data.model

import com.google.firebase.database.Exclude
import java.util.ArrayList

data class BookDao(
    val name: String? = null,
    val description: String? = null,
    val author: String? = null,
    val pic: String? = null,
    val tags: HashMap<String, Boolean>? = null,
    val userId: String? = null,
    val lat: String? = null,
    val log: String? =null,
    @field:JvmField
    var isAvailable: Boolean = false,
    @field:JvmField
    var isRemoved: Boolean = false,
    val bookerId: String? =null
){

    @Exclude
    fun toHashMap(): Map<String, Any?> {
        return hashMapOf(
            "name" to name,
            "description" to description,
            "author" to author,
            "pic" to pic,
            "userId" to userId,
            "tags" to tags,
            "lat" to lat,
            "log" to log,
            "isAvailable" to isAvailable,
            "isRemoved" to isRemoved,
            "bookerId" to bookerId,
        )
    }
}