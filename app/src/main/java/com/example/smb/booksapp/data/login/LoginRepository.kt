package com.example.smb.booksapp.data.login

import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.model.LoggedInUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginRepository(val dataSource: LoginDataSource) {


    var user: FirebaseUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun login(
        username: String,
        password: String,
        myCallback: (result: Result<FirebaseUser>) -> Unit
    ) {
        dataSource.login(username, password) { result ->
            if (result is Result.Success) {
                setLoggedInUser(result.data)
            }
            myCallback(result)
        }
    }

    private fun setLoggedInUser(loggedInUser: FirebaseUser) {
        this.user = loggedInUser
    }
}