package com.example.health_cat

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/*class GeofencingClient(private val context: Context)
{
    companion object
    {


        private const val GEOFENCE_ACTION = "com.example.theapp.ACTION_GEOFENCE"
    }

    private val TAG = "GeofencingClient"
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    @SuppressLint("MissingPermission")
    fun addGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        radius: Float,
        expirationDuration: Long = Geofence.NEVER_EXPIRE
    ) {
        Log.d(TAG, "Adding geofence: $id, Lat: $latitude, Lon: $longitude, Radius: $radius")

        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude, longitude, radius)
            .setExpirationDuration(expirationDuration)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = GEOFENCE_ACTION

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )


        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "Geofence added successfully: $id")
            }
            .addOnFailureListener { e ->
                if (e is ApiException) {
                    val errorMessage = getErrorString(e.statusCode)
                    Log.e(TAG, "Failed to add geofence: $errorMessage")
                } else {
                    Log.e(TAG, "Failed to add geofence", e)
                }
            }
    }

}

/*fun removeGeofence(id: String) {
    Log.d(TAG, "Removing geofence: $id")
    GeofencingClient.removeGeofences(listOf(id))
        .addOnSuccessListener {
            Log.d(TAG, "Geofence $id removed successfully")
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "Failed to remove geofence: $id", e)
        }
}*/

private fun getErrorString(errorCode: Int): String {
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence service is not available now"
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Your app has registered too many geofences"
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "You have provided too many PendingIntents to the addGeofences() call"
        else -> "Unknown error: $errorCode"
    }

}*/
class GeofencingClient(private val context: Context) {
    companion object {
        private const val GEOFENCE_ACTION = "com.example.theapp.ACTION_GEOFENCE"
    }
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "GeofencingClient"
    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    @SuppressLint("MissingPermission")
    fun addGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        radius: Float,
        expirationDuration: Long = Geofence.NEVER_EXPIRE
    ) {
        Log.d(TAG, "Adding geofence: $id, Lat: $latitude, Lon: $longitude, Radius: $radius")

        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude, longitude, radius)
            .setExpirationDuration(expirationDuration)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = GEOFENCE_ACTION

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "Geofence added successfully: $id")
                if (userId != null) {
                    saveTriggerToFirestore(userId, "Outside the geofence")
                }
            }
            .addOnFailureListener { e ->
                if (e is ApiException) {
                    val errorMessage = getErrorString(e.statusCode)
                    Log.e(TAG, "Failed to add geofence: $errorMessage")
                } else {
                    Log.e(TAG, "Failed to add geofence", e)
                }
            }
    }

    fun removeGeofence(id: String) {
        Log.d(TAG, "Removing geofence: $id")
        geofencingClient.removeGeofences(listOf(id))
            .addOnSuccessListener {
                Log.d(TAG, "Geofence $id removed successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove geofence: $id", e)
            }
    }

    private fun getErrorString(errorCode: Int): String {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence service is not available now"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Your app has registered too many geofences"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "You have provided too many PendingIntents to the addGeofences() call"
            else -> "Unknown error: $errorCode"
        }
    }
    private fun saveTriggerToFirestore(userId: String, message: String) {
        val trigger = hashMapOf(
            "userId" to userId,
            "notification" to message,
            // Use the provided count
            // You can also add other fields as needed
        )

        val docRef = firestore.collection("Trigger").document("50kc50upUXTWnpM68eKE")

        docRef.set(trigger, SetOptions.merge())
            .addOnSuccessListener {
                //Log.d(GeofencingClient.TAG, "Trigger added successfully")
            }
            .addOnFailureListener { e ->
               // Log.e(GeofencingClient.TAG, "Error adding trigger", e)
            }
    }
}