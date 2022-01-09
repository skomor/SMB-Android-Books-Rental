package com.example.smb.booksapp.data.drawerFragments

import android.util.Log
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.model.Tag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList

class TagsRepository() {

    private var db: DatabaseReference =
        FirebaseDatabase.getInstance("https://bookapp-0404-default-rtdb.europe-west1.firebasedatabase.app").reference;
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()


    private lateinit var _tags: MutableList<String>
    private var _combinedTags: MutableList<Tag> = mutableListOf()
    private lateinit var _userTags: MutableList<String>
    lateinit var dataLoadedCallback: (Result<Boolean>) -> Unit
    lateinit var userDataLoadedCallback: (Result<Boolean>) -> Unit

    var combinedTags: MutableList<Tag>? = null
        get() = _combinedTags

    init {
        getNormalTags()
    }

    private fun getNormalTags() {
        db.child("tags").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var tags: MutableList<String> = mutableListOf()
                for (dsnap in snapshot.children) {
                    var asTag = dsnap.getValue(String::class.java)
                    if (asTag != null)
                        tags.add(asTag);
                }
                if (tags.count() == 0){
                    dataLoadedCallback.invoke(Result.Error(IOException("Could not load base tags.")))
                }
                else {
                    _tags = tags
                    dataLoadedCallback.invoke(Result.Success(true))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "onCancelled ${error.message}")
                dataLoadedCallback(Result.Error(IOException("Could not load base tags. Reason: Cancelled, ${error.message}")))
            }
        })
    }

    fun getUserTags() {
        val currUser = auth.currentUser;
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
                        _userTags = tags
                        _combinedTags.clear()
                        for (tag in _tags) {
                            if (_userTags.contains(tag)) {
                                _combinedTags.add(Tag(tag, true))
                            } else {
                                _combinedTags.add(Tag(tag, false))
                            }
                        }
                        userDataLoadedCallback.invoke(Result.Success(true))
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("firebase", "onCancelled ${error.message}")
                        userDataLoadedCallback(Result.Error(IOException("Could not load user adv data. Canceled ")))
                    }
                })
        }
    }

    fun saveUserTags(values: MutableList<Tag>, resultCallback:(Result<Unit>)-> Unit) {
        val userTags = mutableListOf<Tag>();
        values.forEach {
            if (it.isUsers)
                userTags.add(it)
        }
        val currUser = auth.currentUser;
        val names: ArrayList<String> = arrayListOf();
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