package com.iqsoft.my96chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.iqsoft.my96chat.R
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        intro_sign_in_button.setOnClickListener{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        intro_sign_up_button.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}