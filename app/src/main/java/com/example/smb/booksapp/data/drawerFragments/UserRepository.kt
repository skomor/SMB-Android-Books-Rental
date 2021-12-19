package com.example.smb.booksapp.data.drawerFragments

import android.graphics.Bitmap
import android.net.Uri
import com.example.smb.booksapp.data.Result

import com.example.smb.booksapp.data.model.UserDao
import com.google.firebase.auth.FirebaseUser
import java.io.IOException

class UserRepository(private val dataSource: UserDataSource) {


    fun setData(name: String, desc: String, imageUri: Uri?, callback: (Result<Unit>) -> Unit) {
        dataSource.updateUser(name,desc,imageUri,callback);
    }

    fun getUserPic(callback: (Result<Bitmap>) -> Unit) {
        dataSource.getUserPic(callback);
    }

    var user: FirebaseUser? = null
        private set

    var userAdv: UserDao? = null
        private set

    lateinit var dataLoadedCallback: (Result<Boolean>) -> Unit

    init {
        user = null
        userAdv = null
        user = dataSource.getBasicUserData()
        dataSource.getAdvUserData { result ->
            if (result is Result.Success) {
                userAdv = result.data
                dataLoadedCallback(Result.Success(true))
            } else {
                dataLoadedCallback(Result.Error(IOException(result.toString())))
            }
        }
    }


}