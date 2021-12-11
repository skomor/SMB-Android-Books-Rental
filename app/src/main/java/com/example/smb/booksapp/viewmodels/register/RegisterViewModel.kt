package com.example.smb.booksapp.viewmodels.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smb.booksapp.R
import com.example.smb.booksapp.data.Result.Success
import com.example.smb.booksapp.data.register.RegisterRepository

class RegisterViewModel(private val registerRepository: RegisterRepository) : ViewModel() {
    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    fun register(username: String, password: String) {
       registerRepository
            .register(username, password){ result ->
                if(result is Success)
                    _registerResult.value = RegisterResult(true)
                else _registerResult.value = RegisterResult(false,result.toString() )
        }
    }

    fun registerDataChanged(email: String, pass: String, passwordRepeat: String) {
        if (!isUserNameValid(email)) {
            _registerForm.value = RegisterFormState(emailError = R.string.invalid_username)
        } else if (!isPasswordValid(pass)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else if (passwordRepeat != pass ) {
            _registerForm.value = RegisterFormState(passwordRepeatError = R.string.PasswordsDontMatch)
        } else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }

    private fun isUserNameValid(username: String): Boolean {
        return  Patterns.EMAIL_ADDRESS.matcher(username).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}