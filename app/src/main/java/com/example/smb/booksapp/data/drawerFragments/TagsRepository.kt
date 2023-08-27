package com.example.smb.booksapp.data.drawerFragments

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.smb.booksapp.R
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.model.Tag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList

class TagsRepository(val context: Context)
{

	private var db: DatabaseReference =
		FirebaseDatabase.getInstance("https://bookapp-0404-default-rtdb.europe-west1.firebasedatabase.app").reference;
	private val pref: SharedPreferences =
		context.getSharedPreferences("com.example.booksapp.23423", Context.MODE_PRIVATE)
	private lateinit var _tags: MutableList<String>
	private var _combinedTags: MutableList<Tag> = mutableListOf()

	private var dataLoadedCallback: (Result<Boolean>) -> Unit
	lateinit var userDataLoadedCallback: (Unit) -> Unit
	var updatedUserTags: (Unit) -> Unit = {}

	var combinedTags: MutableList<Tag>? = null
		get() = _combinedTags

	init
	{
		getNormalTags()
		dataLoadedCallback = {
			getUserTags()
		}
	}

	fun getNormalTags()
	{
		db.child("tags").addListenerForSingleValueEvent(object : ValueEventListener
		{
			override fun onDataChange(snapshot: DataSnapshot)
			{
				var tags: MutableList<String> = mutableListOf()
				for (dsnap in snapshot.children)
				{
					var asTag = dsnap.getValue(String::class.java)
					if (asTag != null)
						tags.add(asTag);
				}
				if (tags.count() == 0)
				{
					dataLoadedCallback.invoke(Result.Error(IOException("Could not load base tags.")))
				}
				else
				{
					_tags = tags
					_combinedTags.clear()
					for (tag in tags)
					{
						_combinedTags.add(Tag(tag, false))
					}
					dataLoadedCallback.invoke(Result.Success(true))
				}
			}

			override fun onCancelled(error: DatabaseError)
			{
				Log.e("firebase", "onCancelled ${error.message}")
				dataLoadedCallback(Result.Error(IOException("Could not load base tags. Reason: Cancelled, ${error.message}")))
			}
		})
	}

	private fun getUserTags()
	{
		val userTags = getListOfTagsFromSharedPref()
		_combinedTags.clear()
		for (tag in _tags)
		{
			if (userTags.contains(tag))
			{
				_combinedTags.add(Tag(tag, true))
			}
			else
			{
				_combinedTags.add(Tag(tag, false))
			}
		}
		userDataLoadedCallback.invoke(Unit)
	}

	fun saveUserTags(values: MutableList<Tag>)
	{
		_combinedTags = values;
		val userTags = mutableListOf<Tag>();
		values.forEach {
			if (it.isUsers)
				userTags.add(it)
		}
		val names: ArrayList<String> = arrayListOf();
		userTags.forEach { names.add(it.name) }

		setListsOfTagsToSharedPref(names)
		updatedUserTags.invoke(Unit);
	}

	private fun setListsOfTagsToSharedPref(list: ArrayList<String>)
	{
		val gson = Gson()
		val json = gson.toJson(list)//converting list to Json
		with(pref.edit()) {
			putString("UserTags", json)
			apply()
		}
	}

	private fun getListOfTagsFromSharedPref(): ArrayList<String>
	{
		val gson = Gson()
		val tags = pref.getString("UserTags", "")
		if (tags != "")
		{
			val type = object : TypeToken<ArrayList<String>>()
			{}.type//converting the json to list
			return gson.fromJson(tags, type)//returning the list
		}
		else return arrayListOf()
	}
}