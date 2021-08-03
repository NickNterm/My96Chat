package com.iqsoft.my96chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.iqsoft.my96chat.R
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        setupActionBar()
        sign_in_button.setOnClickListener {
            hideKeyboard(this)
            val email = sign_in_email_edit_text.text.toString()
            val password = sign_in_password_edit_text.text.toString()
            if (validateForm(email, password)) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }.addOnFailureListener {
                        showErrorSnackBar(resources.getString(R.string.sign_in_error))
                    }
            }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please Enter Email")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please Enter Password")
                false
            }
            else -> {
                true
            }
        }
    }


    private fun setupActionBar() {
        setSupportActionBar(sign_in_toolbar)
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)

        sign_in_toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }
    }
}