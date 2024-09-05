/*package com.example.bl

import kotlinx.coroutines.*
import kotlin.math.*
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GeofenceService : Service() {


   // private lateinit var documentFetcher: DocumentFetcher
   private lateinit var locationClient: LocationClient



    private val handler = Handler(Looper.getMainLooper())
    private lateinit var geofencingClient: GeofencingClient
    private val geofenceId = "DYNAMIC_GEOFENCE_ID"
    private lateinit var firestore: FirebaseFirestore
    private lateinit var locationListener: ListenerRegistration

    val auth = FirebaseAuth.getInstance()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        geofencingClient = GeofencingClient(this)

        firestore = FirebaseFirestore.getInstance()
        scheduleGeofenceUpdates()
    }

    private fun scheduleGeofenceUpdates() {
        handler.postDelayed(object : Runnable {
            override fun run() {

                updateGeofenceFromFirestore()
                handler.postDelayed(this, 5000) // 6 seconds
            }
        }, 5000)
    }

    private fun updateGeofenceFromFirestore() {





        val documentId = "0QYTnkUReYcAxR2Ybmo9" // Replace with your document ID
        val documentRef = firestore.collection("locations").document(documentId)

        documentRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                updateGeofence(latitude, longitude)
            } else {
                Log.e(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error getting document: ", exception)
        }
    }

    private fun updateGeofence(latitude: Double, longitude: Double) {
        geofencingClient.removeGeofence(geofenceId)
        geofencingClient.addGeofence(geofenceId, latitude, longitude, 100f, 60000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        geofencingClient.removeGeofence(geofenceId)
    }

    companion object {
        private const val TAG = "GeofenceService"
    }



}*/
