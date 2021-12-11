package com.example.smb.booksapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.URLSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import com.example.smb.booksapp.data.register.RegisterDataSource
import com.example.smb.booksapp.data.register.RegisterRepository
import com.example.smb.booksapp.databinding.ActivityRegisterBinding
import com.example.smb.booksapp.viewmodels.register.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.editTextEmailAddress
        val password = binding.editTextPassword
        val passwordRepeat = binding.editTextPassword2
        val registerBtn = binding.registerBtn
        val loading = binding.loading
        val goToLoginTextView = binding.haveAnAccountLink

       goToLoginTextView.hyperlinkStyle()

        registerViewModel = RegisterViewModel(RegisterRepository(RegisterDataSource()));

        registerViewModel.registerFormState.observe(this@RegisterActivity,
            Observer {
                val registerState = it ?: return@Observer

                registerBtn.isEnabled = registerState.isDataValid

                if (registerState.emailError != null)
                    email.error = getString(registerState.emailError)
                if (registerState.passwordError != null)
                    password.error = getString(registerState.passwordError)
                if (registerState.passwordRepeatError != null)
                    passwordRepeat.error = getString(registerState.passwordRepeatError)
            })

        registerViewModel.registerResult.observe(this, Observer {
            val registerResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (!registerResult.success)
                showRegisterFailed(R.string.RegisterFailed)
            else{
                showRegisterSuccess(R.string.RegisterSuccess)
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                setResult(Activity.RESULT_OK)
                finish()

            }
        })

        email.afterTextChanged {
            registerViewModel.registerDataChanged(
                email.text.toString(),
                password.text.toString(),
                passwordRepeat.text.toString()
            )
        }
        password.afterTextChanged {
            registerViewModel.registerDataChanged(
                email.text.toString(),
                password.text.toString(),
                passwordRepeat.text.toString()
            )}
        passwordRepeat.afterTextChanged {
            registerViewModel.registerDataChanged(
                email.text.toString(),
                password.text.toString(),
                passwordRepeat.text.toString()
            )
        }
        passwordRepeat.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
               registerViewModel.register(email.text.toString(), password = password.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }
        goToLoginTextView.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            setResult(Activity.RESULT_OK)
            finish()
        }


    }

    private fun showRegisterSuccess(registerSuccess: Int) {
        Toast.makeText(applicationContext, registerSuccess, Toast.LENGTH_SHORT).show()
    }

    private fun showRegisterFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }
    private fun TextView.hyperlinkStyle() {
        setText(
            SpannableString(text).apply {
                setSpan(
                    URLSpan(""),
                    0,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            },
            TextView.BufferType.SPANNABLE
        )
    }
}