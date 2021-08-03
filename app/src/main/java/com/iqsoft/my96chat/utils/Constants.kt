package com.iqsoft.my96chat.utils

import android.app.Activity
import android.net.Uri
import android.webkit.MimeTypeMap

object Constants {
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

    const val USERS = "Users"
    const val USER_NAME = "name"
    const val USER_ID = "id"
    const val USER_PHONE = "phone"
    const val USER_EMAIL = "email"
    const val USER_IMAGE = "image"
    const val USER_FCM = "fcm"


    const val CONV = "Conversation"
    const val CONV_ASSIGNED_TO = "assignedTo"
    const val CONV_MESSAGES = "messages"

    const val INTENT_OTHER_USER = "otherUser"
    const val INTENT_MY_USER = "myUser"
    const val INTENT_CONV = "conversation"

    const val PREFERENCES = "pref"
    const val FCM_TOKEN_UPDATED = "Fcm_token_update_state"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAA_qkiUAM:APA91bFVzFQOxUJwKLWqUqFEeUPr3VRAJjTYj0X54uBj0itmohHI55FyQB9mKbhKv622LexjZgdtsVGwx2qDhYWl3OvD2ZcQCX3JexXOKqttQe_Yb74Z272l8SMePWgk2P-WA_vOXgKw"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"

    const val REQUEST_READ_EXTERNAL_STORAGE_CODE = 1
    const val SELECT_IMAGE_REQUEST_CODE = 2
    const val ACTIVITY_UPDATE_USER = 3
}