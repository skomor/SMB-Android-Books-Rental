package com.example.smb.booksapp.data.drawerFragments

import android.net.Uri
import android.util.Log
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.model.UserDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.lang.Exception
import android.graphics.BitmapFactory

import android.graphics.Bitmap


class UserDataSource {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: DatabaseReference =
        FirebaseDatabase.getInstance("https://bookapp-0404-default-rtdb.europe-west1.firebasedatabase.app").reference;
    val storage = Firebase.storage("gs://bookapp-0404.appspot.com").reference.child("Images")

    fun getBasicUserData(): FirebaseUser? {
        return auth.currentUser;
    }

    fun getAdvUserData(myCallback: (result: Result<UserDao>) -> Unit) {
        val user = auth.currentUser;
        if (user != null) {
            val fromDb = db.child("users").child(user.uid)
            fromDb.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.e("firebase", "onDataChange ${snapshot.value.toString()}")
                    val post = snapshot.getValue<UserDao>()
                    if (post == null) {
                        myCallback(Result.Error(IOException("Could not load user adv data. Data null: ")))
                    } else {
                        myCallback(Result.Success(post))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase", "onCancelled ${error.message}")
                    myCallback(Result.Error(IOException("Could not load user adv data. Load Canceled!")))
                }
            })
        }
    }

    fun updateUser(name: String, desc: String, imageUri: Uri?, callback: (Result<Unit>) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            if (imageUri != null) {
                storage.child(user.uid).downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result != null)
                            forFileExists(user, callback, imageUri, name, desc)
                    } else
                        addNewImage(user, callback, imageUri, name, desc)
                }
            } else {
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                user.updateProfile(profileUpdates);
                db.child("users").child(user.uid).child("description").setValue(desc)
                    .addOnCompleteListener {
                        if (it.isSuccessful)
                            callback.invoke(Result.Success(Unit))
                        else
                            callback.invoke(Result.Error(Exception(it.result.toString())))
                    }
            }
        }
    }

    private fun forFileExists(
        user: FirebaseUser,
        callback: (Result<Unit>) -> Unit,
        imageUri: Uri,
        name: String,
        desc: String
    ) {
        storage.child(user.uid).delete().addOnCompleteListener {
            if (it.isSuccessful)
                addNewImage(user, callback, imageUri, name, desc);
            else
                callback.invoke(Result.Error(Exception(it.result.toString())))
        }
    }

    private fun addNewImage(
        user: FirebaseUser,
        callback: (Result<Unit>) -> Unit,
        imageUri: Uri,
        name: String,
        desc: String
    ) {
        var uploadTask = storage.child(user.uid).putFile(imageUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storage.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                user.updateProfile(profileUpdates);
                db.child("users").child(user.uid).child("description").setValue(desc)
                    .addOnCompleteListener {
                        if (it.isSuccessful)
                            callback.invoke(Result.Success(Unit))
                        else
                            callback.invoke(Result.Error(Exception(it.result.toString())))
                    }
            } else {
                callback.invoke(Result.Error(Exception("Could not save data")))
            }
        }
    }


    fun getUserPic(callback: (Result<Bitmap>) -> Unit) {
        var tmpFile = File.createTempFile("img", "png");

        if (auth.currentUser != null)
            storage.child(auth.currentUser!!.uid).getFile(tmpFile).addOnSuccessListener {
                val image = BitmapFactory.decodeFile(tmpFile.absolutePath)
                callback.invoke(Result.Success(image))
            }.addOnFailureListener {
                callback.invoke(Result.Error(Exception()))
            }
    }

}