package com.example.smb.booksapp.data.drawerFragments

import android.util.Log
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.model.Tag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.IOException
import java.util.Arrays

import java.util.ArrayList

class TagsDataSource {
    private var db: DatabaseReference =
        FirebaseDatabase.getInstance("https://bookapp-0404-default-rtdb.europe-west1.firebasedatabase.app").reference;
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()


    fun getNormalTags(cllBck: (Result<MutableList<String>>) -> Unit) {
        db.child("tags").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var tags: MutableList<String> = mutableListOf()
                for (dsnap in snapshot.children) {
                    var asTag = dsnap.getValue(String::class.java)
                    if (asTag != null)
                        tags.add(asTag);
                }
                if (tags.count() == 0)
                    cllBck.invoke(Result.Error(IOException("Could not load base tags.")))
                else {
                    cllBck.invoke(Result.Success(tags))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "onCancelled ${error.message}")
                cllBck(Result.Error(IOException("Could not load base tags. Exe")))
            }
        })
    }

    fun getUserTags(myCallback: (result: Result<MutableList<String>>) -> Unit) {
        var currUser = auth.currentUser;
        if (currUser != null) {
            db.child("userTags").child(currUser.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var tags: MutableList<String> = mutableListOf()
                        for (dsnap in snapshot.children) {
                            var asTag = dsnap.getValue(String::class.java)
                            if (asTag != null)
                                tags.add(asTag)
                        }
                        myCallback.invoke(Result.Success(tags))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("firebase", "onCancelled ${error.message}")
                        myCallback(Result.Error(IOException("Could not load user adv data. Canceled ")))
                    }
                })
        }
    }

    fun saveUserTags(userTags: MutableList<Tag>, resultCallback:(Result<Unit>)-> Unit) {
        val currUser = auth.currentUser;
        val names:ArrayList<String> = arrayListOf();
        userTags.forEach {  names.add(it.name) }

        if (currUser != null) {
            db.child("userTags").child(currUser.uid).setValue(names).addOnSuccessListener {
                resultCallback(Result.Success(Unit));
            }.addOnCanceledListener {
                resultCallback(Result.Error(IOException("Could not save")));
            }
        }
        else{
            Log.e("MINEERROR","User NULL")
        }
    }
}