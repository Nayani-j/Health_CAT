package com.example.health_cat

import android.content.Context
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

// WorkManager class that performs the quantity update task
class DecreaseQuantityWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Access Firestore
        val db = FirebaseFirestore.getInstance()

        // Reference the medicines collection
        val medicinesCollection = db.collection("medicines")

        // Fetch all documents from the collection
        medicinesCollection.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val currentQuantity = document.getLong("mq") ?: 0
                    if (currentQuantity > 0) {
                        // Decrease the quantity by 1
                        medicinesCollection.document(document.id)
                            .update("mq", currentQuantity - 1)
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle the error
                println("Error updating documents: $exception")
            }

        // Indicate whether the work finished successfully with Result.success()
        return Result.success()
    }
}

// Function to schedule the periodic task using WorkManager
fun scheduleQuantityDecrease(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<DecreaseQuantityWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(24, TimeUnit.HOURS)  // Delay for 24 hours
        .build()

    // Enqueue the work request
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "DecreaseQuantityWork",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}
