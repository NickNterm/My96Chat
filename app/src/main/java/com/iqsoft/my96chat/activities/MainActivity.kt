package com.iqsoft.my96chat.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.iqsoft.my96chat.R
import com.iqsoft.my96chat.adapters.ConvListAdapter
import com.iqsoft.my96chat.firebase.FirestoreClass
import com.iqsoft.my96chat.models.Conversation
import com.iqsoft.my96chat.models.User
import com.iqsoft.my96chat.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_add_user.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : BaseActivity() {
    private lateinit var mUser: User
    private var mConvList: ArrayList<Conversation> = ArrayList()
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_activity_refresh.isRefreshing = true
        main_activity_floating_button.setOnClickListener {
            showAddDialog()
        }
        main_activity_refresh.setOnRefreshListener {
            FirestoreClass().loadConvsFromID(this, mUser.id)
        }

        FirestoreClass().loadLoggedInUser(this)
    }

    fun tokenUpdateSuccess() {
        main_activity_refresh.isRefreshing = false
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        FirestoreClass().loadLoggedInUser(this)

    }

    private fun updateFCMToken(token: String) {
        mUser.fcm = token
        FirestoreClass().updateUserProfile(this, mUser)
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_log_out -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, IntroActivity::class.java))
                return true
            }
            R.id.action_edit_account -> {
                val intent = Intent(this, EditAccountActivity::class.java)
                intent.putExtra(Constants.INTENT_MY_USER, mUser)
                startActivityForResult(intent, Constants.ACTIVITY_UPDATE_USER)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.ACTIVITY_UPDATE_USER) {
            FirestoreClass().loadLoggedInUser(this)
        }
    }

    private fun showAddDialog() {
        val dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_add_user)
        dialog.dialog_add_button.setOnClickListener {
            if (dialog.dialog_add_user_edit_text.toString()
                    .isNotEmpty() && dialog.dialog_add_user_edit_text.toString() != mUser.email
            ) {
                val email = dialog.dialog_add_user_edit_text.text.toString()
                FirestoreClass().getUserFromEmail(this, email)
                main_activity_refresh.isRefreshing = true

            } else {
                showErrorSnackBar("email Can't be empty")
            }
            dialog.dismiss()
        }
        dialog.dialog_cancel_button.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun userLoadSuccessfully(user: User) {
        mUser = user
        mSharedPreferences =
            this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        if (!tokenUpdated)  {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                    updateFCMToken(instanceIdResult)
                }
        }
        setupActionBar()
        loadConvs()
    }

    fun createConvSuccess() {
        loadConvs()
    }

    private fun loadConvs() {
        FirestoreClass().loadConvsFromID(this, FirestoreClass().getCurrentUserID())
    }

    private fun setupActionBar() {
        setSupportActionBar(main_activity_toolbar)
        val actionBar = supportActionBar!!
        actionBar.title = mUser.name
    }

    fun convsLoadedSuccessfully(convList: ArrayList<Conversation>) {
        mConvList = convList
        val idList = ArrayList<String>()
        for (i in mConvList) {
            i.assignedTo.remove(mUser.id)
            idList.add(i.assignedTo[0])
        }
        if (idList.size > 0) {
            FirestoreClass().getUserFromIdList(this, idList)
        } else {
            main_activity_refresh.isRefreshing = false
        }
    }


    fun setupConvRecyclerView(userList: ArrayList<User>) {
        main_activity_refresh.isRefreshing = false
        main_activity_main_recycler_view.layoutManager = LinearLayoutManager(this)
        Log.e("testt", userList.toString())
        val adapter = ConvListAdapter(this, userList)
        main_activity_main_recycler_view.adapter = adapter
        adapter.setOnClickListener(object : ConvListAdapter.OnClickListener {
            override fun onClick(user: User, position: Int) {
                val intent = Intent(this@MainActivity, ConversationActivity::class.java)
                intent.putExtra(Constants.INTENT_CONV, mConvList[position])
                intent.putExtra(Constants.INTENT_OTHER_USER, user)
                intent.putExtra(Constants.INTENT_MY_USER, mUser)
                startActivity(intent)
            }
        })
    }

    fun userFromEmailSuccess(usersList: ArrayList<User>) {
        if (usersList.size > 0) {
            val idList = ArrayList<String>()
            idList.add(mUser.id)
            idList.add(usersList[0].id)
            Log.w("connection", "Start1")
            FirestoreClass().sendNotification(this, usersList[0].fcm, "You have been invited in a conversation", "Dealer: ${mUser.name}, wants to talk you")
            val conv = Conversation(idList)
            FirestoreClass().createConv(this, conv)
        } else {
            showErrorSnackBar("UserNotFound")
        }
    }

    fun startRefreshing(){
        main_activity_refresh.isRefreshing = true
    }
    fun stopRefreshing(){
        main_activity_refresh.isRefreshing = false
    }
}