package com.example.smb.booksapp.data.main

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.drawerFragments.TagsRepository
import com.example.smb.booksapp.data.model.*
import com.google.android.gms.drive.events.CompletionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.CancellationException

import com.google.firebase.database.DataSnapshot

import com.google.firebase.database.ValueEventListener


class MainRepository()
{
	private var auth: FirebaseAuth = FirebaseAuth.getInstance()
	private var db: DatabaseReference =
		FirebaseDatabase.getInstance("https://bookapp-0404-default-rtdb.europe-west1.firebasedatabase.app").reference;
	private val storage =
		Firebase.storage("gs://bookapp-0404.appspot.com").reference.child("Images")

	fun logout()
	{
		auth.signOut()
		user = null
	}

	private var _authors: MutableList<Author>
	val authors: MutableList<Author>
		get() = _authors

	var user: FirebaseUser? = null
		private set

	private var _books: MutableList<Book>
	val books: MutableList<Book>
		get() = _books

	private var _userBooks: MutableList<Book>
	val userBooks: MutableList<Book>
		get() = _userBooks
	private var _userBookedBooks: MutableList<Book>
	val userBookedBooks: MutableList<Book>
		get() = _userBookedBooks

	lateinit var authorsLoaded: () -> Unit
	lateinit var booksLoaded: (Result<Unit>) -> Unit

	init
	{
		user = null
		_books = mutableListOf()
		_userBookedBooks = mutableListOf()
		_userBooks = mutableListOf()
		_authors = mutableListOf()
	}

	fun isLoggedIn(): Boolean
	{
		return auth.currentUser != null
	}

	fun loadUser(notLoggedCallback: () -> Unit)
	{
		user = getUserDataAndSetupAuthStateListener {
			notLoggedCallback.invoke()
		}
	}

	fun getBooksByTags(combinedTags: MutableList<Tag>)
	{
		var count = 0;
		_books.clear()
		var desiredCount = combinedTags.count { it.isUsers }
		for (tag in combinedTags)
		{
			if (tag.isUsers)
			{
				val fromPath = db.child("userBooks").orderByChild("tags/${tag.name}").equalTo(true)
				fromPath.addListenerForSingleValueEvent(object : ValueEventListener
				{
					override fun onDataChange(snapshot: DataSnapshot)
					{
						count++;
						val books: MutableList<Book> = mutableListOf()

						for (dsnap in snapshot.children)
						{
							val book = dsnap.getValue(BookDao::class.java)
							if (book != null && !book.isRemoved && book.userId != user?.uid && !_books.any { it.bookId != dsnap.key }) //dont add the same stuff
							{
								Log.e("dsnap", "isAvailable: " + dsnap.child("isAvailable").value)
								val tags = mutableListOf<Tag>()
								book.tags?.forEach { tags.add(Tag(it.key)) }
								if (book.isAvailable && !book.isRemoved)
								{
									books.add(
										Book(
											book.name, book.description, book.author,
											book.pic.toString(), tags, book.userId, dsnap.key,
											book.lat, book.log,book.isRemoved, book.bookerId
										)
									)
								}

							}
						}
						if (count == desiredCount)
						{
							_books.addAll(books)
							booksLoaded.invoke(Result.Success(Unit))
						}
					}

					override fun onCancelled(error: DatabaseError)
					{
						booksLoaded.invoke(Result.Error(IOException("Could not load base tags.")))
					}
				}
				)
			}
		}

	}

	fun setBookAvailabilityAndBooker(
		ownerId: String,
		bookId: String,
		isAvailable: Boolean,
		callback: (Result<Unit>) -> Unit
	)
	{
		val currUser = user;
		if (currUser != null)
		{
			val userId = currUser.uid
			val fromPath = db.child("userBooks/$bookId")
			val toPath = db.child("bookedUserBooks/$userId/$bookId")
			copyFirebaseRecord(fromPath, toPath) {
				if (it is Result.Success)
				{
					val updates: MutableMap<String, Any> = HashMap()
					updates["users/$ownerId/books/$bookId/isAvailable"] = isAvailable
					updates["userBooks/$bookId/isAvailable"] = isAvailable
					updates["bookedUserBooks/$userId/$bookId/isAvailable"] = isAvailable

					updates["users/$ownerId/books/$bookId/bookerId"] = userId
					updates["userBooks/$bookId/bookerId"] = userId
					updates["bookedUserBooks/$userId/$bookId/bookerId"] = userId
					db.updateChildren(updates).addOnCompleteListener { yt ->
						if (yt.isSuccessful)
							callback.invoke(Result.Success(Unit))
						else if (yt.isCanceled)
							callback.invoke(Result.Error(CancellationException("canceled")))
						else if (yt.exception != null)
						{
							callback.invoke(Result.Error(yt.exception!!))
						}
					}
				}
				else
				{
					callback.invoke(it)
				}
			}
		}
	}
	fun unsetBookAvailabilityAndBooker(
		bookId: String,
		callback: (Result<Unit>) -> Unit
	)
	{
		val currUser = user;
		if (currUser != null)
		{
			val userId = currUser.uid
			val toPath = db.child("bookedUserBooks/$userId/$bookId")
			removeFirebaseRecord(toPath) {
				if (it is Result.Success)
				{
					val updates: MutableMap<String, Any?> = HashMap()
					updates["users/$userId/books/$bookId/isAvailable"] = true
					updates["userBooks/$bookId/isAvailable"] = true

					updates["users/$userId/books/$bookId/bookerId"] = null
					updates["userBooks/$bookId/bookerId"] = null
					db.updateChildren(updates).addOnCompleteListener { yt ->
						if (yt.isSuccessful)
							callback.invoke(Result.Success(Unit))
						else if (yt.isCanceled)
							callback.invoke(Result.Error(CancellationException("canceled")))
						else if (yt.exception != null)
						{
							callback.invoke(Result.Error(yt.exception!!))
						}
					}
				}
				else
				{
					callback.invoke(it)
				}
			}
		}
	}


