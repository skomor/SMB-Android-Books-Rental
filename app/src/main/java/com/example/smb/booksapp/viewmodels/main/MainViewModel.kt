package com.example.smb.booksapp.viewmodels.main

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smb.booksapp.R
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.main.MainRepository
import com.example.smb.booksapp.data.model.Author
import com.example.smb.booksapp.data.model.Book
import com.example.smb.booksapp.data.model.Tag

class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {

    private lateinit var _callback: () -> Unit

    private val _tags = MutableLiveData<MutableList<Tag>>().apply {
        value = mutableListOf()
    }
    private val _authors = MutableLiveData<MutableList<Author>>().apply {
        value = mutableListOf()
    }
    private val _books = MutableLiveData<MutableList<Book>>().apply {
        value = mutableListOf()
    }
    val tags: LiveData<MutableList<Tag>> = _tags
    val authors: LiveData<MutableList<Author>> = _authors
    val books: LiveData<MutableList<Book>> = _books

    init{

        _tags.value = mainRepository.tags;
        _authors.value = mainRepository.authors;
        _books.value = mainRepository.books;

        mainRepository.loaded = {
            _tags.value = mainRepository.tags;
            _authors.value = mainRepository.authors;
        }
        mainRepository.booksLoaded = {
            _books.value = mainRepository.books;
        }
    }

    fun loadBooks(){
        mainRepository.loadBooks()
    }

    private val _addingForm = MutableLiveData<AddingFormState>()
    val addingFormState: LiveData<AddingFormState> = _addingForm

    private val _addingResult = MutableLiveData<Result<Unit>>()
    val addingResult: LiveData<Result<Unit>> = _addingResult

    fun isLogged(): Boolean {
        return mainRepository.isLoggedIn()
    }

    fun setupListenerForSignOut(callback: () -> Unit) {
        _callback = callback
        mainRepository.loadUser {
            callback.invoke()
        }
    }

    fun addBook(book: Book) {
        mainRepository.addBook(book) { result ->
            _addingResult.value = result
        }
    }

    fun logout() {
        mainRepository.logout()
    }

    fun bookAddingDataChanged(
        bookName: String,
        description: String,
        author: String,
        listOfTags: List<Tag>,
        picUri: Uri?
    ) {
        if (bookName.length < 2) {
            _addingForm.value = AddingFormState(bookNameError = R.string.BookTitleError)
        } else if (description.length < 6) {
            _addingForm.value = AddingFormState(descriptionError = R.string.DescriptionError)
        } else if (author.length < 4) {
            _addingForm.value = AddingFormState(authorError = R.string.AuthorNameError)
        } else if (listOfTags.count() < 1) {
            _addingForm.value = AddingFormState(tagsError = R.string.TagsError)
        } else if (picUri == null) {
            _addingForm.value = AddingFormState(pictureError = R.string.picError)
        } else {
            _addingForm.value = AddingFormState(isDataValid = true)
        }
    }
}