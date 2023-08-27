package com.example.smb.booksapp.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserDao(
    val email: String? = null,
    val description: String? = null,
    val lat: String? = null,
    val logn: String? = null
){

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "email" to email,
            "description" to description,
            "lat" to lat,
            "logn" to logn
        )
    }
}