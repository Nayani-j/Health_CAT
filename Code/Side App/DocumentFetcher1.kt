package com.example.healthcatalyst1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.O)
class DocumentFetcher1(private val context: Context) {
    companion object {
        private const val TAG = "DocumentFetcher"

        private const val CHANNEL_ID1 = "notification_channel1"

        private const val DOCUMENT_PATH1="emergency/xY4VKjrZsqbdkvUVvhcx"
        private const val NOTIFICATION_ID1 = 102
    }

    private val firestore = FirebaseFirestore.getInstance()
    private var notificationManager: NotificationManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isFetching = false
    private var isFetching1 = false

    init {
        startFetching()
        createNotificationChannel1()
    }

    fun startFetching() {

        if (!isFetching1) {
            isFetching1 = true
            fetchNotification1()
        }
    }


    private fun fetchNotification1() {
        if (!isFetching1) return

        firestore.document(DOCUMENT_PATH1)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val notification = document.getString("notification")
                    val address = document.getString("place")
                    if (!notification.isNullOrBlank()) {
                        if (address != null) {
                            showNotification1(notification,address)

                        }
                    } else {
                        Log.d(TAG, "Notification field is null or empty")
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
                // Schedule the next fetch if still fetching
                if (isFetching1) {

                    handler.postDelayed({ fetchNotification1() }, 10000) // Fetch every 30 seconds
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting document", exception)
                // Retry on failure if still fetching
                if (isFetching1) {
                    handler.postDelayed({ fetchNotification1() }, 10000)
                }
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel1() {
        val name = "Trigger notifications for trips"
        val descriptionText = "Shows trigger notifications"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID1, name, importance).apply {
            description = descriptionText

        }
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.createNotificationChannel(channel)
    }


    private fun showNotification1(address: String,notification:String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID1)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Trigger notification for trips")
            .setContentText(notification+"   "+address)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        notificationManager?.notify(NOTIFICATION_ID1, builder.build())
    }

    fun stopFetching() {
        isFetching = false
        handler.removeCallbacksAndMessages(null)
    }
}