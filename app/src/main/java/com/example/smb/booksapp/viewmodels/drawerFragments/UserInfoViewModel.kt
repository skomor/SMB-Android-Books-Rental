package com.example.smb.booksapp.viewmodels.drawerFragments

import android.graphics.Bitmap
import android.net.Uri
import com.example.smb.booksapp.data.Result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smb.booksapp.R
import com.example.smb.booksapp.data.drawerFragments.UserRepository
import com.example.smb.booksapp.data.model.UserDao
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseUser
import android.location.Address

import java.util.Locale

import android.location.Geocoder
import android.util.Log


class UserInfoViewModel(private val userRepository: UserRepository) : ViewModel() {

    private var _marker: Marker? = null
    private val _userForm = MutableLiveData<UserFormState>()
    val userFormState: LiveData<UserFormState> = _userForm
    var _markerString= MutableLiveData<String>()
    val markerString: LiveData<String> = _markerString

    fun saveUserData(name: String, desc: String, imageUri: Uri?, callback: (Result<Unit>) -> Unit) {
        userRepository.setData(name, desc, imageUri) {
            callback.invoke(it)
        }
    }

    fun userDataChanged(nick: String, desc: String) {
        Log.e("tut",nick.toString() + desc.toString() )
        if (nick.length < 3) {
            _userForm.value = UserFormState(nickError = (R.string.ValidNameError))
        } else if (desc.length < 3) {
            _userForm.value = UserFormState(descriptionError = (R.string.notLong))
        }else if(_marker == null){
            _userForm.value = UserFormState(locationError = R.string.SpecifyLoc)
        } else {
            _userForm.value = UserFormState(isDataValid = true)
        }
    }

    fun locationChange(marker: Marker, cityName:String) {
        _marker = marker;
        if(_marker == null) {
            _userForm.value = UserFormState(locationError = R.string.SpecifyLoc)

        }
        if(_marker != null) {
            _userForm.value = UserFormState(isDataValid = true)
            _markerString.value = cityName;
        }
    }

    private val _loadDataResult = MutableLiveData<Result<Boolean>?>()
    val loadResult: LiveData<Result<Boolean>?> = _loadDataResult
    private val _userimage = MutableLiveData<Bitmap?>()
    val userimage: LiveData<Bitmap?> = _userimage

    init {
        userRepository.dataLoadedCallback = {
            _loadDataResult.value = it
        }
        userRepository.getUserPic {
            if (it is Result.Success)
                _userimage.value = it.data
        }
    }

    var user: FirebaseUser? = null
        get() = userRepository.user

    var userAdv: UserDao? = null
        get() = userRepository.userAdv;
}