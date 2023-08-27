package com.example.smb.booksapp.viewmodels.drawerFragments

import android.graphics.Bitmap
import android.location.Geocoder
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
import android.util.Log


class UserInfoViewModel(private val userRepository: UserRepository) : ViewModel()
{

	private var _marker: Marker? = null
	private val _userForm = MutableLiveData<UserFormState>()
	private var _markerDisplayString = MutableLiveData<String>()
	private val _loadDataResult = MutableLiveData<Result<Boolean>?>()
	private val _userimage = MutableLiveData<Bitmap?>()

	val userFormState: LiveData<UserFormState> = _userForm
	val markerString: LiveData<String> = _markerDisplayString

	var user: FirebaseUser? = null
		get() = userRepository.user
	var userAdv: UserDao? = null
		get() = userRepository.userAdv;

	val loadResult: LiveData<Result<Boolean>?> = _loadDataResult
	val userimage: LiveData<Bitmap?> = _userimage

	init
	{
		userRepository.dataLoadedCallback = {
			_loadDataResult.value = it
		}
		userRepository.getUserPic {
			if (it is Result.Success)
				_userimage.value = it.data
		}
	}

	fun saveUserData(name: String, desc: String, imageUri: Uri?, callback: (Result<Unit>) -> Unit)
	{
		userRepository.updateUser(
			name,
			desc,
			imageUri,
			_marker?.position?.latitude.toString(),
			_marker?.position?.longitude.toString()
		) {
			callback.invoke(it)
		}
	}

	fun userDataChanged(nick: String, desc: String, location: String = "")
	{
		Log.e("tut", nick.toString() + desc.toString())
		if (nick.length < 3)
		{
			_userForm.value = UserFormState(nickError = (R.string.ValidNameError))
		}
		else if (desc.length < 3)
		{
			_userForm.value = UserFormState(descriptionError = (R.string.notLong))
		}
		else if (location == "")
		{
			_userForm.value = UserFormState(locationError = R.string.SpecifyLoc)
		}
		else
		{
			_userForm.value = UserFormState(isDataValid = true)
		}
	}

	fun changeLocation(marker: Marker, cityName: String)
	{
		_marker = marker;
		if (_marker == null)
		{
			_userForm.value = UserFormState(locationError = R.string.SpecifyLoc)
		}
		if (_marker != null)
		{
			if (_userForm.value == UserFormState(locationError = R.string.SpecifyLoc))
				_userForm.value = UserFormState(isDataValid = true)
			_markerDisplayString.value = cityName;
		}
	}
}