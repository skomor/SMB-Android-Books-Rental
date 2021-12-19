package com.example.smb.booksapp.data.drawerFragments

import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.model.Tag
import java.io.IOException
import java.lang.Exception

class TagsRepository(private val tagsDataSource: TagsDataSource) {

    fun saveUserTags(value: MutableList<Tag>, resultCallback: (Result<Unit>) -> Unit) {
        var userTags = mutableListOf<Tag>();
        value.forEach {
            if (it.isUsers == true)
                userTags.add(it)
        }
        tagsDataSource.saveUserTags(userTags) {
            resultCallback(it);
        }
    }
    private lateinit var _tags: MutableList<String>
    private var _combinedTags: MutableList<Tag> = mutableListOf()
    private lateinit var _userTags: MutableList<String>
    lateinit var dataLoadedCallback: (Result<Boolean>) -> Unit

    var combinedTags: MutableList<Tag>? = null
        get() = _combinedTags

    init {
        tagsDataSource.getNormalTags {
            if (it is Result.Success) {
                _tags = it.data
                tagsDataSource.getUserTags { yt ->
                    if (yt is Result.Success) {
                        _userTags = yt.data
                        _combinedTags.clear()
                        for (tag in _tags) {
                            if (_userTags.contains(tag)) {
                                _combinedTags.add(Tag(tag, true))
                            } else {
                                _combinedTags.add(Tag(tag, false))
                            }
                        }
                        dataLoadedCallback(Result.Success(true))
                    } else {
                        dataLoadedCallback(Result.Error(IOException(it.toString())))
                    }
                }
            } else {
                throw Exception("Could not load data!")
            }
        }

    }
}