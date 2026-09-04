package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class SchoolApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:747068637568:android:fb19908efbcace87e84165")
                    .setProjectId("academic-track")
                    .setApiKey("AIzaSyAfl-5NxHzABsJQeD4iAz9a_mloqM_7rKY")
                    .setStorageBucket("academic-track.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("SchoolApplication", "FirebaseApp initialized for AcademiaTrack (academic-track).")
            }
        } catch (e: Exception) {
            Log.e("SchoolApplication", "FirebaseApp initialization handled: ${e.message}", e)
        }
    }
}
