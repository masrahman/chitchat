package com.maasrahman.chitchat.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationManager
import android.R
import android.app.Notification
import android.content.Context
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.content.Intent
import android.app.NotificationChannel
import android.graphics.Color
import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import com.maasrahman.chitchat.data.AppDatabase
import com.maasrahman.chitchat.data.entity.MessageEntity
import com.maasrahman.chitchat.ui.chat.ChatActivity
import com.maasrahman.chitchat.utils.UserData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.media.AudioAttributes



class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        val userId = remoteMessage?.data?.get("userId")
        val name = remoteMessage?.data?.get("name")
        val date = remoteMessage?.data?.get("dateTime")
        val message = remoteMessage?.data?.get("messages")
        val type = remoteMessage?.data?.get("dataType")

        insertMessage(MessageEntity(userId = userId.toString(), nama = name.toString(), dateTime = date.toString(),
            message = message.toString(), dataType = type.toString()))

        val intent = Intent(this, ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle("Chit Chat")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle())
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id",
                "Chit Chat",
                NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.description = remoteMessage?.data?.get("messages").toString()
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.setShowBadge(true)
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), attributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }


    private fun insertMessage(model: MessageEntity){
        println("INSERT MESSAGE NIH BRO")
        val dao = AppDatabase(applicationContext).messageDao()
        dao.insertMessage(model)
        sendBroadcast(model)
    }

    private fun sendBroadcast(model: MessageEntity){
        val broadcaster = LocalBroadcastManager.getInstance(baseContext)
        val intent = Intent("UPDATE_UI")
        intent.putExtra("model", model)
        broadcaster.sendBroadcast(intent)
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        val addData = hashMapOf(
            getString(com.maasrahman.chitchat.R.string.token) to token
        )
        if(UserData.loadString(baseContext,getString(com.maasrahman.chitchat.R.string.emailvalue)) != ""){
            val db = FirebaseFirestore.getInstance()
            db.collection(getString(com.maasrahman.chitchat.R.string.users))
                .document(UserData.loadString(baseContext, getString(com.maasrahman.chitchat.R.string.emailvalue)))
                .update(addData as Map<String, Any>)
        }
    }
}