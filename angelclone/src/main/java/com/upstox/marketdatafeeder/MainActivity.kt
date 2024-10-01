package com.upstox.marketdatafeeder

import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.upstox.marketdatafeeder.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.upstox.marketdatafeeder.pref.AccessTokenPref
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sharedPreferencesManager: AccessTokenPref

//    private val firebaseFirestore : FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val sharedPreferencesManagerr = SharedPreferencesManager(this)
//        sharedPreferencesManagerr.clearUserData()



        // Disable tinting for icon colors
        binding.navView.itemIconTintList = null

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_port,
                R.id.navigation_notifications,
                R.id.navigation_user
            )
        )

        // Setup BottomNavigationView with NavController
        binding.navView.setupWithNavController(navController)

        // Set Bottom Navigation Item Selection Listener
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_dashboard -> {
                    navController.navigate(R.id.navigation_dashboard)
                    true
                }
                R.id.navigation_port -> {
                    navController.navigate(R.id.navigation_port)
                    true
                }
                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications)
                    true
                }
                R.id.navigation_user -> {
                    navController.navigate(R.id.navigation_user)
                    true
                }
                else -> false
            }
        }



        sharedPreferencesManager = AccessTokenPref(this)

        // Check if access token is already saved
        showAccessTokenDialog()

        // Use the access token as needed in other activities
        val accessToken = sharedPreferencesManager.accessToken

    }





    private fun showAccessTokenDialog() {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Enter Access Token")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val accessToken = editText.text.toString()
                sharedPreferencesManager.accessToken = accessToken
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



//
//    override fun onResume() {
//        super.onResume()
//        val userUid = FirebaseAuth.getInstance().currentUser!!.uid.toString()
//        checkAccountValidity(userUid)
//    }
//
//
//    private fun checkAccountValidity(userUid: String) {
//        firebaseFirestore.collection("angelusers2")
//            .document(userUid)
//            .get()
//            .addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    // Replace 'validityDateTime' with the actual field name in your Firestore document
//                    val validityDateTimeString = documentSnapshot.getString("validity")
//                    val type = documentSnapshot.getString("type")
//
//                    if (validityDateTimeString != null) {
//                        // Parse the date and time string from Firestore into a Date object
//                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                        val validityDateTime = sdf.parse(validityDateTimeString)
//
//                        if (validityDateTime != null) {
//                            // Get the current date and time
//                            val currentDateTime = Calendar.getInstance().time
//
//                            // Check if the current date and time are after the validity date and time
//                            if (currentDateTime.after(validityDateTime)) {
//                                // The validity date and time have passed, show a toast message
//                                showToast("Your account validity has expired.")
//                            }
//                        }
//                    }
//
//                }
//            }
//            .addOnFailureListener { e ->
//                showToast("Error retrieving account validity: ${e.message}")
//            }
//    }
//

//    private fun showToast(message: String) {
//        showExpiredValidityDialog()
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    private fun showExpiredValidityDialog() {
//        val alertDialogBuilder = AlertDialog.Builder(this)
//        alertDialogBuilder.setTitle("Please Upgrade Your Validity")
//        alertDialogBuilder.setMessage("Your account validity has expired. Please upgrade your account.")
//        alertDialogBuilder.setCancelable(false) // Prevent the dialog from being dismissed by tapping outside
//
//        alertDialogBuilder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
//            // Handle the "OK" button click, e.g., finish the activity
//            Toast.makeText(this, "Account Validity Expire", Toast.LENGTH_SHORT).show()
//            finish()
//
//        }
//
//        val alertDialog = alertDialogBuilder.create()
//        alertDialog.show()
//    }



}
