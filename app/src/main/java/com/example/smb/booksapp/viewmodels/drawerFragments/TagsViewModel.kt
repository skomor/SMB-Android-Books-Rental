package com.example.smb.booksapp.viewmodels.drawerFragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.data.drawerFragments.TagsRepository
import com.example.smb.booksapp.data.model.Tag

class TagsViewModel(private val tagsRepository: TagsRepository) : ViewModel() {

    private val _loadDataResult = MutableLiveData<Result<Boolean>?>()
    val loadResult: LiveData<Result<Boolean>?> = _loadDataResult
    private val _saveResult = MutableLiveData<Result<Unit>>()
    val saveResult: LiveData<Result<Unit>> = _saveResult

    private val _tags = MutableLiveData<MutableList<Tag>>().apply {
        value = mutableListOf()
    }
    val tags: LiveData<MutableList<Tag>> = _tags

    fun saveUserTags() {
            tagsRepository.saveUserTags(_tags.value!!) {
                _saveResult.value = it
                Log.e("tut",it.toString())
            }
    }

    init {
        tagsRepository.dataLoadedCallback = {
            _loadDataResult.value = it
            Log.e("tut",it.toString())
            tagsRepository.getUserTags()
        }
        tagsRepository.userDataLoadedCallback = {
            _tags.value = tagsRepository.combinedTags;
        }
    }
}