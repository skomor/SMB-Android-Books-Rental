package com.example.smb.booksapp.data.main

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.IOException
import java.util.ArrayList
import kotlin.coroutines.cancellation.CancellationException
import android.graphics.BitmapFactory

import java.io.BufferedInputStream

import java.net.URL

import android.graphics.Bitmap


class MainDataSource {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: DatabaseReference =
        FirebaseDatabase.getInstance("https://bookapp-0404-default-rtdb.europe-west1.firebasedatabase.app").reference;
    val storage = Firebase.storage("gs://bookapp-0404.appspot.com").reference.child("Images")

    fun isLogged(): Boolean {
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
    }

    fun getAuthors(cllBck: (Result<MutableList<Author>>) -> Unit) {
        db.child("authors").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var authors: MutableList<Author> = mutableListOf()
                for (dsnap in snapshot.children) {
                    var author = dsnap.getValue(Author::class.java)
                    if (author != null)
                        authors.add(author);
                }
                if (authors.count() == 0)
                    cllBck.invoke(Result.Error(IOException("Could not load base tags.")))
                else {
                    cllBck.invoke(Result.Success(authors))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "onCancelled ${error.message}")
                cllBck(Result.Error(CancellationException("Could not load base tags. Exe")))
            }
        })
    }

    fun addBookAsUser(book: Book, callback: (Result<Unit>) -> Unit) {
        addImageToStorage(book) {
            if (it is Result.Success) {
                val key = db.child("userBooks").push().key
                val user = auth.currentUser;

                val names: ArrayList<String> = arrayListOf();
                try {
                    book.tags!!.forEach { names.add(it.name) }
                } catch (e: Exception) {
                    callback.invoke(Result.Error(e));
                }
                if (user != null) {
                    val useruid = user.uid
                    var bookDao = BookDao(
                        book.name,
                        book.description,
                        book.author,
                        it.data.toString(),
                        names,
                        useruid
                    )
                    val updates: MutableMap<String, Any> = HashMap()
                    updates["users/$useruid/books/$key"] = bookDao
                    updates["userBooks/$key"] = bookDao
                    db.updateChildren(updates).addOnCompleteListener { yt ->
                        if (yt.isSuccessful)
                            callback.invoke(Result.Success(Unit))
                        else if (yt.isCanceled)
                            callback.invoke(Result.Error(CancellationException("canceled")))
                        else if (yt.exception != null) {
                            callback.invoke(Result.Error(yt.exception!!))
                        }
                    }
                }
            }else{
                callback(Result.Error(Exception(it.toString())))
            }
        }
    }

    private fun addImageToStorage(book: Book, callback: (Result<Uri>) -> Unit) {
        storage.child(book.name + auth.currentUser?.uid).putFile(book.pic!!).addOnSuccessListener {
            storage.child(book.name + auth.currentUser?.uid).downloadUrl.addOnCompleteListener() { taskSnapshot ->

                var url = taskSnapshot.result
                if (url != null)
                    callback.invoke(Result.Success(url))
                else
                    callback.invoke(Result.Error(Exception("Could not get url back")))

            }.addOnFailureListener{
                callback.invoke(Result.Error(Exception("some error")))
            }
    }
}

fun getUserDataAndSetupAuthStateListener(notLoggedCallback: () -> Unit): FirebaseUser? {
    val user = auth.currentUser
    if (user != null) {
        db.child("users").child(user.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue() == null)
                        db.child("users").child(user.uid).setValue(UserDao(user.email))

                }

                override fun onCancelled(error: DatabaseError) {
                    db.child("users").child(user.uid).setValue(UserDao(user.email))
                }
            })
    }

    auth.addAuthStateListener {
        if (it.currentUser == null)
            notLoggedCallback.invoke();
    }
    return auth.currentUser;
}
    private fun getImageBitmap(url: String): Bitmap? {
        var bm: Bitmap? = null
        try {
            val aURL = URL(url)
            val conn = aURL.openConnection()
            conn.connect()
            val `is` = conn.getInputStream()
            val bis = BufferedInputStream(`is`)
            bm = BitmapFactory.decodeStream(bis)
            bis.close()
            `is`.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error getting bitmap", e)
        }
        return bm
    }

    fun getBooks(callback: (Result<MutableList<Book>>) -> Unit) {
    db.child("userBooks")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val books: MutableList<Book> = mutableListOf()

                for (dsnap in snapshot.children) {
                    val book = dsnap.getValue(BookDao::class.java)
                    if (book != null) {
                        val tags = mutableListOf<Tag>()
                        book.tags?.forEach { tags.add(Tag(it)) }
                        if(book.pic != null){
                            var bmap = getImageBitmap(book.pic)
                            books.add(Book(book.name, book.description, book.author,
                                Uri.parse(book.pic),   bmap , tags,book.userId,dsnap.key))
                        }
                    }
                }

                if (books.count() == 0)
                    callback.invoke(Result.Error(IOException("Could not load base tags.")))
                else {
                    callback.invoke(Result.Success(books))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "onCancelled ${error.message}")
                callback(Result.Error(IOException("Could not load base tags. Exe")))
            }
        })
}
}