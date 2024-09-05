/*package com.example.bl

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceReceiver"
        private const val CHANNEL_ID = "geofence_channel"
    }
    var x=0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive called. Intent action: ${intent?.action}")

        if (context == null || intent == null) {
            Log.e(TAG, "Null context or intent")
            return
        }

        // Handle geofence transitions
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e(
                TAG,
                "GeofencingEvent is null. This might indicate an issue with the geofence trigger or intent."
            )
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = getErrorString(geofencingEvent.errorCode)
            Log.e(TAG, "Error in GeofencingEvent: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d(TAG, "Geofence transition: $geofenceTransition")

        val triggeringGeofences = geofencingEvent.triggeringGeofences
        Log.d(TAG, "Triggering geofences: ${triggeringGeofences?.map { it.requestId }}")

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d(TAG, "Geofence entered")
                x++
                if(x<2)
                    showNotification(context, "Entered a geofence", "You entered a geofence.")
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d(TAG, "Geofence exited")
                showNotification(context, "Exited a geofence", "You exited a geofence.")
            }

            else -> {
                Log.d(TAG, "Geofence transition not handled: $geofenceTransition")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Geofence Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getErrorString(errorCode: Int): String {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence not available"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Too many geofences"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Too many pending intents"
            else -> "Unknown error code: $errorCode"
        }
    }
}*/
package com.example.health_cat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {

        private const val TAG = "GeofenceReceiver"
        private const val CHANNEL_ID = "geofence_channel1"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()





    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive called. Intent action: ${intent?.action}")
       // saveTriggerToFirestore("", "")
        if (context == null || intent == null) {
            Log.e(TAG, "Null context or intent")
            return
        }

        // Handle geofence transitions
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e(
                TAG,
                "GeofencingEvent is null. This might indicate an issue with the geofence trigger or intent."
            )
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = getErrorString(geofencingEvent.errorCode)
            Log.e(TAG, "Error in GeofencingEvent: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d(TAG, "Geofence transition: $geofenceTransition")

        val triggeringGeofences = geofencingEvent.triggeringGeofences
        Log.d(TAG, "Triggering geofences: ${triggeringGeofences?.map { it.requestId }}")

        val userId = auth.currentUser?.uid
        if (userId != null) {
            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    Log.d(TAG, "Geofence entered")




                    /*val triggeringGeofenceId = geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId
                    val userId = auth.currentUser?.uid
                    if (triggeringGeofenceId != null) {
                        FirebaseFirestore.getInstance().collection("reminders").document(triggeringGeofenceId).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val reminderTitle = document.getString("title") ?: "Reminder"
                                    val reminderDetails = document.getString("details") ?: "You have entered the reminder area."
                                    Log.d(TAG, "Fetched reminder: $reminderTitle - $reminderDetails")
                                    if (userId != null) {*/
                                         saveTriggerToFirestore(userId, "Entered"/* $reminderTitle*/, "yes")
                                        //showNotification(context, "Entered: $reminderTitle", reminderDetails)
                                  /*  }
                                } else {
                                    Log.e(TAG, "No document found for geofence ID: $triggeringGeofenceId")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error fetching reminder document", e)
                            }
                    }*/

                       // showNotification(context, "Entered a geofence", "You entered a geofence.")
                   // saveTriggerToFirestore(userId, "Reached Hospital","yes")
                    handleGeofenceTransition(context, geofencingEvent, "Entered")
                }

                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d(TAG, "Geofence exited")

                      handleGeofenceTransition(context, geofencingEvent, "Left")
                }

                else -> {
                    Log.d(TAG, "Geofence transition not handled: $geofenceTransition")
                }
            }
        } else {
            Log.e(TAG, "User not logged in")
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleGeofenceTransition(context: Context, geofencingEvent: GeofencingEvent, transitionType: String) {
        val triggeringGeofenceId = geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId
        if (triggeringGeofenceId == null) {
            Log.e(TAG, "No triggering geofence found")

            return
        }

        Log.d(TAG, "Handling geofence transition for ID: $triggeringGeofenceId")
        val userId = auth.currentUser?.uid
        FirebaseFirestore.getInstance().collection("reminders").document(triggeringGeofenceId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val reminderTitle = document.getString("title") ?: "Reminder"
                    val reminderDetails = document.getString("details") ?: "You have $transitionType the reminder area."
                    Log.d(TAG, "Fetched reminder: $reminderTitle - $reminderDetails")
                    if (userId != null) {
                       // saveTriggerToFirestore(userId, "$transitionType: $reminderTitle", indicator)
                        showNotification(context, "$transitionType: $reminderTitle", reminderDetails)
                    }
                } else {
                    Log.e(TAG, "No document found for geofence ID: $triggeringGeofenceId")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching reminder document", e)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Geofence Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }


    private fun saveTriggerToFirestore(userId: String, message: String,indicator:String) {
        val trigger = hashMapOf(
            "userId" to userId,
            "notification" to message,
            "ind" to indicator,
             // Use the provided count
            // You can also add other fields as needed
        )

        val docRef = firestore.collection("trigger").document("eSbAmSgSTFXOEsWDMfvS")

        docRef.set(trigger, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Trigger added successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding trigger", e)
            }
    }

    private fun getErrorString(errorCode: Int): String {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence not available"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Too many geofences"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Too many pending intents"
            else -> "Unknown error code: $errorCode"
        }
    }
}

