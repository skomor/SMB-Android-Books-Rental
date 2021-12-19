package com.example.smb.booksapp.data.register

import com.example.smb.booksapp.data.Result
import com.google.firebase.auth.FirebaseUser

class RegisterRepository(val dataSource: RegisterDataSource) {


    fun register(
        username: String,
        password: String,
        nick:String,
        myCallback: (result: Result<FirebaseUser>) -> Unit
    ) {
        dataSource.register(username, password,nick, myCallback);
    }
}