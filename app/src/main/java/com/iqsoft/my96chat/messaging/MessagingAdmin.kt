package com.iqsoft.my96chat.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iqsoft.my96chat.R
import com.iqsoft.my96chat.activities.IntroActivity
import com.iqsoft.my96chat.activities.MainActivity
import com.iqsoft.my96chat.firebase.FirestoreClass
import com.iqsoft.my96chat.utils.Constants
import kotlin.random.Random

class MessagingAdmin: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.data.isNotEmpty().let {
            Log.d("testt", "message data payload: ${message.data}")
            val title = message.data[Constants.FCM_KEY_TITLE]!!
            val messageNotification = message.data[Constants.FCM_KEY_MESSAGE]!!

            sendNotification(title, messageNotification)
        }

        message.notification?.let {
            Log.d("testt", "message notificastion Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        val sharedPreferences =
            this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.USER_FCM, token)
        editor.apply()
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = if(FirestoreClass().getCurrentUserID().isNotEmpty()){
            Intent(this, MainActivity::class.java)
        }else{
            Intent(this, IntroActivity::class.java)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = this.resources.getString(R.string.default_notification_channel_id)

        val defaultSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(
            this, channelId
        ).setSmallIcon(R.drawable.ic_icon_cube)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(defaultSoundURI)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel Notif Title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(Random.nextInt(0, 9999) , notificationBuilder.build())
    }
}