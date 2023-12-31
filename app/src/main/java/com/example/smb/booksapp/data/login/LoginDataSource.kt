package com.example.smb.booksapp.data.login

import com.example.smb.booksapp.data.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.IOException

class LoginDataSource {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(username: String,
              password: String,
              myCallback: (res: Result<FirebaseUser>)-> Unit
    ) {
        try {
            auth.signInWithEmailAndPassword(username, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null)
                        myCallback.invoke(Result.Success(user))
                } else {
                    myCallback.invoke(Result.Error(IOException("Error logging in: " + task.exception)))
                }
            }
        } catch (e: Throwable) {
            myCallback( Result.Error(IOException("Error logging in", e)))
        }
    }

    fun iSLoggedIn(): Boolean{
        return auth.currentUser != null;
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrUser(): FirebaseUser? {
        return auth.currentUser
    }
}