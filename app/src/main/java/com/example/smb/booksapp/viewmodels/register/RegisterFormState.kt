package com.example.smb.booksapp.viewmodels.register

/**
 * Data validation state of the login form.
 */
data class RegisterFormState(
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val passwordRepeatError : Int? = null,
    val nickError: Int? = null,
    val isDataValid: Boolean = false
)