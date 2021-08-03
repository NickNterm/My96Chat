package com.iqsoft.my96chat.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.iqsoft.my96chat.R
import com.iqsoft.my96chat.firebase.FirestoreClass
import com.iqsoft.my96chat.models.User
import com.iqsoft.my96chat.utils.Constants
import kotlinx.android.synthetic.main.activity_sign_up.*
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import androidx.core.app.ActivityCompat.startActivityForResult

import android.provider.MediaStore


class SignUpActivity : BaseActivity() {
    private var mSelectedImage: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        setupActionBar()

        sign_up_image_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@SignUpActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.REQUEST_READ_EXTERNAL_STORAGE_CODE
                )
            } else {
                selectImage()
            }
        }

        sign_up_button.setOnClickListener {
            hideKeyboard(this)
            val name = sign_up_name_edit_text.text.toString()
            val email = sign_up_email_edit_text.text.toString()
            val password = sign_up_password_edit_text.text.toString()
            val phone = sign_up_phone_edit_text.text.toString().toLong()
            if (validateForm(name, email, password, phone, mSelectedImage)) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser: com.google.firebase.auth.FirebaseUser =
                                task.result!!.user!!
                            val registerEmail = firebaseUser.email!!
                            val user =
                                User(firebaseUser.uid, name, registerEmail, phone, mSelectedImage)

                            FirestoreClass().registerUser(this, user)
                        } else {
                            showErrorSnackBar(task.exception!!.message.toString())
                        }
                    }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_READ_EXTERNAL_STORAGE_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                showErrorSnackBar(resources.getString(R.string.permission_error))
            }
        }
    }

    private fun selectImage() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, Constants.SELECT_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SELECT_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri = data!!.data!!
            Log.e("testt", "come")
            val storage = FirebaseStorage.getInstance().reference.child(
                "profile/USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this,
                    selectedImageUri
                )
            )
            storage.putFile(selectedImageUri).addOnSuccessListener { task ->
                task.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    mSelectedImage = uri.toString()
                    Glide.with(this)
                        .load(selectedImageUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_baseline_face_24)
                        .into(sign_up_image_button)
                }
            }.addOnFailureListener {
                showErrorSnackBar(resources.getString(R.string.load_image_error))
            }
        }
    }

    fun userRegisteredComplete() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            sign_up_email_edit_text.text.toString(),
            sign_up_password_edit_text.text.toString()
        ).addOnSuccessListener {
            startActivity(Intent(this, MainActivity::class.java))
        }.addOnFailureListener {
            showErrorSnackBar(resources.getString(R.string.sign_in_error))
        }
    }


    private fun validateForm(
        name: String,
        email: String,
        password: String,
        phone: Long,
        image: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please Enter Name")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please Enter Email")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please Enter Password")
                false
            }
            phone == 0L -> {
                showErrorSnackBar("Please Enter Phone Number")
                false
            }
            TextUtils.isEmpty(image) -> {
                showErrorSnackBar("Please Select Image")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(sign_up_toolbar)
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)

        sign_up_toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }
    }
}