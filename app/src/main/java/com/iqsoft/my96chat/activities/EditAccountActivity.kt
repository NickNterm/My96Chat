package com.iqsoft.my96chat.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.iqsoft.my96chat.R
import com.iqsoft.my96chat.firebase.FirestoreClass
import com.iqsoft.my96chat.models.User
import com.iqsoft.my96chat.utils.Constants
import kotlinx.android.synthetic.main.activity_edit_account.*

class EditAccountActivity : BaseActivity() {
    private lateinit var mUser: User
    private var mSelectedImage: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_account)
        mUser = intent.getParcelableExtra(Constants.INTENT_MY_USER)!!
        edit_account_name_edit_text.setText(mUser.name)
        edit_account_phone_edit_text.setText(mUser.phone.toString())
        mSelectedImage = mUser.image
        Glide.with(this)
            .load(mUser.image)
            .centerCrop()
            .placeholder(R.drawable.ic_baseline_face_24)
            .into(edit_account_image_button)
        setupActionBar()

        edit_account_image_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@EditAccountActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.REQUEST_READ_EXTERNAL_STORAGE_CODE
                )
            } else {
                selectImage()
            }
        }
        edit_account_button.setOnClickListener {
            hideKeyboard(this)
            val name = edit_account_name_edit_text.text.toString()
            val phone = edit_account_phone_edit_text.text.toString().toLong()
            if (validateForm(name, phone, mSelectedImage)) {
                val user = User(
                    mUser.id,
                    name,
                    mUser.email,
                    phone,
                    mSelectedImage
                )
                FirestoreClass().updateUserProfile(this, user)
            }
        }
    }

    fun userUpdatedSuccessfully(){
        setResult(Activity.RESULT_OK)
        finish()
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
                        .into(edit_account_image_button)
                }
            }.addOnFailureListener {
                showErrorSnackBar(resources.getString(R.string.load_image_error))
            }
        }
    }

    private fun validateForm(
        name: String,
        phone: Long,
        image: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please Enter Name")
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
        setSupportActionBar(edit_account_toolbar)
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)

        edit_account_toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }
    }
}