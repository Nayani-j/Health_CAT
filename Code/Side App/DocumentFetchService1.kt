package com.example.healthcatalyst1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.O)
class DocumentFetchService1 : Service() {

    companion object {
        private const val TAG = "DocumentFetchService1"
        private const val DOCUMENT_PATH = "emergency/xY4VKjrZsqbdkvUVvhcx"
        private const val CHANNEL_ID = "notification_channel"
        private const val NOTIFICATION_ID = 102
    }

    private val firestore = FirebaseFirestore.getInstance()
    private var notificationManager: NotificationManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isFetching = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startFetching()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildForegroundNotification("Fetching documents in the background")
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopFetching()
    }

    private fun startFetching() {
        if (!isFetching) {
            isFetching = true
            fetchNotification()
        }
    }

    private fun fetchNotification() {
        if (!isFetching) return

        firestore.document(DOCUMENT_PATH)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val notification = document.getString("notification")
                    val address = document.getString("place")
                    if (!notification.isNullOrBlank()) {
                        if (address != null) {
                            showNotification(notification,address)
                        }
                    } else {
                        Log.d(TAG, "Notification field is null or empty")
                    }
                } else {
                    Log.d(TAG, "No such document")
                }

                if (isFetching) {
                    handler.postDelayed({ fetchNotification() }, 30000)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting document", exception)
                if (isFetching) {
                    handler.postDelayed({ fetchNotification() }, 10000)
                }
            }
    }

    private fun showNotification(message: String,address: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Trigger Notification")
            .setContentText(message+"      "+address)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setSound(null)
            .setVibrate(null)
            .setOngoing(true)

        notificationManager?.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {
        val name = "Trigger Notifications"
        val descriptionText = "Shows trigger notifications"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.createNotificationChannel(channel)
    }

    private fun buildForegroundNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Document Fetch Service")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setSound(null)
            .setVibrate(null)
            .setOngoing(true)
            .build()
    }

    private fun stopFetching() {
        isFetching = false
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}


