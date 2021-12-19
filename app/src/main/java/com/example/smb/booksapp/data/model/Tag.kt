package com.example.smb.booksapp.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Tag (var name :String ="", var isUsers:Boolean = false){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
        )
    }
}

