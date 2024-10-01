package com.clone.upstoxclone.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.clone.upstoxclone.MainActivity
import com.clone.upstoxclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var auth: FirebaseAuth


    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference


        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->

                        if (task.isSuccessful) {
                            // Login success
                            // You can navigate to another activity or perform other actions here


                            getUserOnlineStatus()


                        } else {


                            Toast.makeText(this, "something wrong", Toast.LENGTH_SHORT).show()

                            // Login failed
                            // Handle the error, e.g., show a message to the user
                        }
                    }
            } else {
                // Handle empty fields
            }
        }
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun setUserOnlineStatus(isOnline: String) {
        val user = auth.currentUser
        user?.let {
            val uid = user.uid
            val userRef = databaseReference.child("upstoxusers").child(uid)
            userRef.child("isOnline").setValue(isOnline)
        }
    }


    private fun getUserOnlineStatus() {
        val user = auth.currentUser
        user?.let {
            val uid = user.uid
            val userRef = databaseReference.child("upstoxusers").child(uid)

            // Check if the user exists
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val isOnline = dataSnapshot.child("isOnline").getValue(String::class.java)

                        if (isOnline != "") {
                        // Use the value of "isOnline" here
                        // You can print it or perform any other actions
                        val deviceId = getDeviceId(this@LoginActivity)

                        if (isOnline.toString() == deviceId.toString()){

                            intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            Toast.makeText(this@LoginActivity, "Logged In", Toast.LENGTH_SHORT).show()

                        }else{

                            Toast.makeText(this@LoginActivity, "This Account Already Logged In Other Device !", Toast.LENGTH_SHORT).show()


                        }


                    } else {


                        val deviceId = getDeviceId(this@LoginActivity)

                        val user = auth.currentUser
                        user?.let {
                            val uid = user.uid
                            val userRef = databaseReference.child("upstoxusers").child(uid)
                            userRef.child("isOnline").setValue(deviceId.toString())

                            intent = Intent(this@LoginActivity,MainActivity::class.java)
                            startActivity(intent)
                            finish()

                            Toast.makeText(this@LoginActivity, "Logged In", Toast.LENGTH_SHORT).show()

                        }
                        // Handle the case where the "isOnline" field doesn't exist or is null
                        println("isOnline field is null or doesn't exist")
                    }
                } else {

                        val deviceId = getDeviceId(this@LoginActivity)

                        val user = auth.currentUser
                        user?.let {
                            val uid = user.uid
                            val userRef = databaseReference.child("upstoxusers").child(uid)
                            userRef.child("isOnline").setValue(deviceId.toString())
                            intent = Intent(this@LoginActivity,MainActivity::class.java)
                            startActivity(intent)
                            finish()

                            Toast.makeText(this@LoginActivity, "Logged In", Toast.LENGTH_SHORT).show()

                        }

                    // Handle the case where the user doesn't exist
                    println("User with UID $uid does not exist")
                }
            }

                    override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur during the read operation
                println("Database Error: ${databaseError.message}")
            }
        })
    }
}






}