package com.cscorner.myclothes.menu.chatNotifications

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cscorner.myclothes.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

//Handles Firebase Cloud Messaging (FCM) notifications
class FCMNotificationService: FirebaseMessagingService() {

    //This function is called when a message is received from FCM
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("recmes", "From: ${remoteMessage.from}")
        remoteMessage.data.isNotEmpty().let {
            Log.d("recmes", "Message data payload: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            Log.d("recmes", "Message Notification Body: ${it.body}")
            //showNotification(it.body ?: "")
        }
    }

    //This function is called when a new FCM token is generated or refreshed
    override fun onNewToken(token: String) {
        Log.d("recmes", "Refreshed token: $token")
    }

//    private fun showNotification(messageBody: String) {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val channelId = "my_clothes_app_notifications"
//        val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            .setContentTitle("FCM Message")
//            .setContentText(messageBody)
//            .setSmallIcon(R.drawable.account_image)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//
//        // Show the notification
//        notificationManager.notify(1, notificationBuilder.build())
//    }
}