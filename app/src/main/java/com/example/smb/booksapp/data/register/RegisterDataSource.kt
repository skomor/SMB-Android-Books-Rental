package com.example.smb.booksapp.data.register

import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.model.UserDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest

import java.io.IOException
import java.lang.Exception

class RegisterDataSource {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun register(
        username: String,
        password: String,
        nick:String,
        myCallback: (result: Result<FirebaseUser>) -> Unit
    ) {

        try {
            auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null){
                        val profileUpdates = userProfileChangeRequest {
                            displayName = nick
                        }
                        user.updateProfile(profileUpdates);
                        myCallback.invoke(Result.Success(user))
                    }
                    }
                else{
                    throw Exception(task.result.toString())
                }
            }
        } catch (e: Throwable) {
            myCallback(Result.Error(IOException("Error, can't register:", e)))
        }
    }
}