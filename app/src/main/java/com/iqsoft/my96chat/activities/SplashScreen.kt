package com.iqsoft.my96chat.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.iqsoft.my96chat.R
import com.iqsoft.my96chat.firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val typeFace: Typeface = Typeface.createFromAsset(assets, "Sen-Bold.ttf")

        splash_screen_title.typeface = typeFace

        Handler().postDelayed({
            if (FirestoreClass().getCurrentUserID().isEmpty()) {
                startActivity(Intent(this, IntroActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 3000)
    }
}