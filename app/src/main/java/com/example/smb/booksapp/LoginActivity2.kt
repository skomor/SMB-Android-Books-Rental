package com.example.smb.booksapp

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.smb.booksapp.databinding.ActivityLogin2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.content.Intent
import android.util.Log
import android.util.Patterns
import android.widget.Toast

class LoginActivity2 : AppCompatActivity() {

    private val Tag = "LoginActivity2"

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLogin2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.editTextEmailAddress.afterTextChanged{ removeError(binding.editTextEmailAddress);}
        binding.editTextPassword.afterTextChanged{ removeError(binding.editTextPassword);}

        binding.LoginBtn.setOnClickListener {
            if(!binding.editTextEmailAddress.text.toString()
                .isValidEmail()){
                binding.editTextEmailAddress.error = "Wrong email address!"
            }else{
                if (!binding.editTextPassword.text.toString().isValidPassword()) {
                    binding.editTextPassword.error = "too short password!"
                }
                else   loginWithEmailAndPassword()
            }
        }
        binding.registerBtn.setOnClickListener{
            gotoRegister();
        }
    }

    private fun gotoRegister() {
    }

    private fun removeError(editText: EditText) {
        editText.setError(null)
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

    private fun CharSequence?.isValidEmail(): Boolean {
        return !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun CharSequence?.isValidPassword(): Boolean {
        return this?.length!! > 6
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            runMainActivity();
        }
    }

    private fun loginWithEmailAndPassword() {
        val email = binding.editTextEmailAddress.text;
        val password = binding.editTextPassword.text;
        auth.signInWithEmailAndPassword(email.toString(), password.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(Tag, "signInWithEmail:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(Tag, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.editTextPassword.error = "Wrong email or password!"
                    binding.editTextEmailAddress.error = "Wrong email or password!"
                }
            }
    }

    private fun runMainActivity() {
        val intentDrugieActivity = Intent(this, MainActivity::class.java)
        startActivity(intentDrugieActivity)
    }
}