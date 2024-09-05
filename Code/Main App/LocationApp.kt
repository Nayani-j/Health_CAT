package com.example.health_cat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp

class LocationApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        Log.d("LocationApp", "Initializing Firebase...")
       // FirebaseApp.initializeApp(this)
        Log.d("LocationApp", "Firebase initialized")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location",
                "Location",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
