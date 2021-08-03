package com.iqsoft.my96chat.firebase

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.iqsoft.my96chat.activities.ConversationActivity
import com.iqsoft.my96chat.activities.EditAccountActivity
import com.iqsoft.my96chat.activities.MainActivity
import com.iqsoft.my96chat.activities.SignUpActivity
import com.iqsoft.my96chat.models.Conversation
import com.iqsoft.my96chat.models.Message
import com.iqsoft.my96chat.models.User
import com.iqsoft.my96chat.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL


class FirestoreClass() {
    private val mFirestore = FirebaseFirestore.getInstance()
    fun registerUser(activity: Activity, user: User) {
        mFirestore.collection(Constants.USERS)
            .document(user.id)
            .set(user)
            .addOnCompleteListener {
                if (activity is SignUpActivity) {
                    activity.userRegisteredComplete()
                }
            }.addOnFailureListener {
                if (activity is SignUpActivity) {
                    activity.showErrorSnackBar("Error While Signing Up")
                }
            }
    }

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun loadLoggedInUser(activity: Activity) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)!!
                if (activity is MainActivity) {
                    activity.userLoadSuccessfully(user)
                }
            }
    }

    fun createConv(activity: Activity, conv: Conversation) {
        mFirestore.collection(Constants.CONV)
            .document()
            .set(conv)
            .addOnSuccessListener {
                if (activity is MainActivity) {
                    activity.createConvSuccess()
                }
            }
    }

    fun updateUserProfile(activity: Activity, user: User) {
        val hashMap = HashMap<String, Any>()
        hashMap[Constants.USER_ID] = user.id
        hashMap[Constants.USER_NAME] = user.name
        hashMap[Constants.USER_PHONE] = user.phone
        hashMap[Constants.USER_IMAGE] = user.image
        hashMap[Constants.USER_FCM] = user.fcm
        mFirestore.collection(Constants.USERS)
            .document(user.id)
            .update(hashMap)
            .addOnSuccessListener {
                if (activity is EditAccountActivity) {
                    activity.userUpdatedSuccessfully()
                }
                if (activity is MainActivity) {
                    activity.tokenUpdateSuccess()
                }
            }
    }

    fun getUserFromIdList(activity: Activity, idList: ArrayList<String>) {
        mFirestore.collection(Constants.USERS)
            .whereIn(Constants.USER_ID, idList)
            .get()
            .addOnSuccessListener { document ->
                Log.e("testt", document.documents.toString())
                val usersList: ArrayList<User> = ArrayList()
                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if (activity is MainActivity) {
                    activity.setupConvRecyclerView(usersList)
                }
            }.addOnFailureListener {
                if (activity is MainActivity) {
                    activity.showErrorSnackBar("Error in adding the user")
                }
            }
    }

    fun getUserFromEmail(activity: Activity, email: String) {
        val emailList = ArrayList<String>()
        emailList.add(email)
        mFirestore.collection(Constants.USERS)
            .whereIn(Constants.USER_EMAIL, emailList)
            .get()
            .addOnSuccessListener { document ->
                val usersList: ArrayList<User> = ArrayList()
                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if (activity is MainActivity) {
                    activity.userFromEmailSuccess(usersList)
                }
            }.addOnFailureListener {
                if (activity is MainActivity) {
                    activity.showErrorSnackBar("Error in adding the user")
                }
            }
    }

    fun createMessage(
        activity: Activity,
        listMessages: ArrayList<Message>,
        message: Message,
        id: String
    ) {
        val hashMap = HashMap<String, Any>()
        listMessages.add(message)
        hashMap[Constants.CONV_MESSAGES] = listMessages
        mFirestore.collection(Constants.CONV)
            .document(id)
            .update(hashMap)
            .addOnSuccessListener {
                if (activity is ConversationActivity) {
                    activity.messageCreated()
                }

            }
    }


    fun loadConvsFromID(activity: Activity, currentUserID: String) {
        mFirestore.collection(Constants.CONV)
            .whereArrayContains(Constants.CONV_ASSIGNED_TO, currentUserID)
            .get()
            .addOnSuccessListener { document ->
                val convList: ArrayList<Conversation> = ArrayList()
                for (i in document.documents) {
                    val conv = i.toObject(Conversation::class.java)!!
                    conv.id = i.id
                    convList.add(conv)
                }
                if (activity is MainActivity) {
                    activity.convsLoadedSuccessfully(convList)
                }
            }
    }
    fun sendNotification(activity: Activity, token: String, title: String, message:String){
        SendNotificationToUserAsyncTask(activity,token,title,message).execute()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SendNotificationToUserAsyncTask(
        val activity: Activity,
        val token: String,
        val title: String,
        val message: String
    ) :
        AsyncTask<Any, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            if (activity is MainActivity) {
                activity.startRefreshing()
            }
            Log.e("connection", "Start")
        }

        override fun doInBackground(vararg params: Any?): String {
            var result: String
            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,
                    "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )
                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, title)
                dataObject.put(Constants.FCM_KEY_MESSAGE, message)

                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)
                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val sb = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (e: SocketTimeoutException) {
                result = "connection Timeout"
            } catch (e: Exception) {
                result = "Error: " + e.toString()
            } finally {
                connection?.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            if (activity is MainActivity) {
                activity.stopRefreshing()
            }
            Log.e("connection", "this is the result $result")
        }

    }
}