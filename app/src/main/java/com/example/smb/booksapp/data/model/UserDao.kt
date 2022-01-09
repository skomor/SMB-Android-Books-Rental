package com.example.smb.booksapp.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserDao(
    val email: String? = null,
    val description: String? = null,
    val locationLat: String? = null,
    val locationLog: String? = null
){

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "email" to email,
            "description" to description,
            "locationLat" to locationLat,
            "locationLog" to locationLog
        )
    }
}