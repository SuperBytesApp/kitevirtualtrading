package com.clone.upstoxclone

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.clone.upstoxclone.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val firebaseFirestore : FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val sharedPreferencesManager = SharedPreferencesManager(this)
        sharedPreferencesManager.clearUserData()
        sharedPreferencesManager.clearUserData2()
        sharedPreferencesManager.clearUserData3()


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_user
            )
        )

        navView.setupWithNavController(navController)




    }




    override fun onResume() {
        super.onResume()
        val userUid = FirebaseAuth.getInstance().currentUser!!.uid.toString()

        // Check if the user is authenticated and has a UID
        if (userUid != null) {

            // In your MainActivity.kt or Application class

// To set the initial validity in minutes and timestamp


            checkAccountValidity(userUid)
        }

    }


    private fun checkAccountValidity(userUid: String) {
        firebaseFirestore.collection("upstoxusers")
            .document(userUid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Replace 'validityDateTime' with the actual field name in your Firestore document
                    val validityDateTimeString = documentSnapshot.getString("validity")
                    val name = documentSnapshot.getString("name")

                    if (validityDateTimeString != null) {
                        // Parse the date and time string from Firestore into a Date object
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val validityDateTime = sdf.parse(validityDateTimeString)

                        if (validityDateTime != null) {
                            // Get the current date and time
                            val currentDateTime = Calendar.getInstance().time

                            // Check if the current date and time are after the validity date and time
                            if (currentDateTime.after(validityDateTime)) {
                                // The validity date and time have passed, show a toast message
                                showToast("Your account validity has expired.")
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error retrieving account validity: ${e.message}")
            }
    }


    private fun showToast(message: String) {

        showExpiredValidityDialog()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showExpiredValidityDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Please Upgrade Your Validity")
        alertDialogBuilder.setMessage("Your account validity has expired. Please upgrade your account.")
        alertDialogBuilder.setCancelable(false) // Prevent the dialog from being dismissed by tapping outside

        alertDialogBuilder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
            // Handle the "OK" button click, e.g., finish the activity
            finish()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }





}