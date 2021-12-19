package com.example.smb.booksapp.data.main

import com.example.smb.booksapp.data.login.LoginDataSource
import com.example.smb.booksapp.data.model.Book
import com.google.firebase.auth.FirebaseUser
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.drawerFragments.TagsDataSource
import com.example.smb.booksapp.data.model.Author
import com.example.smb.booksapp.data.model.Tag

class MainRepository(val dataSource: MainDataSource, private val tagsDataSource: TagsDataSource) {

    private var _authors: MutableList<Author>
    val authors: MutableList<Author>
        get() = _authors

    var user: FirebaseUser? = null
        private set

    private var _tags: MutableList<Tag>
    val tags: MutableList<Tag>
        get() = _tags

    private var _books: MutableList<Book>
    val books: MutableList<Book>
        get() = _books

    lateinit var loaded: () -> Unit
    lateinit var booksLoaded: () -> Unit

    init {
        user = null
        _tags = mutableListOf()
        _books = mutableListOf()
        _authors = mutableListOf()
        tagsDataSource.getNormalTags {
            if (it is Result.Success) {
                for (tag in it.data)
                    _tags.add(Tag(tag))
            }
        }
        dataSource.getAuthors {
            if (it is Result.Success) {
                _authors = it.data
                loaded.invoke()
            }
        }
    }

    fun loadBooks(){
        dataSource.getBooks {
            if (it is Result.Success) {
                _books = it.data
                booksLoaded.invoke()
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return dataSource.isLogged()
    }

    fun logout() {
        dataSource.logout()
        user = null
    }

    fun loadUser(notLoggedCallback: () -> Unit) {
        user = dataSource.getUserDataAndSetupAuthStateListener {
            notLoggedCallback.invoke()
        }
    }

    fun addBook(book: Book, cllback: (Result<Unit>) -> Unit) {
        dataSource.addBookAsUser(book) {
            cllback(it)
        }
    }
}

