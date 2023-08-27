package com.example.smb.booksapp.viewmodels.main

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smb.booksapp.R
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.drawerFragments.TagsRepository
import com.example.smb.booksapp.data.main.MainRepository
import com.example.smb.booksapp.data.model.Author
import com.example.smb.booksapp.data.model.Book
import com.example.smb.booksapp.data.model.Tag

class MainViewModel(
	private val mainRepository: MainRepository,
	private val tagsRepo: TagsRepository
) : ViewModel()
{

	private lateinit var _callback: () -> Unit
	var userTagsLoaded: () -> Unit = {}

	private val _tags = MutableLiveData<MutableList<Tag>>().apply {
		value = mutableListOf()
	}
	private val _authors = MutableLiveData<MutableList<Author>>().apply {
		value = mutableListOf()
	}
	private val _books = MutableLiveData<MutableList<Book>>().apply {
		value = mutableListOf()
	}
	private val _userBooks = MutableLiveData<MutableList<Book>>().apply {
		value = mutableListOf()
	}
	private val _userBookedBooks = MutableLiveData<MutableList<Book>>().apply {
		value = mutableListOf()
	}

	private val _addingForm = MutableLiveData<AddingFormState>()
	val addingFormState: LiveData<AddingFormState> = _addingForm

	private val _addingResult = MutableLiveData<Result<Unit>>()
	val addingResult: LiveData<Result<Unit>> = _addingResult

	val tags: LiveData<MutableList<Tag>> = _tags
	val authors: LiveData<MutableList<Author>> = _authors
	val books: LiveData<MutableList<Book>> = _books
	val userBooks: LiveData<MutableList<Book>> = _userBooks
	val userBookedBooks: LiveData<MutableList<Book>> = _userBookedBooks

	init
	{
		_tags.value = tagsRepo.combinedTags;
		_authors.value = mainRepository.authors;
		//_books.value = mainRepository.books;

		mainRepository.authorsLoaded = {
			_authors.value = mainRepository.authors;
		}
		tagsRepo.userDataLoadedCallback = {
			_tags.value = tagsRepo.combinedTags;
			userTagsLoaded.invoke()
		}
		mainRepository.booksLoaded = {
			if (it is Result.Success)
				_books.value = mainRepository.books;
		}
		tagsRepo.userDataLoadedCallback = {
			_tags.value = tagsRepo.combinedTags!!
			userTagsLoaded.invoke()
		}
		tagsRepo.updatedUserTags = {
			userTagsLoaded.invoke()
		}
	}

	fun setAvailabilityAndBooker(book: Book, isAvailable: Boolean, callback: (Result<Unit>) -> Unit)
	{
		mainRepository.setBookAvailabilityAndBooker(
			book.userId!!,
			book.bookId!!,
			isAvailable,
			callback
		)
	}

	fun reloadTags()
	{
		tagsRepo.getNormalTags()
	}

	fun loadBooksByTags()
	{
		mainRepository.getBooksByTags(_tags.value!!)
	}

	fun loadAuthors()
	{
		mainRepository.getAuthors()
	}

	fun isLogged(): Boolean
	{
		return mainRepository.isLoggedIn()
	}

	fun setupListenerForSignOut(callback: () -> Unit)
	{
		_callback = callback
		mainRepository.loadUser {
			callback.invoke()
		}
	}

	fun addBook(book: Book, imageUri: Uri?)
	{
		mainRepository.addBook(book, imageUri) { result ->
			_addingResult.value = result
		}
	}

	fun logout()
	{
		mainRepository.logout()
	}

	fun bookAddingDataChanged(
		bookName: String,
		description: String,
		author: String,
		listOfTags: List<Tag>,
		picUri: Uri?
	)
	{
		if (bookName.length < 2)
		{
			_addingForm.value = AddingFormState(bookNameError = R.string.BookTitleError)
		}
		else if (description.length < 6)
		{
			_addingForm.value = AddingFormState(descriptionError = R.string.DescriptionError)
		}
		else if (author.length < 4)
		{
			_addingForm.value = AddingFormState(authorError = R.string.AuthorNameError)
		}
		else if (listOfTags.count() < 1)
		{
			_addingForm.value = AddingFormState(tagsError = R.string.TagsError)
		}
		else if (picUri == null)
		{
			_addingForm.value = AddingFormState(pictureError = R.string.picError)
		}
		else
		{
			_addingForm.value = AddingFormState(isDataValid = true)
		}
	}

	fun getUserAddedBooks()
	{
		mainRepository.getUserAddedBooks {
			_userBooks.value = mainRepository.userBooks
		}
	}

	fun removeUserBook(clickedItem: Book, callback: (Result<Unit>) -> Unit)
	{
		mainRepository.removeUserBook(clickedItem, callback)
	}

	fun getUserBookedBooks()
	{
		mainRepository.getUserBookedBooks {
			_userBookedBooks.value = mainRepository.userBookedBooks
		}
	}

	fun unbookBook(clickedItem: Book, function: (Result<Unit>) -> Unit)
	{
		mainRepository.unsetBookAvailabilityAndBooker(clickedItem.bookId!!, function)
	}
}