	fun getAuthors()
	{
		db.child("authors").addListenerForSingleValueEvent(object : ValueEventListener
		{
			override fun onDataChange(snapshot: DataSnapshot)
			{
				var authors: MutableList<Author> = mutableListOf()
				for (dsnap in snapshot.children)
				{
					var author = dsnap.getValue(Author::class.java)
					if (author != null)
						authors.add(author);
				}
				_authors = authors;
				authorsLoaded.invoke()
			}

			override fun onCancelled(error: DatabaseError)
			{
				Log.e("firebase", "onCancelled ${error.message}")
				authorsLoaded()
			}
		})
	}

	fun addBook(book: Book, imageUri: Uri?, callback: (Result<Unit>) -> Unit)
	{
		addImageToStorage(book, imageUri) { it ->
			if (it is Result.Success)
			{
				val key = db.child("userBooks").push().key
				val user = auth.currentUser;
				val tags: HashMap<String, Boolean> = hashMapOf();
				try
				{
					book.tags!!.forEach { yt -> tags.put(yt.name, true) }
				} catch (e: Exception)
				{
					callback.invoke(Result.Error(e));
				}
				if (user != null)
				{
					val useruid = user.uid
					var bookDao = BookDao(
						book.name,
						book.description,
						book.author,
						it.data.toString(),
						tags,
						useruid,
						book.lat,
						book.log,
						true,
						false
					)
					val updates: MutableMap<String, Any> = HashMap()
					updates["users/$useruid/books/$key"] = bookDao
					updates["userBooks/$key"] = bookDao
					db.updateChildren(updates).addOnCompleteListener { yt ->
						if (yt.isSuccessful)
							callback.invoke(Result.Success(Unit))
						else if (yt.isCanceled)
							callback.invoke(Result.Error(CancellationException("canceled")))
						else if (yt.exception != null)
						{
							callback.invoke(Result.Error(yt.exception!!))
						}
					}
				}
			}
			else
			{
				callback(Result.Error(Exception(it.toString())))
			}
		}
	}

	private fun addImageToStorage(book: Book, imageUri: Uri?, callback: (Result<Uri>) -> Unit)
	{
		if (imageUri != null)
		{
			storage.child(book.name + auth.currentUser?.uid).putFile(imageUri)
				.addOnSuccessListener {
					storage.child(book.name + auth.currentUser?.uid).downloadUrl.addOnCompleteListener() { taskSnapshot ->

						var url = taskSnapshot.result
						if (url != null)
							callback.invoke(Result.Success(url))
						else
							callback.invoke(Result.Error(Exception("Could not get url back")))

					}.addOnFailureListener {
						callback.invoke(Result.Error(Exception("some error")))
					}
				}
		}
		else
		{
			callback.invoke(Result.Error(Exception("no image uri provided")))
		}
	}

