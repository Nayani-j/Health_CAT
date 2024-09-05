/*package com.example.theapp

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
class DocumentFetcher(private val context: Context) {

    companion object {
        private const val TAG = "DocumentFetcher"
        private const val DOCUMENT_PATH = "Trigger/50kc50upUXTWnpM68eKE" // Update with your Firestore path
        private const val CHANNEL_ID = "notification_channel"
        private const val NOTIFICATION_ID = 101 // Unique ID for the notification
    }

    private val firestore = FirebaseFirestore.getInstance()
    private var notificationManager: NotificationManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isFetching = false

    init {
        startFetching()
        createNotificationChannel()
    }

    fun startFetching() {
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
                    if (!notification.isNullOrBlank()) {
                       // if(notification == "Outside the geofence") {
                            showNotification(notification)
                       // }
                    } else {
                        Log.d(TAG, "Notification field is null or empty")
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
                // Schedule the next fetch if still fetching
                if (isFetching) {
                    handler.postDelayed({ fetchNotification() }, 30000) // Fetch every 10 seconds
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting document", exception)
                // Retry on failure if still fetching
                if (isFetching) {
                    handler.postDelayed({ fetchNotification() }, 10000)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Trigger Notifications"
            val descriptionText = "Shows trigger notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification(message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Trigger Notification")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationManager?.notify(NOTIFICATION_ID, builder.build())
    }

    fun stopFetching() {
        isFetching = false
        handler.removeCallbacksAndMessages(null)
    }

}*/
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
class DocumentFetcher(private val context: Context) {
    companion object {
        private const val TAG = "DocumentFetcher"
        private const val DOCUMENT_PATH = "trigger/eSbAmSgSTFXOEsWDMfvS" // Update with your Firestore path
        private const val CHANNEL_ID = "notification_channel"

        private const val NOTIFICATION_ID = 101 // Unique ID for the notification

    }

    private val firestore = FirebaseFirestore.getInstance()
    private var notificationManager: NotificationManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isFetching = false


    init {
        startFetching()
        createNotificationChannel()

    }

    fun startFetching() {
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

                    if (!notification.isNullOrBlank()) {
                        showNotification(notification)
                    } else {
                        Log.d(TAG, "Notification field is null or empty")
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
                // Schedule the next fetch if still fetching
                if (isFetching) {
                    Log.d(TAG,"abcd")
                    handler.postDelayed({ fetchNotification() }, 30000) // Fetch every 30 seconds
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting document", exception)
                // Retry on failure if still fetching
                if (isFetching) {

                    handler.postDelayed({ fetchNotification() }, 10000)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = "Trigger Notifications"
        val descriptionText = "Shows trigger notifications"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.createNotificationChannel(channel)
    }


    private fun showNotification(message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Trigger Notification")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setSound(null)
            .setVibrate(null)
            .setOngoing(true)

        notificationManager?.notify(NOTIFICATION_ID, builder.build())
    }


    fun stopFetching() {
        isFetching = false
        handler.removeCallbacksAndMessages(null)
    }
}