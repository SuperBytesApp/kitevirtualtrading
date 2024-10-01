package com.clone.kitevirtualtrading

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.clone.kitevirtualtrading.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()
//    internal lateinit var itemAdapter: ItemAdapter // Declare the itemAdapter property
//    var itemList = mutableListOf<Item>()

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferencesManager = SharedPreferencesManager(this)
        sharedPreferencesManager.clearUserData()
        sharedPreferencesManager.clearUserData2()
        sharedPreferencesManager.clearUserData3()

        Toast.makeText(this, "Updated", Toast.LENGTH_LONG).show()

        databaseReference = FirebaseDatabase.getInstance().reference
        var uid = FirebaseAuth.getInstance().currentUser!!.uid.toString()

        databaseReference.child("myData").child(uid).removeValue()


        val db = FirebaseFirestore.getInstance()

// Get a reference to the document with the specified UID
        val userDocRef = db.collection("user").document(uid)

// Retrieve the "name" field from the document
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("name")
                    if (name != null) {
                        // Use the "name" value here
                        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)

// Find the menu item by its ID
                        val menuItem = bottomNavigationView.menu.findItem(R.id.navigation_user)

// Set the new title for the menu item
                        menuItem.title = name
                    } else {
                        // Handle the case where "name" is null
                    }
                } else {
                    // Handle the case where the document does not exist
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors that may occur while fetching the document
                println("Error retrieving document: $e")
            }




//        itemAdapter = ItemAdapter(itemList,this) // Replace 'itemList' with your actual data source

//        auth = FirebaseAuth.getInstance()
//        database = FirebaseDatabase.getInstance()
//        userRef = database.reference.child("users").child(auth.currentUser?.uid ?: "")


//        userRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dataSnapshot.exists() && dataSnapshot.child("isOnline").value == true) {
//                    showToastt("You are already logged in on another device.")
//                    // You can choose to sign the user out in this case
//                    // auth.signOut()
//                } else {
//                    // Set the user as online when they log in
//                    userRef.child("isOnline").setValue(true)
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Handle errors if needed
//            }
//        })


    }




    private fun checkAccountValidity(userUid: String) {
        firebaseFirestore.collection("user")
            .document(userUid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Replace 'validityDate' with the actual field name in your Firestore document
                    val validityDateString = documentSnapshot.getString("validity")
                    val name = documentSnapshot.getString("name")


                    if (validityDateString != null) {
                        // Parse the date string from Firestore into a Date object
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val validityDate = sdf.parse(validityDateString)

                        if (validityDate != null) {
                            // Get the current date
                            val currentDate = Date()

                            // Check if the current date is after the validity date
                            if (currentDate.after(validityDate)) {
                                // The validity date has passed, show a toast message
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






    override fun onStart() {
        super.onStart()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications , R.id.navigation_tool , R.id.navigation_user
            )
        )

        navView.setupWithNavController(navController)



        // Replace 'your_uid' with the actual user's UID
        val userUid = firebaseAuth.currentUser?.uid

        // Check if the user is authenticated and has a UID
        if (userUid != null) {
            checkAccountValidity(userUid)
        }



    }


    override fun onDestroy() {
        super.onDestroy()

      Log.d("anu","destroy")


    }


}




