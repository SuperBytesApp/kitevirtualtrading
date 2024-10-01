package com.clone.kitevirtualtrading

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BackgroundService : Service() {

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Remove data from Realtime Database here
//        val dataPathToRemove = "your/data/path"
//        databaseReference.child(dataPathToRemove).removeValue()
        Toast.makeText(this@BackgroundService, "app close", Toast.LENGTH_SHORT).show()


        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
