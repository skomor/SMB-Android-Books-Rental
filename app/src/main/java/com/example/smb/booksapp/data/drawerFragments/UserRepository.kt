package com.example.smb.booksapp.data.drawerFragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

class UserRepository() {

    var user: FirebaseUser? = null
        private set

    var userAdv: UserDao? = null
        private set
    var userImageAddress: Uri?

    lateinit var dataLoadedCallback: (Result<Boolean>) -> Unit

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: DatabaseReference =
        FirebaseDatabase.getInstance("https://bookapp-0404-default-rtdb.europe-west1.firebasedatabase.app").reference;
    val storage = Firebase.storage("gs://bookapp-0404.appspot.com").reference.child("Images")

    init {
        user = null
        userAdv = null
        user = getBasicUserData()
        userImageAddress = user?.photoUrl;
        getAdvUserData()
    }

    private fun getBasicUserData(): FirebaseUser? {
        return auth.currentUser;
    }

    private fun getAdvUserData() {
        val user = auth.currentUser;
        if (user != null) {
            val fromDb = db.child("users").child(user.uid)
            fromDb.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val post = snapshot.getValue<UserDao>()
                    if (post == null) {
                        dataLoadedCallback(Result.Error(IOException("Could not load user adv data. Data null: ")))
                    } else {
                        userAdv = post
                        dataLoadedCallback(Result.Success(true))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase", "onCancelled ${error.message}")
                    dataLoadedCallback(Result.Error(IOException("Could not load user adv data. Load Canceled")))
                }
            })
        }
    }

    fun updateUser(
        name: String, desc: String, imageUri: Uri?, locationLat: String?,
        locationLog: String?, callback: (Result<Unit>) -> Unit
    ) {
        val firebaseUser = user
        if (firebaseUser != null) {
            if (imageUri != null && imageUri != firebaseUser.photoUrl) {
                storage.child(firebaseUser.photoUrl.toString()).downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result != null)
                            removePreviousImage() {
                                if (it is Result.Success)
                                    addNewImage(imageUri) {
                                        if (it is Result.Success)
                                            updateUserAdvData(
                                                name,
                                                desc,
                                                it.data,
                                                locationLat,
                                                locationLog,
                                                callback
                                            )
                                    }
                            }
                    } else
                        addNewImage(imageUri) {
                            if (it is Result.Success)
                                updateUserAdvData(
                                    name,
                                    desc,
                                    it.data,
                                    locationLat,
                                    locationLog,
                                    callback
                                )
                        }
                }
            } else {
                updateUserAdvData(name, desc, null, locationLat, locationLog, callback)
            }
        }
    }

    private fun removePreviousImage(
        callback: (Result<Unit>) -> Unit
    ) {
        val firebaseUser = user

        storage.child(firebaseUser?.photoUrl.toString()).delete().addOnCompleteListener {
            if (it.isSuccessful)
                callback.invoke(Result.Success(Unit));
            else
                callback.invoke(Result.Error(Exception(it.result.toString())))
        }
    }

    private fun addNewImage(
        imageUri: Uri, callback: (Result<Uri>) -> Unit
    ) {
        val firebaseUser = user
        if (firebaseUser != null) {
            val ref = storage.child(firebaseUser.uid)
            val uploadTask = ref.putFile(imageUri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.invoke(Result.Success(task.result))
                } else {
                    callback.invoke(Result.Error(Exception("Could not save data")))
                }
            }
        }
    }

    private fun updateUserAdvData(
        name: String, desc: String, picUri: Uri?, locationLat: String?,
        locationLog: String?, callback: (Result<Unit>) -> Unit
    ) {
        val firebaseUser = user

        val childUpdates: HashMap<String, Any>
        if (locationLat != null && locationLog != null && locationLat != "" && locationLog != "") {
            childUpdates = hashMapOf<String, Any>(
                "description" to desc,
                "logn" to locationLog,
                "lat" to locationLat,
            )
        } else {
            childUpdates = hashMapOf<String, Any>(
                "description" to desc,
            )
        }

        val profileUpdates = userProfileChangeRequest {
            displayName = name
            photoUri = picUri
        }
        firebaseUser?.updateProfile(profileUpdates);
        if (firebaseUser != null) {
            db.child("users").child(firebaseUser.uid).updateChildren(childUpdates)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        callback.invoke(Result.Success(Unit))
                    else
                        callback.invoke(Result.Error(Exception(it.result.toString())))
                }
        }
    }

    fun getUserPic(callback: (Result<Bitmap>) -> Unit) {
        var tmpFile = File.createTempFile("img", "png");

        if (auth.currentUser != null)
            storage.child(userImageAddress.toString()).getFile(tmpFile).addOnSuccessListener {
                val image = BitmapFactory.decodeFile(tmpFile.absolutePath)
                callback.invoke(Result.Success(image))
            }.addOnFailureListener {
                callback.invoke(Result.Error(Exception()))
            }
    }

}