	private fun getUserDataAndSetupAuthStateListener(notLoggedCallback: () -> Unit): FirebaseUser?
	{
		val user = auth.currentUser
		if (user != null)
		{
			db.child("users").child(user.uid)
				.addListenerForSingleValueEvent(object : ValueEventListener
				{
					override fun onDataChange(snapshot: DataSnapshot)
					{
						if (snapshot.getValue() == null)
							db.child("users").child(user.uid).setValue(UserDao(user.email))

					}

					override fun onCancelled(error: DatabaseError)
					{
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

	fun getBooks()
	{
		db.child("userBooks")
			.addValueEventListener(object : ValueEventListener
			{
				override fun onDataChange(snapshot: DataSnapshot)
				{
					val books: MutableList<Book> = mutableListOf()

					for (dsnap in snapshot.children)
					{
						val book = dsnap.getValue(BookDao::class.java)
						if (book != null)
						{
							val tags = mutableListOf<Tag>()
							book.tags?.forEach { tags.add(Tag(it.key)) }
							books.add(
								Book(
									book.name, book.description, book.author,
									book.pic.toString(), tags, book.userId, dsnap.key,
									book.lat, book.log
								)
							)
						}
					}
					if (books.count() == 0)
						booksLoaded.invoke(Result.Error(IOException("Could not load base tags.")))
					else
					{
						_books = books
						booksLoaded.invoke(Result.Success(Unit))
					}
				}

				override fun onCancelled(error: DatabaseError)
				{
					Log.e("firebase", "onCancelled ${error.message}")
					booksLoaded(Result.Error(IOException("Could not load base tags. Exe")))
				}
			})
	}

	private fun copyFirebaseRecord(
		fromPath: DatabaseReference,
		toPath: DatabaseReference,
		callback: (Result<Unit>) -> Unit
	)
	{
		fromPath.addListenerForSingleValueEvent(object : ValueEventListener
		{
			override fun onDataChange(dataSnapshot: DataSnapshot)
			{
				toPath.setValue(dataSnapshot.value).addOnCompleteListener {
					if (it.isSuccessful)
					{
						Log.e("firebase", "added $fromPath to $toPath")
						callback.invoke(Result.Success(Unit))
					}
					if (it.isCanceled)
					{
						Log.e("firebase", "not added $fromPath to $toPath, error: Canceled")
						callback.invoke(Result.Error(Exception("Canceled")))
					}
				}
			}

			override fun onCancelled(error: DatabaseError)
			{
				Log.e("firebase", "onCancelled ${error.message}")
				callback.invoke(Result.Error(Exception("Canceled")))
			}
		}
		)
	}

	private fun removeFirebaseRecord(toPath: DatabaseReference,	callback: (Result<Unit>) -> Unit)
	{
		toPath.removeValue().addOnSuccessListener {
			callback.invoke(Result.Success(Unit))
		}.addOnCanceledListener{
			callback.invoke(Result.Error(Exception("Canceled")))
		}
	}


	fun getUserAddedBooks(function: (Result<Unit>) -> Unit)
	{
		val user = user;
		if (user != null)
		{
			val userid = user.uid
			db.child("users").child(userid).child("books")
				.addListenerForSingleValueEvent(object : ValueEventListener
				{
					override fun onDataChange(snapshot: DataSnapshot)
					{
						val books: MutableList<Book> = mutableListOf()

						for (dsnap in snapshot.children)
						{
							val book = dsnap.getValue(BookDao::class.java)
							if (book != null && !book.isRemoved)
							{
								val tags = mutableListOf<Tag>()
								book.tags?.forEach { tags.add(Tag(it.key)) }
								books.add(
									Book(
										book.name, book.description, book.author,
										book.pic.toString(), tags, book.userId, dsnap.key,
										book.lat, book.log,book.isRemoved, book.bookerId
									)
								)
							}
						}
						_userBooks = books
						function.invoke(Result.Success(Unit))
					}

					override fun onCancelled(error: DatabaseError)
					{
						function.invoke(Result.Error(Exception("Canceled")))
					}
				}
				)
		}
	}

	fun removeUserBook(book: Book, callback: (Result<Unit>) -> Unit)
	{
		val user = user;
		if(user != null ){
			val updates: MutableMap<String, Any> = HashMap()
			updates["users/${user.uid}/books/${book.bookId}/isRemoved"] = true
			updates["userBooks/${book.bookId}/isRemoved"] = true
			updates["bookedUserBooks/${book.bookerId}/${book.bookId}/isRemoved"] = true
			db.updateChildren(updates).addOnCompleteListener { yt ->
				if (yt.isSuccessful)
					callback.invoke(Result.Success(Unit))
				else if (yt.isCanceled)
					callback.invoke(Result.Error(CancellationException("canceled")))
				else if (yt.exception != null)
				{
					callback.invoke(Result.Error(yt.exception!!))
				}
			}
		}
	}

	fun getUserBookedBooks(function: (Result<Unit>) -> Unit)
	{
		val user = user;
		if (user != null)
		{
			val userid = user.uid
			db.child("bookedUserBooks").child(userid)
				.addListenerForSingleValueEvent(object : ValueEventListener
				{
					override fun onDataChange(snapshot: DataSnapshot)
					{
						val books: MutableList<Book> = mutableListOf()

						for (dsnap in snapshot.children)
						{
							val book = dsnap.getValue(BookDao::class.java)
							if (book != null && !book.isRemoved )
							{
								val tags = mutableListOf<Tag>()
								book.tags?.forEach { tags.add(Tag(it.key)) }
								books.add(
									Book(
										book.name, book.description, book.author,
										book.pic.toString(), tags, book.userId, dsnap.key,
										book.lat, book.log,book.isRemoved, book.bookerId
									)
								)
							}
						}
						_userBookedBooks = books
						function.invoke(Result.Success(Unit))
					}

					override fun onCancelled(error: DatabaseError)
					{
						function.invoke(Result.Error(Exception("Canceled")))
					}
				}
				)
		}
	}
}



